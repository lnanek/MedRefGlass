package com.neatocode.medrefglass.model.cards;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.mirror.model.MenuItem;
import com.google.api.services.mirror.model.MenuValue;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.common.collect.Lists;
import com.google.glassware.MirrorClient;
import com.google.glassware.WebUtil;
import com.neatocode.medrefglass.model.Dao;
import com.neatocode.medrefglass.model.EMFService;
import com.neatocode.medrefglass.model.FileUpload;
import com.neatocode.medrefglass.model.Subject;

public class PatientNotesCard {
	
	// TODO refresh menu option on notes?

	public static final String SOURCE_ID = "medref-results-found";

	private static final Logger LOG = Logger.getLogger(PatientNotesCard.class
			.getSimpleName());

	public static boolean insert(HttpServletRequest request,
			Credential credential, Subject found, List<FileUpload> keys) {
		LOG.info("insert");

		final String bundleId = UUID.randomUUID().toString();

		TimelineItem timelineItem = new TimelineItem();
		timelineItem.setSourceItemId(SOURCE_ID);
		timelineItem.setText("Notes: " + found.getName());
		timelineItem.setSpeakableText("Notes: " + found.getName());
		timelineItem.setBundleId(bundleId);
		timelineItem.setIsBundleCover(true);

		// Triggers an audible tone when the timeline item is received
		timelineItem.setNotification(new NotificationConfig()
				.setLevel("DEFAULT"));

		List<MenuItem> menuItems = new LinkedList<MenuItem>();

		final MenuItem reply = new MenuItem().setAction("REPLY");
		final MenuValue icon = new MenuValue().setIconUrl(WebUtil.buildUrl(
				request, "/static/images/medref_record_menu_item_50x50.png"));
		final MenuValue replyLabel = new MenuValue();
		replyLabel.set("displayName", "ADD NOTE");
		reply.setValues(Lists.newArrayList(replyLabel, icon));
		menuItems.add(reply);

		menuItems.add(new MenuItem().setAction("TOGGLE_PINNED"));
		menuItems.add(new MenuItem().setAction("READ_ALOUD"));
		menuItems.add(new MenuItem().setAction("DELETE"));
		timelineItem.setMenuItems(menuItems);

		final String contentType = "image/png";
		final String appBaseUrl = WebUtil.buildUrl(request, "/");
		final String imageUrl = appBaseUrl
				+ "static/images/medref_reference_contact_640x360.png";
		try {
			URL url = new URL(imageUrl);
			MirrorClient.insertTimelineItem(credential, timelineItem,
					contentType, url.openStream());
		} catch (MalformedURLException e1) {
			LOG.log(Level.SEVERE, "Failed to add card to timeline.", e1);
			return false;
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Failed to add card to timeline.", e);
			return false;
		}


		LOG.info("Outputting files: " + keys.size());
		for (FileUpload key : keys) {
			insertNote(request, credential, key, bundleId);
			//if (!insertNote(request, credential, key, bundleId)) {
			//	return false;
			//}

		}
		
		List<String> notes = found.getNotes();
		LOG.info("Outputting notes, found: " + notes.size());		
		for (String note : notes) {
			if (!insertNote(request, credential, note, bundleId)) {
				return false;
			}
		}


		return true;
	}

	private static boolean insertNote(HttpServletRequest request,
			Credential credential, String note, final String bundleId) {
		LOG.info("insertNote");

		TimelineItem timelineItem = new TimelineItem();
		timelineItem.setText(note);
		timelineItem.setSpeakableText(note);
		timelineItem.setBundleId(bundleId);
		timelineItem.setIsBundleCover(false);

		// TODO allow deleting notes. Have to remove from DB too, not just user
		// timeline.
		List<MenuItem> menuItems = new LinkedList<MenuItem>();
		menuItems.add(new MenuItem().setAction("READ_ALOUD"));
		timelineItem.setMenuItems(menuItems);

		final String contentType = "image/png";
		final String appBaseUrl = WebUtil.buildUrl(request, "/");
		final String imageUrl = appBaseUrl
				+ "static/images/medref_reference_contact_640x360.png";
		try {
			URL url = new URL(imageUrl);
			MirrorClient.insertTimelineItem(credential, timelineItem,
					contentType, url.openStream());
		} catch (MalformedURLException e1) {
			LOG.log(Level.SEVERE, "Failed to add card to timeline.", e1);
			return false;
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Failed to add card to timeline.", e);
			return false;
		}

		return true;
	}

	private static boolean insertNote(HttpServletRequest request,
			Credential credential, FileUpload key, final String bundleId) {
		LOG.info("insertNote");

		TimelineItem timelineItem = new TimelineItem();
		timelineItem.setBundleId(bundleId);
		timelineItem.setIsBundleCover(false);

		final String contentType = "image/jpeg";
		try {
			byte[] data = Dao.INSTANCE.readImageData(key.getBlob(),
					key.getBlobSize());
			MirrorClient.insertTimelineItem(credential, timelineItem,
					contentType, data);

		} catch (MalformedURLException e1) {
			LOG.log(Level.SEVERE, "Failed to add card to timeline.", e1);
			return false;
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Failed to add card to timeline.", e);
			return false;
		}

		return true;
	}

}
