/**
 * 
 */
package org.athento.nuxeo.workers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.work.AbstractWork;

/**
 * @author athento
 *
 */
public class SetACEWorker extends AbstractWork {

	/**
	 * For version 1.0
	 */
	private static final long serialVersionUID = 8077878313385599097L;
	public SetACEWorker(String id) {
		super(id);
	}
	
	public SetACEWorker(DocumentRef _ref, ACP _acp, boolean _overwrite) {
		docRef = _ref;
		acp =_acp;
	}
	/* (non-Javadoc)
	 * @see org.nuxeo.ecm.core.work.api.Work#getTitle()
	 */
	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return getCategory();
	}
	@Override
	public String getCategory() {
		return CATEGORY;
	}
	/* (non-Javadoc)
	 * @see org.nuxeo.ecm.core.work.AbstractWork#work()
	 */
	@Override
	public void work() throws Exception {
		if (_log.isInfoEnabled()) {
			_log.info("=== INITIALIZING Work for documentRef (" + docRef + ") and ACP (" + acp + ")");
		}
		initSession();
		setProgress(new Progress(0));
		try {
			setStatus("Setting ACL");
			session.setACP(docRef, acp, overwrite);
			setStatus("ACL set");
		} finally {
			commitOrRollbackTransaction();
			startTransaction();
			setProgress(new Progress(100));
		}
		if (_log.isInfoEnabled()) {
			_log.info("=== ENDING Work for documentRef (" + docRef + ") and ACP (" + acp + ")");
		}
	}
	private ACP acp;
	private DocumentRef docRef;
	private boolean overwrite;

	private static final String CATEGORY = "SetACEWorker";
	private static final Log _log = LogFactory.getLog(SetACEWorker.class);


}
