/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.common.clients.workflow.mgt;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.workflow.impl.stub.WorkflowImplAdminServiceStub;
import org.wso2.carbon.identity.workflow.impl.stub.WorkflowImplAdminServiceWorkflowImplException;
import org.wso2.carbon.identity.workflow.impl.stub.bean.BPSProfile;
import org.wso2.carbon.identity.workflow.mgt.stub.bean.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.stub.bean.WorkflowRequestAssociation;
import org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceStub;
import org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.Association;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.Template;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowEvent;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowImpl;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowWizard;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.bean.ParametersMetaData;

import java.rmi.RemoteException;
import java.util.List;

public class WorkflowAdminClient {

    private WorkflowAdminServiceStub stub;
    private WorkflowImplAdminServiceStub stubImpl;
    private static final Log log = LogFactory.getLog(WorkflowAdminClient.class);

    /**
     * @param cookie
     * @param backendServerURL
     * @param configCtx
     * @throws AxisFault
     */
    public WorkflowAdminClient(String cookie, String backendServerURL,
                               ConfigurationContext configCtx) throws AxisFault {

        String serviceURL = backendServerURL + "WorkflowAdminService";
        stub = new WorkflowAdminServiceStub(configCtx, serviceURL);
        serviceURL = backendServerURL + "WorkflowImplAdminService";
        stubImpl = new WorkflowImplAdminServiceStub(configCtx, serviceURL);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        ServiceClient clientImpl = stubImpl._getServiceClient();
        Options optionImpl = clientImpl.getOptions();
        optionImpl.setManageSession(true);
        optionImpl.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public WorkflowEvent[] listWorkflowEvents() throws RemoteException {

        try {
            WorkflowEvent[] workflowEvents = stub.listWorkflowEvents();
            if (workflowEvents == null) {
                workflowEvents = new WorkflowEvent[0];
            }
            return workflowEvents;
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    public Template[] listTemplates() throws RemoteException, WorkflowAdminServiceWorkflowException {

        try {
            Template[] templates = stub.listTemplates();
            if (templates == null) {
                templates = new Template[0];
            }
            return templates;
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    public Template getTemplate(String templateId) throws RemoteException, WorkflowAdminServiceWorkflowException {

        try {
            Template templateDTO = stub.getTemplate(templateId);
            if (templateDTO != null) {
                if (templateDTO.getParametersMetaData() == null) {
                    templateDTO.setParametersMetaData(new ParametersMetaData());
                }
            }
            return templateDTO;
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    public WorkflowImpl getWorkflowImpl(String templateId, String implId)
            throws RemoteException, WorkflowAdminServiceWorkflowException {

        try {
            WorkflowImpl workflowImpl = stub.getWorkflowImpl(templateId, implId);
            if (workflowImpl != null) {
                if (workflowImpl.getParametersMetaData() == null) {
                    workflowImpl.setParametersMetaData(new ParametersMetaData());
                }
            }
            return workflowImpl;
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Add new workflow
     *
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public void addWorkflow(WorkflowWizard workflow) throws RemoteException, WorkflowAdminServiceWorkflowException {

        try {
            stub.addWorkflow(workflow);
        } finally {
            stub._getServiceClient().cleanupTransport();
        }

    }

    /**
     * Add new BPS profile
     *
     * @param bpsProfileDTO
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public void addBPSProfile(BPSProfile bpsProfileDTO) throws RemoteException,
            WorkflowAdminServiceWorkflowException, WorkflowImplAdminServiceWorkflowImplException {

        try {
            stubImpl.addBPSProfile(bpsProfileDTO);
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }


    /**
     * Retrieve BPS Profiles
     *
     * @return
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public BPSProfile[] listBPSProfiles()
            throws RemoteException, WorkflowAdminServiceWorkflowException,
            WorkflowImplAdminServiceWorkflowImplException {

        try {
            BPSProfile[] bpsProfiles = stubImpl.listBPSProfiles();
            if (bpsProfiles == null) {
                bpsProfiles = new BPSProfile[0];
            }
            return bpsProfiles;
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Get BPS Profile detail for given profile name
     *
     * @param profileName
     * @return
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public BPSProfile getBPSProfiles(String profileName) throws RemoteException, WorkflowAdminServiceWorkflowException,
            WorkflowImplAdminServiceWorkflowImplException {

        try {
            return stubImpl.getBPSProfile(profileName);
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Update BPS Profile
     *
     * @param bpsProfileDTO
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public void updateBPSProfile(BPSProfile bpsProfileDTO) throws RemoteException,
            WorkflowAdminServiceWorkflowException, WorkflowImplAdminServiceWorkflowImplException {

        try {
            stubImpl.updateBPSProfile(bpsProfileDTO);
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    public void deleteBPSProfile(String profileName) throws RemoteException, WorkflowAdminServiceWorkflowException,
            WorkflowImplAdminServiceWorkflowImplException {

        try {
            stubImpl.removeBPSProfile(profileName);
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Retrieve Workflows
     *
     * @return
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public WorkflowWizard[] listWorkflows() throws RemoteException, WorkflowAdminServiceWorkflowException {

        try {
            WorkflowWizard[] workflows = stub.listWorkflows();
            if (workflows == null) {
                workflows = new WorkflowWizard[0];
            }
            return workflows;
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    public void deleteWorkflow(String workflowId) throws RemoteException, WorkflowAdminServiceWorkflowException {

        try {
            stub.removeWorkflow(workflowId);
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    public Association[] listAssociationsForWorkflow(String workflowId) throws RemoteException,
            WorkflowAdminServiceWorkflowException {

        try {
            Association[] associationsForWorkflow = stub.listAssociations(workflowId);
            if (associationsForWorkflow == null) {
                associationsForWorkflow = new Association[0];
            }
            return associationsForWorkflow;
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    public Association[] listAllAssociations() throws RemoteException, WorkflowAdminServiceWorkflowException {

        try {
            Association[] associations = stub.listAllAssociations();
            if (associations == null) {
                associations = new Association[0];
            }
            return associations;
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    public void deleteAssociation(String associationId) throws RemoteException, WorkflowAdminServiceWorkflowException {
        stub.removeAssociation(associationId);
    }

    public void addAssociation(String workflowId, String associationName, String eventId, String condition)
            throws RemoteException, WorkflowAdminServiceWorkflowException {

        try {
            stub.addAssociation(associationName, workflowId, eventId, condition);
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Enable association to allow to execute
     *
     * @param associationId
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public void enableAssociation(String associationId) throws RemoteException, WorkflowAdminServiceWorkflowException {

        try {
            stub.changeAssociationState(associationId, true);
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    /**
     * Disable association to avoid with execution of the workflows
     *
     * @param associationId
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public void disableAssociation(String associationId) throws RemoteException, WorkflowAdminServiceWorkflowException {

        try {
            stub.changeAssociationState(associationId, false);
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    public WorkflowEvent getEvent(String id) throws RemoteException {

        try {
            return stub.getEvent(id);
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    public void deleteRequest(String requestId) throws WorkflowAdminServiceWorkflowException, RemoteException {

        try {
            stub.deleteWorkflowRequest(requestId);
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

    public WorkflowRequestAssociation[] getWorkflowsOfRequest(String requestId) throws
            WorkflowAdminServiceWorkflowException, RemoteException {

        try {
            return stub.getWorkflowsOfRequest(requestId);
        } finally {
            stub._getServiceClient().cleanupTransport();
        }
    }

}
