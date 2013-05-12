/*
 * Copyright (C) 2013 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.glassware;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.phsystems.irisx.utils.betaface.Face;
import ru.phsystems.irisx.utils.betaface.Image;
import ru.phsystems.irisx.utils.betaface.Person;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.mirror.Mirror;
import com.google.api.services.mirror.model.Attachment;
import com.google.api.services.mirror.model.Contact;
import com.google.api.services.mirror.model.Notification;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.api.services.mirror.model.UserAction;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Blob;
import com.neatocode.medrefglass.model.Dao;
import com.neatocode.medrefglass.model.EMFService;
import com.neatocode.medrefglass.model.FileUpload;
import com.neatocode.medrefglass.model.Subject;
import com.neatocode.medrefglass.model.cards.ErrorCard;
import com.neatocode.medrefglass.model.cards.NoSearchMatchesCard;
import com.neatocode.medrefglass.model.cards.PatientNotesCard;
import com.neatocode.medrefglass.model.cards.SearchResultsCard;
import com.neatocode.medrefglass.model.cards.WelcomeCard;

public class NotifyServlet extends HttpServlet {

	private static final Logger LOG = Logger.getLogger(NotifyServlet.class
			.getSimpleName());

	@Override
	protected void doPost(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		LOG.info("doPost");

		ack(response);

		final Notification notification = getNotification(request);
		final String userId = notification.getUserToken();
		final Credential credential = AuthUtil.getCredential(userId);
		final Mirror glass = MirrorClient.getMirror(credential);

		// Get Timeline item notification is about.
		final TimelineItem replyItem = glass.timeline()
				.get(notification.getItemId()).execute();
		LOG.info("Shared timeline item: " + replyItem.toPrettyString());

		if ("INSERT".equals(notification.getOperation())
				&& notification.getUserActions().contains(
						new UserAction().setType("REPLY"))) {
			final String spokenText = replyItem.getText();
			LOG.info("reply action received, spoken text: " + spokenText);

			// Get Timeline item notification was sent from.
			final TimelineItem sourceItem = glass.timeline()
					.get(replyItem.getInReplyTo()).execute();
			LOG.info("Source timeline item: " + sourceItem.toPrettyString());

			// Set patient from welcome card.
			if (sourceItem.getSourceItemId().equals(WelcomeCard.SOURCE_ID)) {
				LOG.info("setting patient");

				// Add new patient or switch to a previous, spokenText is name.
				final Subject s = Dao.INSTANCE.createSubject(
						notification.getUserToken(), spokenText);
				SearchResultsCard.insert(request, credential, s);

				return;

				// Add a voice note to a patient.
			} else if (sourceItem.getSourceItemId().equals(
					SearchResultsCard.SOURCE_ID)) {
				LOG.info("adding patient voice note");

				Dao.INSTANCE.addNote(notification.getUserToken(), spokenText);
				return;
			}

			// No other INSERT/REPLY notifications coded yet.
			LOG.warning("I don't know what to do with this notification, so I'm ignoring it.");
			return;
		}

		// Handle custom action to see previous notes/images.
		if ("UPDATE".equals(notification.getOperation())) {
			for (final UserAction userAction : notification.getUserActions()) {
				if (userAction.getType().equals("CUSTOM")) {
					final String payload = userAction.getPayload();
					LOG.info("custom action received, payload: " + payload);
					if (payload.startsWith("seeNotes")) {
						LOG.info("Returning patient notes...");

						final Subject s = Dao.INSTANCE
								.getCurrentSubject(notification.getUserToken());
						// TODO send not found if no notes?

						// TODO edit previous requests and bring them to the
						// front
						// instead of many dupes?

						// TODO word wrap text

						// TODO sort cards by date

						// XXX This is weird, but the persistence layer has to
						// be left
						// open to retrieve the blob keys off the file objects.
						// and stream the data out.
						final EntityManager em = EMFService.get()
								.createEntityManager();
						try {
							List<FileUpload> files = Dao.INSTANCE.getFilesBy(
									em, s.getId());
							PatientNotesCard.insert(request, credential, s,
									files);
						} finally {
							em.close();
						}
						return;
					}
				}
			}

			// No other UPDATE notifications coded yet.
			LOG.warning("I don't know what to do with this notification, so I'm ignoring it.");
			return;
		}

		// Shares to the inserted contacts.
		if ("INSERT".equals(notification.getOperation())
				&& notification.getUserActions().contains(
						new UserAction().setType("SHARE"))) {

			handleImageShare(request, notification, credential, replyItem);
			return;
		}

		LOG.warning("I don't know what to do with this notification, so I'm ignoring it.");
	}

	private Notification getNotification(HttpServletRequest request)
			throws IOException {
		// Get the notification object from the request body (into a string so
		// we
		// can log it)
		BufferedReader notificationReader = new BufferedReader(
				new InputStreamReader(request.getInputStream()));
		String notificationString = "";

		// Count the lines as a very basic way to prevent Denial of Service
		// attacks
		int lines = 0;
		while (notificationReader.ready()) {
			notificationString += notificationReader.readLine();
			lines++;

			// No notification would ever be this long. Something is very wrong.
			if (lines > 1000) {
				throw new IOException(
						"Attempted to parse notification payload that was unexpectedly long.");
			}
		}

		LOG.info("got raw notification " + notificationString);

		JsonFactory jsonFactory = new JacksonFactory();

		// If logging the payload is not as important, use
		// jacksonFactory.fromInputStream instead.
		Notification notification = jsonFactory.fromString(notificationString,
				Notification.class);
		return notification;
	}

	private void ack(HttpServletResponse response) throws IOException {
		LOG.info("ack");
		// Respond with OK and status 200 in a timely fashion to prevent
		// redelivery
		response.setContentType("text/html");
		Writer writer = response.getWriter();
		writer.append("OK");
		writer.close();
	}

	private static boolean containsContact(final String aId,
			final List<Contact> contacts) {
		for (Contact contact : contacts) {
			if (contact.getId().equals(aId)) {
				return true;
			}
		}
		return false;
	}

	private void handleImageShare(HttpServletRequest request,
			Notification notification, Credential credential,
			TimelineItem replyItem) {
		LOG.info("handleImageShare.");

		try {
			// Get image shared with app.
			final Mirror glass = MirrorClient.getMirror(credential);
			List<Attachment> attachments = replyItem.getAttachments();
			if (null == attachments || attachments.isEmpty()) {
				LOG.info("No attachment found.");
				return;
			}
			InputStream is = downloadAttachment(glass, attachments.get(0),
					credential);
			Image trainingImage = new Image(is);

			if (containsContact("remember", replyItem.getRecipients())) {
				LOG.info("saving photo to patient notes");
				Subject s = Dao.INSTANCE.getCurrentSubject(notification
						.getUserToken());
				if (null != s) {
					// Save photo for sending back in response to searches.
					FileUpload file = new FileUpload();
					file.setSubjectId(s.getId());
					byte[] imageBytes = trainingImage.getImageBytes();
					BlobKey key = Dao.INSTANCE.writeImageData(imageBytes);
					file.setBlob(key);
					file.setBlobSize(imageBytes.length);
					// file.setBlob(new Blob(trainingImage.getImageBytes()));
					Dao.INSTANCE.saveFile(file);
				}
			}

			// Extract face.
			final ArrayList<Face> faces = trainingImage.getFaces();
			// TODO support multiple faces
			final Face face = null != faces && faces.size() > 0 ? faces.get(0)
					: null;

			if (containsContact("remember", replyItem.getRecipients())) {
				LOG.info("saving face id to patient");
				
				// Save face UUID for comparisons
				Subject s = Dao.INSTANCE.addFace(notification.getUserToken(),
						face.getUID());

				return;
			}

			if (containsContact("reference", replyItem.getRecipients())) {
				LOG.info("performing face search");
				
				if (null == face) {
					ErrorCard.insert(request, credential);
					return;
				}

				// Lookup subject by facial recognition
				Person probe = new Person();
				probe.addUID(face.getUID());

				// For all entered subjects.
				List<Subject> subjects = Dao.INSTANCE.listSubjects(notification.getUserToken());
				for (Subject s : subjects) {
					float confidence = probe.compareWithUIDsForConfidence(s
							.getFaces());
					s.setMatch(confidence);
				}

				Collections.sort(subjects);
				boolean sentResult = false;
				List<Subject> results = new LinkedList<Subject>();
				for (Subject s : subjects) {
					if (results.size() > 5) {
						break;
					}
					SearchResultsCard.insert(request, credential, s);
					sentResult = true;
				}

				if (!sentResult) {
					NoSearchMatchesCard.insert(request, credential);
					return;
				}
			}

		} catch (Throwable e) {
			LOG.log(Level.SEVERE, "Error handling search request.", e);
		}
	}

	/**
	 * Download a timeline items's attachment.
	 * 
	 * @param service
	 *            Authorized Mirror service.
	 * @param itemId
	 *            ID of the timeline item to download the attachment for.
	 * @param attachment
	 *            Attachment to download content for.
	 * @return The attachment content on success, {@code null} otherwise.
	 */
	public static InputStream downloadAttachment(Mirror service,
			Attachment attachment, Credential credential) {
		return SendServerRequest.streamGet(attachment.getContentUrl(),
				credential.getAccessToken());
	}

}
