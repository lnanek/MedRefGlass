package com.neatocode.medrefglass.model;

import java.util.ArrayList;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class Subject implements Comparable<Subject> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
		
	@Basic
	private String createdByWearerId;
	
	@Basic
	private String name;
	
	@Basic
	private ArrayList<String> faces = new ArrayList<String>();
	
	@Basic
	private ArrayList<String> notes = new ArrayList<String>();
	
	@Transient
	private float match;

	public Subject() {
	}

	public String getName() {
		return name;
	}

	public void setName(String aName) {
		this.name = aName;
	}

	public ArrayList<String> getFaces() {
		return faces;
	}

	public void setFaces(ArrayList<String> faces) {
		this.faces = faces;
	}

	public ArrayList<String> getNotes() {
		return notes;
	}

	public void setNotes(ArrayList<String> notes) {
		this.notes = notes;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public float getMatch() {
		return match;
	}

	public void setMatch(float match) {
		this.match = match;
	}

	public String getCreatedByWearerId() {
		return createdByWearerId;
	}

	public void setCreatedByWearerId(String createdByWearerId) {
		this.createdByWearerId = createdByWearerId;
	}

	@Override
	public int compareTo(Subject other) {
		if ( null == other ) {
			return -1;
		}
		
		return Float.compare(match, other.match);
	}
}