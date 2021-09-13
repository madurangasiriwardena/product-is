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
package org.wso2.identity.integration.common.clients.openid;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.identity.provider.openid.stub.OpenIDProviderServiceStub;
import org.wso2.carbon.identity.provider.openid.stub.dto.*;
import org.wso2.identity.integration.common.clients.AuthenticateStub;

public class OpenIDProviderServiceClient {

    private final String serviceName = "OpenIDProviderService";
    private OpenIDProviderServiceStub openidProviderServiceStub;

    public OpenIDProviderServiceClient(String backEndUrl, String sessionCookie) throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        openidProviderServiceStub = new OpenIDProviderServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, openidProviderServiceStub);
    }

    public OpenIDProviderServiceClient(String backEndUrl, String userName, String password) throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        openidProviderServiceStub = new OpenIDProviderServiceStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, openidProviderServiceStub);
    }

    public OpenIDClaimDTO[] getClaimValues(String openId, String profileId, OpenIDParameterDTO[] requredClaims)
            throws Exception {

        try {
            return openidProviderServiceStub.getClaimValues(openId, profileId, requredClaims);
        } finally {
            openidProviderServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public OpenIDUserProfileDTO[] getUserProfiles(String openId, OpenIDParameterDTO[] requredClaims) throws Exception {

        try {
            return openidProviderServiceStub.getUserProfiles(openId, requredClaims);
        } finally {
            openidProviderServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public OpenIDProviderInfoDTO getOpenIDProviderInfo(String userName, String openid) throws Exception {

        try {
            return openidProviderServiceStub.getOpenIDProviderInfo(userName, openid);
        } finally {
            openidProviderServiceStub._getServiceClient().cleanupTransport();
        }
    }
    
    public boolean isOpenIDUserApprovalBypassEnabled() throws RemoteException {

        try {
            return openidProviderServiceStub.isOpenIDUserApprovalBypassEnabled();
        } finally {
            openidProviderServiceStub._getServiceClient().cleanupTransport();
        }
    }
    
    public int getOpenIDSessionTimeout() throws RemoteException {

        try {
            return openidProviderServiceStub.getOpenIDSessionTimeout();
        } finally {
            openidProviderServiceStub._getServiceClient().cleanupTransport();
        }
    }
    
    public boolean authenticateWithOpenID(String openID, String password) throws Exception {

        try {
            return openidProviderServiceStub.authenticateWithOpenID(openID, password);
        } finally {
            openidProviderServiceStub._getServiceClient().cleanupTransport();
        }
    }
    
    public OpenIDUserRPDTO getOpenIDUserRPInfo(String openID, String rpUrl) throws Exception {

        try {
            return openidProviderServiceStub.getOpenIDUserRPInfo(openID, rpUrl);
        } finally {
            openidProviderServiceStub._getServiceClient().cleanupTransport();
        }
    }

    public OpenIDRememberMeDTO authenticateWithOpenIDRememberMe(String openID, String password,
            String ipaddress, String cookie) throws Exception {

        try {
            return openidProviderServiceStub.authenticateWithOpenIDRememberMe(openID, password, ipaddress, cookie);
        } finally {
            openidProviderServiceStub._getServiceClient().cleanupTransport();
        }
    }
    
    public void updateOpenIDUserRPInfo(OpenIDUserRPDTO rpdto) throws Exception {

        try {
            openidProviderServiceStub.updateOpenIDUserRPInfo(rpdto);
        } finally {
            openidProviderServiceStub._getServiceClient().cleanupTransport();
        }
    }
    
    public OpenIDUserRPDTO[] getOpenIDUserRPs(String openID) throws Exception {

        try {
            return openidProviderServiceStub.getOpenIDUserRPs(openID);
        } finally {
            openidProviderServiceStub._getServiceClient().cleanupTransport();
        }
    }
    
    public String getOpenIDAssociationResponse(OpenIDParameterDTO[] params) throws Exception {

        try {
            return openidProviderServiceStub.getOpenIDAssociationResponse(params);
        } finally {
            openidProviderServiceStub._getServiceClient().cleanupTransport();
        }
    }
    
    public OpenIDAuthResponseDTO getOpenIDAuthResponse(OpenIDAuthRequestDTO request) throws Exception {

        try {
            return openidProviderServiceStub.getOpenIDAuthResponse(request);
        } finally {
            openidProviderServiceStub._getServiceClient().cleanupTransport();
        }
    }
    
    public String verify(OpenIDParameterDTO[] params) throws Exception {

        try {
            return openidProviderServiceStub.verify(params);
        } finally {
            openidProviderServiceStub._getServiceClient().cleanupTransport();
        }
    }
}
