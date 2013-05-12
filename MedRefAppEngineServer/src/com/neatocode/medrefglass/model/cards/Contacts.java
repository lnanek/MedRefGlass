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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.mirror.model.Contact;
import com.google.api.services.mirror.model.MenuItem;
import com.google.api.services.mirror.model.MenuValue;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.Subscription;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.common.collect.Lists;
import com.google.glassware.AuthUtil;
import com.google.glassware.MirrorClient;
import com.google.glassware.WebUtil;

public class Contacts {

	private static final Logger LOG = Logger
			.getLogger(Contacts.class.getSimpleName());

	public static void insert(HttpServletRequest req, String userId)
			throws IOException {
		Credential credential = AuthUtil.newAuthorizationCodeFlow()
				.loadCredential(userId);

		{
			// Insert a contact
			LOG.fine("Inserting reference contact Item");
			Contact contact = new Contact();
			contact.setId("reference");
			contact.setDisplayName("FACE SEARCH");
			contact.setImageUrls(Lists.newArrayList(WebUtil.buildUrl(req,
					"/static/images/medref_reference_contact_2_640x360.png")));
			MirrorClient.insertContact(credential, contact);
		}

		{
			// Insert a contact
			LOG.fine("Inserting remember contact Item");
			Contact contact = new Contact();
			contact.setId("remember");
			contact.setDisplayName("PATIENT NOTE");
			contact.setImageUrls(Lists.newArrayList(WebUtil.buildUrl(req,
					"/static/images/medref_remember_contact_2_640x360.png")));
			MirrorClient.insertContact(credential, contact);
		}

		LOG.info("Bootstrapper inserted contact " + userId);
	}

}
