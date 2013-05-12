package com.google.glassware;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

public class SendServerRequest {
	
	private static final Logger LOG = Logger.getLogger(SendServerRequest.class
			.getSimpleName());

	public static InputStream streamGet(String url, final String token) {
		LOG.info("streamGet");

		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("GET");
			//connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", "OAuth " + token);

			connection.setDoInput(true);
			connection.setDoOutput(true);

			return connection.getInputStream();
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "An error occurred getting: " + url, e);
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
	
	public static JSONObject sendJSONGet(String url, final String token) {
		LOG.info("sendJSONGet");

		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", "OAuth " + token);

			connection.setDoInput(true);
			connection.setDoOutput(true);

			return parseResponse(connection);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "An error occurred getting: " + url, e);
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	static JSONObject parseResponse(HttpURLConnection connection)
			throws IOException, JSONException {
		LOG.info("parseResponse");
		LOG.info("Response code: " + connection.getResponseCode());
		LOG.info("Response message: "
				+ connection.getResponseMessage());

		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		StringBuilder response = new StringBuilder();

		while ((line = reader.readLine()) != null) {
			response.append(line).append('\r');
		}
		reader.close();

		LOG.info("Response: " + response.toString());

		return new JSONObject(response.toString());
	}
}