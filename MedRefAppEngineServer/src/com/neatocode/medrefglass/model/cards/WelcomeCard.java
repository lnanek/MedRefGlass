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
package com.neatocode.medrefglass.model.cards;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.mirror.model.MenuItem;
import com.google.api.services.mirror.model.MenuValue;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.Subscription;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.common.collect.Lists;
import com.google.glassware.AuthUtil;
import com.google.glassware.MirrorClient;
import com.google.glassware.WebUtil;

public class WelcomeCard {

	public static final String SOURCE_ID = "welcome";

	private static final Logger LOG = Logger.getLogger(WelcomeCard.class
			.getSimpleName());

	public static void insert(HttpServletRequest req, String userId)
			throws IOException {
		try {
			Credential credential = AuthUtil.newAuthorizationCodeFlow()
					.loadCredential(userId);

			TimelineItem timelineItem = new TimelineItem();
			timelineItem.setSourceItemId(SOURCE_ID);
			timelineItem.setText("MedRef");
			timelineItem.setSpeakableText("MedRef");

			timelineItem.setNotification(new NotificationConfig()
					.setLevel("DEFAULT"));

			List<MenuItem> menuItems = new LinkedList<MenuItem>();

			final MenuItem reply = new MenuItem().setAction("REPLY");
			final MenuValue icon = new MenuValue().setIconUrl(WebUtil.buildUrl(
					req, "/static/images/medref_mark_menu_item_50x50.png"));
			final MenuValue replyLabel = new MenuValue();
			replyLabel.set("displayName", "SET PATIENT");
			reply.setValues(Lists.newArrayList(replyLabel, icon));
			menuItems.add(reply);

			menuItems.add(new MenuItem().setAction("TOGGLE_PINNED"));
			menuItems.add(new MenuItem().setAction("DELETE"));

			timelineItem.setMenuItems(menuItems);

			final String contentType = "image/png";
			final String appBaseUrl = WebUtil.buildUrl(req, "/");
			final String imageUrl = appBaseUrl
					+ "static/images/medref_welcome_card_640x360.png";
			final URL url = new URL(imageUrl);

			MirrorClient.insertTimelineItem(credential, timelineItem,
					contentType, url.openStream());

			LOG.info("Bootstrapper inserted welcome message for user " + userId);
		} catch (Exception e) {
			LOG.log(Level.INFO, "Error inserting welcome card.", e);
		}
	}

	public static void subscribe(HttpServletRequest req, String userId)
			throws IOException {
		Credential credential = AuthUtil.newAuthorizationCodeFlow()
				.loadCredential(userId);

		try {
			// Subscribe to timeline updates
			Subscription subscription = MirrorClient.insertSubscription(
					credential, WebUtil.buildUrl(req, "/notify"), userId,
					"timeline");
			LOG.info("Bootstrapper inserted subscription "
					+ subscription.getId() + " for user " + userId);
		} catch (GoogleJsonResponseException e) {
			LOG.warning("Failed to create timeline subscription. Might be running on "
					+ "localhost. Details:" + e.getDetails().toPrettyString());
		}
	}
}
