package com.neatocode.medrefglass.model;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Wearer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
		
	@Basic
    private Long currentSubjectId;
	
	@Basic
	private String userId;

	public Wearer() {
	}

	public Long getCurrentSubjectId() {
		return currentSubjectId;
	}

	public void setCurrentSubjectId(Long currentSubjectId) {
		this.currentSubjectId = currentSubjectId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	   
}