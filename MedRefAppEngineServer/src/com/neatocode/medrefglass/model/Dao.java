package com.neatocode.medrefglass.model;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.apache.commons.lang.ArrayUtils;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.neatocode.medrefglass.model.cards.SearchResultsCard;

public enum Dao {

	INSTANCE;

	private static final Logger LOG = Logger.getLogger(SearchResultsCard.class
			.getSimpleName());

	public Subject createSubject(final String aUserId, final String aSubjectName) {
		LOG.info("createSubject: aSubjectName = " + aSubjectName);

		final EntityManager em = EMFService.get().createEntityManager();
		final EntityTransaction t = em.getTransaction();
		t.begin();
		try {
			Subject s = getSubjectBy(em, aSubjectName, aUserId);
			// TODO if have a current subject with no name, just set name on
			// that. allows user to actions in other order without
			// an unknown named subject being created
			if (null == s) {
				LOG.info("no existing subject found, creating anew");
				s = new Subject();
				s.setCreatedByWearerId(aUserId);
				s.setName(aSubjectName.toUpperCase());
			}
			em.persist(s);
			t.commit();

			t.begin();
			Wearer w = getWearerBy(em, aUserId);
			if (null == w) {
				w = new Wearer();
				w.setUserId(aUserId);
			}
			w.setCurrentSubjectId(s.getId());
			em.persist(w);

			em.flush();
			if (null != t) {
				t.commit();
			}
			return s;
		} finally {
			if (null != t && t.isActive()) {
				t.rollback();
			}
			em.close();
		}
	}

	public byte[] readImageData(BlobKey blobKey, long blobSize) {
		BlobstoreService blobStoreService = BlobstoreServiceFactory
				.getBlobstoreService();
		byte[] allTheBytes = new byte[0];
		long amountLeftToRead = blobSize;
		long startIndex = 0;
		while (amountLeftToRead > 0) {
			long amountToReadNow = Math.min(
					BlobstoreService.MAX_BLOB_FETCH_SIZE - 1, amountLeftToRead);

			byte[] chunkOfBytes = blobStoreService.fetchData(blobKey,
					startIndex, startIndex + amountToReadNow - 1);

			allTheBytes = ArrayUtils.addAll(allTheBytes, chunkOfBytes);

			amountLeftToRead -= amountToReadNow;
			startIndex += amountToReadNow;
		}

		return allTheBytes;
	}

	public BlobKey writeImageData(byte[] bytes) throws IOException {
		FileService fileService = FileServiceFactory.getFileService();

		AppEngineFile file = fileService.createNewBlobFile("image/jpeg");
		boolean lock = true;
		FileWriteChannel writeChannel = fileService
				.openWriteChannel(file, lock);

		writeChannel.write(ByteBuffer.wrap(bytes));
		writeChannel.closeFinally();

		return fileService.getBlobKey(file);
	}

	public boolean saveFile(final FileUpload aFile) {
		EntityManager em = EMFService.get().createEntityManager();
		EntityTransaction t = em.getTransaction();
		try {
			if (null != t)
				t.begin();

			em.persist(aFile);
			em.flush();

			if (null != t)
				t.commit();
			System.out.println("Saved File Upload");
			return true;
		} finally {
			if (null != t && t.isActive()) {
				System.out.println("Rolled Back File Upload");
				t.rollback();
			}
			em.close();
		}
	}

	public Subject addNote(final String aUserId, final String aNote) {
		final EntityManager em = EMFService.get().createEntityManager();
		final EntityTransaction t = em.getTransaction();
		t.begin();
		try {
			Wearer w = getWearerBy(em, aUserId);
			t.commit();
			t.begin();

			Long subjectId = w.getCurrentSubjectId();
			Subject s = null == subjectId ? null : em.find(Subject.class,
					subjectId);
			if (null == subjectId) {
				s = new Subject();
				s.setCreatedByWearerId(w.getUserId());
				// TODO just leave unnamed until they name it?
				s.setName("UNKNOWN");
			}
			s.getNotes().add(aNote);
			em.persist(s);
			t.commit();
			t.begin();

			w.setCurrentSubjectId(s.getId());
			em.flush();
			if (null != t) {
				t.commit();
			}
			return s;
		} finally {
			if (null != t && t.isActive()) {
				t.rollback();
			}
			em.close();
		}
	}

	public Subject addFace(final String aUserId, final String aFace) {
		final EntityManager em = EMFService.get().createEntityManager();
		final EntityTransaction t = em.getTransaction();
		t.begin();
		try {
			Wearer w = getWearerBy(em, aUserId);
			if (null == w) {
				w = new Wearer();
				w.setUserId(aUserId);
				em.persist(w);
				t.commit();
				t.begin();
			}

			Long subjectId = w.getCurrentSubjectId();
			Subject s = null == subjectId ? null : em.find(Subject.class,
					subjectId);
			if (null == subjectId) {
				s = new Subject();
				s.setCreatedByWearerId(w.getUserId());
				// TODO just leave unnamed until they name it?
				s.setName("UNKNOWN");
			}
			s.getFaces().add(aFace);
			em.persist(s);
			t.commit();
			t.begin();

			w.setCurrentSubjectId(s.getId());
			em.flush();
			if (null != t) {
				t.commit();
			}
			return s;
		} finally {
			if (null != t && t.isActive()) {
				t.rollback();
			}
			em.close();
		}
	}

	public Wearer getWearerBy(final EntityManager em, final String aUserId) {
		Query q = em
				.createQuery("select w from Wearer w where w.userId = :aUserId");
		q.setParameter("aUserId", aUserId);
		List<Wearer> ws = q.getResultList();
		if (null == ws || ws.isEmpty()) {
			return null;
		}
		return ws.get(0);
	}

	public Subject getCurrentSubject(final String aUserId) {
		final EntityManager em = EMFService.get().createEntityManager();
		try {
			final Wearer w = getWearerBy(em, aUserId);
			if (null == w.getCurrentSubjectId()) {
				return null;
			}

			return em.find(Subject.class, w.getCurrentSubjectId());
		} finally {
			em.close();
		}
	}

	public List<FileUpload> getFilesBy(EntityManager em, final Long aSubjectId) {
			if (null == aSubjectId) {
				return null;
			}
			
			Query q = em
					.createQuery("select f from FileUpload f where f.subjectId = :aSubjectId");
			q.setParameter("aSubjectId", aSubjectId);
			List<FileUpload> ws = q.getResultList();
			return ws;
	}

	public List<Subject> listSubjects(final String aUserId) {
		final EntityManager em = EMFService.get().createEntityManager();
		final EntityTransaction t = em.getTransaction();
		try {
			if (null != t)
				t.begin();

			Query q = em.createQuery("select s from Subject s " + 
					" where s.createdByWearerId = :aUserId");
			q.setParameter("aUserId", aUserId);
			List<Subject> subjects = q.getResultList();

			em.flush();
			if (null != t) {
				t.commit();
			}
			return subjects;
		} finally {
			if (null != t && t.isActive()) {
				t.rollback();
			}
			em.close();
		}

	}
	
	public Subject getSubjectBy(final EntityManager em, 
			final String aName, final String aUserId) {
		if (null == aName || "".equals(aName.trim())) {
			return null;
		}
		Query q = em
				.createQuery("select s from Subject s " + 
						" where TRIM(UPPER(s.name)) = :aName " + 
						" AND s.createdByWearerId = :aUserId ");
		q.setParameter("aName", aName.toUpperCase().trim());
		q.setParameter("aUserId", aUserId);
		List<Subject> ws = q.getResultList();
		if (null == ws || ws.isEmpty()) {
			return null;
		}
		return ws.get(0);
	}

}
