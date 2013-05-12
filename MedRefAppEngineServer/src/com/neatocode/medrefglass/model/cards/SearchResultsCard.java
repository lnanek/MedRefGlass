package com.neatocode.medrefglass.model.cards;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.mirror.model.MenuItem;
import com.google.api.services.mirror.model.MenuValue;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.common.collect.Lists;
import com.google.glassware.MirrorClient;
import com.google.glassware.WebUtil;
import com.neatocode.medrefglass.model.Subject;

public class SearchResultsCard {

	public static final String SOURCE_ID = "medref-results-found";

	private static final Logger LOG = Logger.getLogger(SearchResultsCard.class
			.getSimpleName());

	public static boolean insert(HttpServletRequest request,
			Credential credential, Subject found) {
		LOG.info("insert");

		TimelineItem timelineItem = new TimelineItem();
		timelineItem.setSourceItemId(SOURCE_ID);
		if ( found.getMatch() > 0 ) {
			final String text = Math.round(found.getMatch() * 100) + "% " + found.getName();
		timelineItem.setText(text);
		timelineItem.setSpeakableText(text);
		} else {

			timelineItem.setText(found.getName());
			timelineItem.setSpeakableText(found.getName());
		}

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

		List<MenuValue> menuValues = new ArrayList<MenuValue>();	
		menuValues.add(new MenuValue().setIconUrl(
				WebUtil.buildUrl(request, "/static/images/medref_search_menu_item_50x50.png"))
				.setDisplayName("GET NOTES"));
		menuItems.add(new MenuItem()
				.setValues(menuValues)
				.setId("seeNotes")
				.setAction("CUSTOM"));

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
			LOG.log(Level.SEVERE, "Failed to add no matches card to timeline.",
					e1);
			return false;
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Failed to add no matches card to timeline.",
					e);
			return false;
		}

		return true;
	}

}
