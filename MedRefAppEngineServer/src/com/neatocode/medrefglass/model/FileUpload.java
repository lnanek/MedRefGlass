package com.neatocode.medrefglass.model;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Blob;

@Entity
public class FileUpload {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
		
	@Basic
    private Long subjectId;
	
	@Basic
	private BlobKey blob;
	
	@Basic
    private int blobSize;

	public FileUpload() {
	}

	public Long getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}

	public BlobKey getBlob() {
		return blob;
	}

	public void setBlob(BlobKey blob) {
		this.blob = blob;
	}

	public int getBlobSize() {
		return blobSize;
	}

	public void setBlobSize(int blobSize) {
		this.blobSize = blobSize;
	}
	   
}