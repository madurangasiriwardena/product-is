/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.identity.integration.common.clients.entitlement;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.stub.dto.PaginatedPolicySetDTO;
import org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;
import org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceStub;
import org.wso2.carbon.identity.entitlement.stub.dto.PaginatedStatusHolder;
import org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.stub.dto.PublisherDataHolder;
import org.wso2.identity.integration.common.clients.AuthenticateStub;
import org.xml.sax.SAXException;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;
import java.rmi.RemoteException;

public class EntitlementPolicyServiceClient {
    private static final Log log = LogFactory.getLog(EntitlementPolicyServiceClient.class);

    private final String serviceName = "EntitlementPolicyAdminService";
    private EntitlementPolicyAdminServiceStub entitlementPolicyAdminServiceStub;
    private String endPoint;

    public EntitlementPolicyServiceClient(String backEndUrl, String sessionCookie)
            throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        entitlementPolicyAdminServiceStub = new EntitlementPolicyAdminServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, entitlementPolicyAdminServiceStub);
    }

    public EntitlementPolicyServiceClient(String backEndUrl, String userName, String password)
            throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        entitlementPolicyAdminServiceStub = new EntitlementPolicyAdminServiceStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, entitlementPolicyAdminServiceStub);
    }

    public void addPolicies(File policyFile)
            throws IOException,
            ParserConfigurationException, TransformerException, SAXException,
            EntitlementPolicyAdminServiceEntitlementException {

        DataHandler policydh =
                new DataHandler(new FileDataSource(policyFile));
        String policy = convertXMLFileToString(policyFile);

        PolicyDTO policySetDTO = new PolicyDTO();
        policySetDTO.setPolicy(policy);
        policySetDTO.setActive(true);
        try {
            entitlementPolicyAdminServiceStub.addPolicy(policySetDTO);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public void removePolicy(String policyId)
            throws IOException, EntitlementPolicyAdminServiceEntitlementException {

        try {
            entitlementPolicyAdminServiceStub.removePolicy(policyId, false);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public void removePolicy(String policyId, boolean dePromote)
            throws IOException, EntitlementPolicyAdminServiceEntitlementException {

        try {
            entitlementPolicyAdminServiceStub.removePolicy(policyId, dePromote);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public void updatePolicy(PolicyDTO policyDTO) throws RemoteException,
            EntitlementPolicyAdminServiceEntitlementException {

        try {
            entitlementPolicyAdminServiceStub.updatePolicy(policyDTO);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public void removePolicies(String[] policies, boolean dePromote) throws RemoteException {
        try {
            entitlementPolicyAdminServiceStub.removePolicies(policies, dePromote);
        } catch (EntitlementPolicyAdminServiceEntitlementException e) {
            log.error(e);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public String[] getAllPolicyIds(String searchString)
            throws RemoteException, EntitlementPolicyAdminServiceEntitlementException {

        try {
            return entitlementPolicyAdminServiceStub.getAllPolicyIds(searchString);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public void addPolicy(PolicyDTO policyDTO)
            throws RemoteException, EntitlementPolicyAdminServiceEntitlementException {

        try {
            entitlementPolicyAdminServiceStub.addPolicy(policyDTO);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public void addPolicies(PolicyDTO[] policies) throws RemoteException {
        try {
            entitlementPolicyAdminServiceStub.addPolicies(policies);
        } catch (EntitlementPolicyAdminServiceEntitlementException e) {
            log.error(e);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public PolicyDTO getPolicy(String policyId, boolean isPDPPolicy)
            throws RemoteException, EntitlementPolicyAdminServiceEntitlementException {

        try {
            return entitlementPolicyAdminServiceStub.getPolicy(policyId, isPDPPolicy);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public String[] getPolicyVersions(String policyId)
            throws RemoteException, EntitlementPolicyAdminServiceEntitlementException {

        try {
            return entitlementPolicyAdminServiceStub.getPolicyVersions(policyId);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public PaginatedPolicySetDTO getAllPolicies(String policyTypeFilter, String policySearchString,
                                                int pageNumber, boolean isPDPPolicy)
            throws RemoteException, EntitlementPolicyAdminServiceEntitlementException {

        try {
            return entitlementPolicyAdminServiceStub.getAllPolicies(policyTypeFilter, policySearchString, pageNumber,
                    isPDPPolicy);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public PolicyDTO getPolicyByVersion(String policyId, String version)
            throws RemoteException, EntitlementPolicyAdminServiceEntitlementException {

        try {
            return entitlementPolicyAdminServiceStub.getPolicyByVersion(policyId, version);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public void importPolicyFromRegistry(String policyRegistryPath)
            throws RemoteException, EntitlementPolicyAdminServiceEntitlementException {

        try {
            entitlementPolicyAdminServiceStub.importPolicyFromRegistry(policyRegistryPath);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public PaginatedStatusHolder getStatusData(String about, String key, String type,
                                               String searchString, int pageNumber) throws RemoteException {

        PaginatedStatusHolder holder = null;
        try {
            holder = entitlementPolicyAdminServiceStub.getStatusData(about, key, type, searchString, pageNumber);
        } catch (EntitlementPolicyAdminServiceEntitlementException e) {
            log.error(e);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
        return holder;
    }

    public void addSubscriber(PublisherDataHolder holder) throws RemoteException {
        try {
            entitlementPolicyAdminServiceStub.addSubscriber(holder);
        } catch (EntitlementPolicyAdminServiceEntitlementException e) {
            log.error(e);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public void updateSubscriber(PublisherDataHolder holder) throws RemoteException {
        try {
            entitlementPolicyAdminServiceStub.updateSubscriber(holder);
        } catch (EntitlementPolicyAdminServiceEntitlementException e) {
            log.error(e);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public PublisherDataHolder getSubscriber(String subscribeId) throws RemoteException {
        PublisherDataHolder holder = null;
        try {
            holder = entitlementPolicyAdminServiceStub.getSubscriber(subscribeId);
        } catch (EntitlementPolicyAdminServiceEntitlementException e) {
            log.error(e);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
        return holder;
    }

    public String[] getSubscriberIds(String searchString) throws RemoteException {
        String[] ids = null;
        try {
            ids = entitlementPolicyAdminServiceStub.getSubscriberIds(searchString);
        } catch (EntitlementPolicyAdminServiceEntitlementException e) {
            log.error(e);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
        return ids;
    }

    public void deleteSubscriber(String subscriberId) throws RemoteException {
        try {
            entitlementPolicyAdminServiceStub.deleteSubscriber(subscriberId);
        } catch (EntitlementPolicyAdminServiceEntitlementException e) {
            log.error(e);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public void enableDisablePolicy(String policyId, boolean enable) throws RemoteException {
        try {
            entitlementPolicyAdminServiceStub.enableDisablePolicy(policyId, enable);
        } catch (EntitlementPolicyAdminServiceEntitlementException e) {
            log.error(e);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public void publish(String verificationCode) throws RemoteException {
        try {
            entitlementPolicyAdminServiceStub.publish(verificationCode);
        } catch (EntitlementPolicyAdminServiceEntitlementException e) {
            log.error(e);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public void publishToPDP(String[] policies, String action, boolean enabled, String version, int order)
            throws RemoteException {
        try {
            entitlementPolicyAdminServiceStub.publishToPDP(policies, action, version, enabled, order);
        } catch (EntitlementPolicyAdminServiceEntitlementException e) {
            log.error(e);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public void publishPolicies(String[] policies, String[] subscriberIds, String action, boolean enabled,
                                String version, int order)
            throws RemoteException {

        try {
            entitlementPolicyAdminServiceStub.publishPolicies(policies, subscriberIds, action, version, enabled, order);
        } catch (EntitlementPolicyAdminServiceEntitlementException e) {
            log.error(e);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public void orderPolicy(String policyId, int newOrder) throws RemoteException {
        try {
            entitlementPolicyAdminServiceStub.orderPolicy(policyId, newOrder);
        } catch (EntitlementPolicyAdminServiceEntitlementException e) {
            log.error(e);
        } finally {
            entitlementPolicyAdminServiceStub._getServiceClient().cleanupTransport();
        }
    }

    private String convertXMLFileToString(File fileName)
            throws IOException, ParserConfigurationException, SAXException, TransformerException {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        InputStream inputStream = new FileInputStream(fileName);
        org.w3c.dom.Document doc = documentBuilderFactory.newDocumentBuilder().parse(inputStream);
        StringWriter stw = new StringWriter();
        Transformer serializer = TransformerFactory.newInstance().newTransformer();
        serializer.transform(new DOMSource(doc), new StreamResult(stw));
        return stw.toString();
    }
}
