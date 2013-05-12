package com.neatocode.medrefglass.model.cards;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.mirror.model.MenuItem;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.glassware.MirrorClient;
import com.google.glassware.WebUtil;

public class ErrorCard {

	private static final Logger LOG = Logger.getLogger(ErrorCard.class
			.getSimpleName());

	public static boolean insert(HttpServletRequest request, Credential credential) {
		LOG.info("insert");

		TimelineItem timelineItem = new TimelineItem();
		timelineItem.setSourceItemId("medref-results-error");
		// TODO return not OK from notify servlet so Google tries again automatically
		timelineItem.setText("Error. Please try later");
		timelineItem.setSpeakableText("Error. Please try later");

		timelineItem.setNotification(new NotificationConfig()
				.setLevel("DEFAULT"));

		// TODO support all same actions as welcome card to make easier for users?
		 List<MenuItem> menuItems = new LinkedList<MenuItem>();
		menuItems.add(new MenuItem().setAction("READ_ALOUD"));
		menuItems.add(new MenuItem().setAction("DELETE"));
		timelineItem.setMenuItems(menuItems);

		final String contentType = "image/jpeg";
		final String appBaseUrl = WebUtil.buildUrl(request, "/");
		final String imageUrl = appBaseUrl + "static/images/medref_reference_contact_640x360.png";
		try {		
			URL url = new URL(imageUrl);
			MirrorClient.insertTimelineItem(credential, timelineItem, contentType, url.openStream());
		} catch (MalformedURLException e1) {
			LOG.log(Level.SEVERE, "Failed to add no matches card to timeline.", e1);
			return false;
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Failed to add no matches card to timeline.", e);
			return false;
		}	
		
		return true;
	}

}
