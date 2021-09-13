/**
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.identity.integration.common.clients.entitlement;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceCallbackHandler;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceException;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceIdentityException;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceStub;
import org.wso2.carbon.identity.entitlement.stub.dto.AttributeDTO;
import org.wso2.carbon.identity.entitlement.stub.dto.EntitledResultSetDTO;
import org.wso2.identity.integration.common.clients.AuthenticateStub;

import java.rmi.RemoteException;

public class EntitlementServiceClient {
    private static final Log log = LogFactory.getLog(EntitlementServiceClient.class);

    private final String serviceName = "EntitlementService";
    private EntitlementServiceStub entitlementServiceStub;
    private String endPoint;

    public EntitlementServiceClient(String backEndUrl, String sessionCookie) throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        entitlementServiceStub = new EntitlementServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, entitlementServiceStub);
    }

    public EntitlementServiceClient(String backEndUrl, String userName, String password) throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        entitlementServiceStub = new EntitlementServiceStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, entitlementServiceStub);
    }

    public EntitledResultSetDTO getEntitledAttributes(String subjectName, String resourceName, String subjectId,
                                                      String action, boolean enableChildSearch)
            throws RemoteException, EntitlementServiceIdentityException {

        try {
            return entitlementServiceStub.getEntitledAttributes(subjectName, resourceName, subjectId, action,
                    enableChildSearch);
        } finally {
            entitlementServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public void startgetEntitledAttributes(String subjectName, String resourceName, String subjectId,
                                           String action, boolean enableChildSearch,
                                           EntitlementServiceCallbackHandler callback)
            throws RemoteException {

        try {
            entitlementServiceStub.startgetEntitledAttributes(subjectName, resourceName, subjectId, action,
                    enableChildSearch, callback);
        } finally {
            entitlementServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public String xACMLAuthzDecisionQuery(String request) throws EntitlementServiceException, RemoteException {

        try {
            return entitlementServiceStub.xACMLAuthzDecisionQuery(request);
        } finally {
            entitlementServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public void startxACMLAuthzDecisionQuery(String request, EntitlementServiceCallbackHandler callback)
            throws RemoteException {

        try {
            entitlementServiceStub.startxACMLAuthzDecisionQuery(request, callback);
        } finally {
            entitlementServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public EntitledResultSetDTO getAllEntitlements(String identifier, AttributeDTO[] givenAttributes)
            throws RemoteException, EntitlementServiceIdentityException {

        try {
            return entitlementServiceStub.getAllEntitlements(identifier, givenAttributes);
        } finally {
            entitlementServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public void startgetAllEntitlements(String identifier, AttributeDTO[] givenAttributes,
                                        EntitlementServiceCallbackHandler callback) throws RemoteException {

        try {
            entitlementServiceStub.startgetAllEntitlements(identifier, givenAttributes, callback);
        } finally {
            entitlementServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public String getDecision(String request) throws EntitlementServiceException, RemoteException {

        try {
            return entitlementServiceStub.getDecision(request);
        } finally {
            entitlementServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public void startgetDecision(String request, EntitlementServiceCallbackHandler callback) throws RemoteException {

        try {
            entitlementServiceStub.startgetDecision(request, callback);
        } finally {
            entitlementServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public String getDecisionByAttributes(String subject, String resource, String action, String[] environment)
            throws EntitlementServiceException, RemoteException {

        try {
            return entitlementServiceStub.getDecisionByAttributes(subject, resource, action, environment);
        } finally {
            entitlementServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public void startgetDecisionByAttributes(String subject, String resource, String action, String[] environment,
                                             EntitlementServiceCallbackHandler callback) throws RemoteException {

        try {
            entitlementServiceStub.startgetDecisionByAttributes(subject, resource, action, environment, callback);
        } finally {
            entitlementServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public boolean getBooleanDecision(String subject, String resource, String action)
            throws EntitlementServiceException, RemoteException {

        try {
            return entitlementServiceStub.getBooleanDecision(subject, resource, action);
        } finally {
            entitlementServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public void startgetBooleanDecision(String subject, String resource, String action,
                                        EntitlementServiceCallbackHandler callback) throws RemoteException {

        try {
            entitlementServiceStub.startgetBooleanDecision(subject, resource, action, callback);
        } finally {
            entitlementServiceStub._getServiceClient().cleanupTransport();
        }
    }
}
