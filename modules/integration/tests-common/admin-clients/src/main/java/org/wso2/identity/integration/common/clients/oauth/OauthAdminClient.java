/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.identity.integration.common.clients.oauth;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceIdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceStub;
import org.wso2.identity.integration.common.clients.AuthenticateStub;

public class OauthAdminClient {

	OAuthAdminServiceStub oauthAdminStub;
	private final String serviceName = "OAuthAdminService";
	
	public OauthAdminClient(String backendURL, String sessionCookie) throws AxisFault {

        String endPoint = backendURL + serviceName;
        oauthAdminStub = new OAuthAdminServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, oauthAdminStub);
	}
	
    public OauthAdminClient(String backendURL, String userName, String password)
            throws AxisFault {

        String endPoint = backendURL + serviceName;
        oauthAdminStub = new OAuthAdminServiceStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, oauthAdminStub);        
    }
    
    public void registerOAuthApplicationData(OAuthConsumerAppDTO application)
            throws RemoteException, OAuthAdminServiceIdentityOAuthAdminException {

        try {
            oauthAdminStub.registerOAuthApplicationData(application);
        } finally {
            oauthAdminStub._getServiceClient().cleanupTransport();
        }
    }
    
    public OAuthConsumerAppDTO[] getAllOAuthApplicationData()
            throws RemoteException, OAuthAdminServiceIdentityOAuthAdminException {

        try {
            return oauthAdminStub.getAllOAuthApplicationData();
        } finally {
            oauthAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public OAuthConsumerAppDTO getOAuthAppByConsumerKey(String consumerKey) throws Exception {

        try {
            return oauthAdminStub.getOAuthApplicationData(consumerKey);
        } finally {
            oauthAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public void updateConsumerApp(OAuthConsumerAppDTO updatedConsumerApp) throws Exception {

        try {
            oauthAdminStub.updateConsumerApplication(updatedConsumerApp);
        } finally {
            oauthAdminStub._getServiceClient().cleanupTransport();
        }
    }
    
    public void removeOAuthApplicationData(String consumerKey)
            throws RemoteException, OAuthAdminServiceIdentityOAuthAdminException{

        try {
            oauthAdminStub.removeOAuthApplicationData(consumerKey);
        } finally {
            oauthAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public String getOauthApplicationState(String appName)
            throws Exception {
        OAuthConsumerAppDTO authConsumerAppDTO;
        try {
            authConsumerAppDTO = oauthAdminStub.getOAuthApplicationDataByAppName(appName);
        } finally {
            oauthAdminStub._getServiceClient().cleanupTransport();
        }
        try {
            return oauthAdminStub.getOauthApplicationState(authConsumerAppDTO.getOauthConsumerKey());
        } finally {
            oauthAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public void updateConsumerAppState(String appName, String newState)
            throws Exception {

        OAuthConsumerAppDTO authConsumerAppDTO;
        try {
            authConsumerAppDTO = oauthAdminStub.getOAuthApplicationDataByAppName(appName);
        } finally {
            oauthAdminStub._getServiceClient().cleanupTransport();
        }
        try {
            oauthAdminStub.updateConsumerAppState(authConsumerAppDTO.getOauthConsumerKey(), newState);
        } finally {
            oauthAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public void updateOauthSecretKey(String appName)
            throws Exception {
        OAuthConsumerAppDTO authConsumerAppDTO;
        try {
            authConsumerAppDTO = oauthAdminStub.getOAuthApplicationDataByAppName(appName);
        } finally {
            oauthAdminStub._getServiceClient().cleanupTransport();
        }
        try {
            oauthAdminStub.updateOauthSecretKey(authConsumerAppDTO.getOauthConsumerKey());
        } finally {
            oauthAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public OAuthConsumerAppDTO getOAuthAppByName(String applicationName) throws Exception {

        try {
            return oauthAdminStub.getOAuthApplicationDataByAppName(applicationName);
        } finally {
            oauthAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public void updateConsumerApplication(OAuthConsumerAppDTO application) throws Exception {

        try {
            oauthAdminStub.updateConsumerApplication(application);
        } finally {
            oauthAdminStub._getServiceClient().cleanupTransport();
        }
    }

    public void updateScope(String scope, String[] newClaims, String[] deleteClaims) throws Exception {

        try {
            oauthAdminStub.updateScope(scope, newClaims, deleteClaims);
        } finally {
            oauthAdminStub._getServiceClient().cleanupTransport();
        }
    }
}
