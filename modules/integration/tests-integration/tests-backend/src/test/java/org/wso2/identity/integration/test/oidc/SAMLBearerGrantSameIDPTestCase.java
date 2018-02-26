/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.oidc;

import com.nimbusds.jwt.SignedJWT;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.identity.integration.common.clients.PropertiesAdminServiceClient;
import org.wso2.identity.integration.test.utils.IdentityConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.rmi.RemoteException;

public class SAMLBearerGrantSameIDPTestCase extends AbstractSAMLBearerGrantTestCase {

    private PropertiesAdminServiceClient propertiesAdminServiceClient;

    private static final String OPENID_SCOPE_RESOURCE = "/_system/config/oidc";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.initTest();
        super.createServiceClients(PORT_OFFSET_0, sessionCookie, new IdentityConstants.ServiceClientType[]{
                IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT,
                IdentityConstants.ServiceClientType.REMOTE_USERSTORE_MANAGER,
                IdentityConstants.ServiceClientType.OAUTH_CONFIG,
                IdentityConstants.ServiceClientType.SAML_SSO_CONFIG});

        propertiesAdminServiceClient = new PropertiesAdminServiceClient(backendURL, sessionCookie);

        oidcApplication =
                createOIDCApplication(PORT_OFFSET_0, playgroundAppOneAppName, playgroundAppOneAppCallBackUri);
        createSAMLApplication(PORT_OFFSET_0, APPLICATION_NAME, new String[]{OAuth2Constant.ACCESS_TOKEN_ENDPOINT}, new
                String[]{OAuth2Constant.ACCESS_TOKEN_ENDPOINT}, TRAVELOCITY_ARTIFACT);
        super.addUser(PORT_OFFSET_0, username, password, null, getUserClaims(), null, true);
        deployTomcatServer(TRAVELOCITY_ARTIFACT);
        addSAMLScopeToRegistry();
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        tomcatServer.stop();
        tomcatServer.destroy();

        super.deleteServiceProvider(PORT_OFFSET_0, APPLICATION_NAME);
        super.deleteServiceProvider(PORT_OFFSET_0, playgroundAppOneAppName);
        super.removeUser(PORT_OFFSET_0, username);
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO login", groups = "wso2.is")
    public void testSAMLBearerGrant() {

        try {
            String samlResponse = sendSAMLRequest(TRAVELOCITY_ARTIFACT, SAML_SSO_URL, COMMON_AUTH_URL);
            String assertion = getAssertion(samlResponse);
            System.out.println("===========================================================");
            System.out.println(assertion);

            //Sending token request
            JSONObject obj = sendSAMLBearerGrantRequest(assertion, OAuth2Constant.ACCESS_TOKEN_ENDPOINT,
                    oidcApplication.getClientId(), oidcApplication.getClientSecret());
            System.out.println("============================================================");
            System.out.println(obj);
            Object id_token = obj.get(ID_TOKEN);
            Object refresh_token = obj.get(REFRESH_TOKEN);

            SignedJWT signedJWT = SignedJWT.parse(id_token.toString());
            Assert.assertEquals(signedJWT.getJWTClaimsSet().getAllClaims().get(EMAIL_CLAIM_URI), emailClaim);
            Assert.assertEquals(signedJWT.getJWTClaimsSet().getAllClaims().get(LAST_NAME_CLAIM_URI), lastname);
            Assert.assertEquals(signedJWT.getJWTClaimsSet().getAllClaims().get(FIRST_NAME_CLAIM_URI), firstname);

            obj = sendRefreshGrantRequest((String) refresh_token, OAuth2Constant.ACCESS_TOKEN_ENDPOINT,
                    oidcApplication.getClientId(), oidcApplication.getClientSecret());
            id_token = obj.get(ID_TOKEN);

            signedJWT = SignedJWT.parse(id_token.toString());
            Assert.assertEquals(signedJWT.getJWTClaimsSet().getAllClaims().get(EMAIL_CLAIM_URI), emailClaim);
            Assert.assertEquals(signedJWT.getJWTClaimsSet().getAllClaims().get(LAST_NAME_CLAIM_URI), lastname);
            Assert.assertEquals(signedJWT.getJWTClaimsSet().getAllClaims().get(FIRST_NAME_CLAIM_URI), firstname);

        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed", e);
        }
    }

    protected void addSAMLScopeToRegistry() throws PropertiesAdminServiceRegistryExceptionException, RemoteException {

        String samlScope = EMAIL_CLAIM_URI + "," + FIRST_NAME_CLAIM_URI + "," + LAST_NAME_CLAIM_URI;
        propertiesAdminServiceClient.addProperty(OPENID_SCOPE_RESOURCE, "saml", samlScope);
    }
}
