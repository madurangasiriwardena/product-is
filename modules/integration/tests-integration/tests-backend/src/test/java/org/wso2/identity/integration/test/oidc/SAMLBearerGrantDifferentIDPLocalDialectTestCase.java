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
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.IdentityConstants;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SAMLBearerGrantDifferentIDPLocalDialectTestCase extends AbstractSAMLBearerGrantTestCase {

    private File identityXML;
    private ServerConfigurationManager serverConfigurationManager;

    protected static final String TRAVELOCITY_ARTIFACT = "travelocity.com-samlbearer";
    protected static final String SAML_SSO_URL = "https://localhost:9854/samlsso";
    protected static final String COMMON_AUTH_URL = "https://localhost:9854/commonauth";
    private static final String IDP_ENTITY_ID = "idp.wso2.com";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.initTest();

        Map<String, String> startupParameters = new HashMap<String, String>();
        startupParameters.put("-DportOffset", String.valueOf(PORT_OFFSET_1 + CommonConstants.IS_DEFAULT_OFFSET));
        AutomationContext context = new AutomationContext("IDENTITY", "identity002", TestUserMode.SUPER_TENANT_ADMIN);

        startCarbonServer(PORT_OFFSET_1, context, startupParameters);
        changeISConfiguration();

        super.init();
        super.createServiceClients(PORT_OFFSET_0, sessionCookie, new IdentityConstants.ServiceClientType[]{
                IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT,
                IdentityConstants.ServiceClientType.REMOTE_USERSTORE_MANAGER,
                IdentityConstants.ServiceClientType.IDENTITY_PROVIDER_MGT,
                IdentityConstants.ServiceClientType.OAUTH_CONFIG});
        super.createServiceClients(PORT_OFFSET_1, null, new IdentityConstants.ServiceClientType[]{
                IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT,
                IdentityConstants.ServiceClientType.REMOTE_USERSTORE_MANAGER,
                IdentityConstants.ServiceClientType.SAML_SSO_CONFIG,
                IdentityConstants.ServiceClientType.IDENTITY_PROVIDER_MGT});

        oidcApplication =
                createOIDCApplication(PORT_OFFSET_0, playgroundAppOneAppName, playgroundAppOneAppCallBackUri);
        createSAMLApplication(PORT_OFFSET_1, APPLICATION_NAME, new String[]{getTokenEndpoint(PORT_OFFSET_0)}, new
                String[]{getTokenEndpoint(PORT_OFFSET_0)}, TRAVELOCITY_ARTIFACT);
        super.addUser(PORT_OFFSET_1, username, password, null, getUserClaims(), null, true);
        deployTomcatServer(TRAVELOCITY_ARTIFACT);

        updateResidentIdpEntityId(PORT_OFFSET_1, IDP_ENTITY_ID);

        createIdentityProvider(PORT_OFFSET_0, IDP_ENTITY_ID);
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception{
        tomcatServer.stop();
        tomcatServer.destroy();

        super.deleteServiceProvider(PORT_OFFSET_1, APPLICATION_NAME);
        super.deleteServiceProvider(PORT_OFFSET_0, playgroundAppOneAppName);
        super.removeUser(PORT_OFFSET_1, username);

        updateResidentIdpEntityId(PORT_OFFSET_1, "localhost");
        super.deleteIdentityProvider(PORT_OFFSET_0, IDP_ENTITY_ID);
        super.stopCarbonServer(PORT_OFFSET_1);
        resetISConfiguration();
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO login", groups = "wso2.is")
    public void testSAMLBearerGrantDifferentIDPLocalDialect() {

        try {
            String samlResponse = sendSAMLRequest(TRAVELOCITY_ARTIFACT, SAML_SSO_URL, COMMON_AUTH_URL);
            String assertion = getAssertion(samlResponse);

            //Sending token request
            JSONObject obj = sendSAMLBearerGrantRequest(assertion, getTokenEndpoint(PORT_OFFSET_0),
                    oidcApplication.getClientId(), oidcApplication.getClientSecret());
            Object id_token = obj.get(ID_TOKEN);
            Object refresh_token = obj.get(REFRESH_TOKEN);

            SignedJWT signedJWT = SignedJWT.parse(id_token.toString());
            Assert.assertEquals(signedJWT.getJWTClaimsSet().getAllClaims().get(OIDC_EMAIL_CLAIM_URI), emailClaim);
            Assert.assertEquals(signedJWT.getJWTClaimsSet().getAllClaims().get(OIDC_LAST_NAME_CLAIM_URI), lastname);
            Assert.assertEquals(signedJWT.getJWTClaimsSet().getAllClaims().get(OIDC_FIRST_NAME_CLAIM_URI), firstname);

            obj = sendRefreshGrantRequest((String) refresh_token, getTokenEndpoint(PORT_OFFSET_0),
                    oidcApplication.getClientId(), oidcApplication.getClientSecret());
            id_token = obj.get(ID_TOKEN);

            signedJWT = SignedJWT.parse(id_token.toString());
            Assert.assertEquals(signedJWT.getJWTClaimsSet().getAllClaims().get(OIDC_EMAIL_CLAIM_URI), emailClaim);
            Assert.assertEquals(signedJWT.getJWTClaimsSet().getAllClaims().get(OIDC_LAST_NAME_CLAIM_URI), lastname);
            Assert.assertEquals(signedJWT.getJWTClaimsSet().getAllClaims().get(OIDC_FIRST_NAME_CLAIM_URI), firstname);


        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed", e);
        }
    }

    private void changeISConfiguration() throws Exception {

        String carbonHome = Utils.getResidentCarbonHome();
        identityXML = new File(carbonHome + File.separator
                + "repository" + File.separator + "conf" + File.separator + "identity" + File.separator + "identity" +
                ".xml");
        File configuredIdentityXML = new File(getISResourceLocation()
                + File.separator + "oidc" + File.separator + "convert-claims-identity.xml");

        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredIdentityXML, identityXML, true);
        serverConfigurationManager.restartGracefully();
    }

    private void resetISConfiguration() throws Exception {

        log.info("Replacing identity.xml with default configurations");

        File defaultIdentityXML = new File(getISResourceLocation() + File.separator + "default-identity.xml");

        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(defaultIdentityXML, identityXML, true);
        serverConfigurationManager.restartGracefully();
    }
}
