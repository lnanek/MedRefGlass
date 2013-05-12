package com.google.glassware;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.mirror.Mirror;
import com.google.api.services.mirror.model.Attachment;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class AttachmentDownload {
	
	private static final Logger LOG = Logger.getLogger(AttachmentDownload.class
			.getSimpleName());

  /**
   * Print an attachment's metadata.
   * 
   * @param service Authorized Mirror service.
   * @param itemId ID of the timeline item the attachment belongs to.
   * @param attachmentId ID of the attachment to print metadata for.
   */
  public static void printAttachmentMetadata(Mirror service, String itemId, String attachmentId) {
    try {
      Attachment attachment = service.timeline().attachments().get(itemId, attachmentId).execute();

      LOG.info("Attachment content type: " + attachment.getContentType());
      LOG.info("Attachment content URL: " + attachment.getContentUrl());
    } catch (IOException e) {
    	LOG.info("An error occured: " + e);
    }
  }

  /**
   * Download a timeline items's attachment.
   * 
   * @param service Authorized Mirror service.
   * @param itemId ID of the timeline item to download the attachment for.
   * @param attachment Attachment to download content for.
   * @return The attachment content on success, {@code null} otherwise.
   */
  public static InputStream downloadAttachment(Mirror service, Attachment attachment) {
    try {
      HttpResponse resp =
          service.getRequestFactory().buildGetRequest(new GenericUrl(attachment.getContentUrl()))
              .execute();
      return resp.getContent();
    } catch (IOException e) {
      // An error occurred.
      e.printStackTrace();
      return null;
    }
  }

}