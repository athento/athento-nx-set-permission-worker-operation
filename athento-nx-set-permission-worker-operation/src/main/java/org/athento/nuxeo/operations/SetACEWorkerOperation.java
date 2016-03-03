/**
 * 
 */
package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.workers.SetACEWorker;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.impl.ACLImpl;
import org.nuxeo.ecm.core.api.security.impl.ACPImpl;
import org.nuxeo.ecm.core.work.api.Work.State;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.runtime.api.Framework;


/**
 * @author athento
 *
 */

@Operation(id = SetACEWorkerOperation.ID, category = "Athento", label = "Set Document ACE (as worker)", description = "Launch a worker to Set Document ACE")
public class SetACEWorkerOperation {

	public static final String ID = "Athento.SetACE_as_worker";

	@Context
	protected CoreSession session;

	@Param(name = "user")
	protected String user;

	@Param(name = "permission")
	String permission;

	@Param(name = "acl", required = false, values = ACL.LOCAL_ACL)
	String aclName = ACL.LOCAL_ACL;

	@Param(name = "grant", required = false, values = "true")
	boolean grant = true;

	@Param(name = "overwrite", required = false, values = "true")
	boolean overwrite = true;

	@OperationMethod(collector = DocumentModelCollector.class)
	public DocumentModel run(DocumentModel doc) throws Exception {
		setACE(doc.getRef());
		return session.getDocument(doc.getRef());
	}

	@OperationMethod(collector = DocumentModelCollector.class)
	public DocumentModel run(DocumentRef doc) throws Exception {
		setACE(doc);
		return session.getDocument(doc);
	}

	protected void setACE(DocumentRef ref) throws ClientException {
		ACPImpl acp = new ACPImpl();
		ACLImpl acl = new ACLImpl(aclName);
		acp.addACL(acl);
		ACE ace = new ACE(user, permission, grant);
		acl.add(ace);
		launchWorker(ref, acp);
	}
	
	private void launchWorker (DocumentRef ref, ACP acp) {
		SetACEWorker work = new SetACEWorker(ref, acp, overwrite);
		WorkManager workManager = Framework.getLocalService(WorkManager.class);
		workManager.schedule(work, WorkManager.Scheduling.IF_NOT_RUNNING_OR_SCHEDULED);
		String workId = work.getId();
		State workState = workManager.getWorkState(workId);
		if (_log.isInfoEnabled()) {
			_log.info("Work [" + workId + "] queued in state [" + workState + "]");
		}
	}
	private static final Log _log = LogFactory.getLog(SetACEWorkerOperation.class);

}