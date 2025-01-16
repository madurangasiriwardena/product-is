/*
 * Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.base.MockApplicationServer;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;

import java.util.Map;

/**
 * This class is to handle the test cases to skip the login consent based on the file and per service provider.
 */
public class OIDCSPWiseSkipLoginConsentTestCase extends OIDCAbstractIntegrationTest {

    protected Log log = LogFactory.getLog(getClass());
    protected HttpClient client;
    private CookieStore cookieStore = new BasicCookieStore();
    private UserObject user;
    private Map<String, OIDCApplication> applications;
    protected String sessionDataKey;
    protected String sessionDataKeyConsent;
    private MockApplicationServer mockApplicationServer;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        user = OIDCUtilTest.initUser();
        createUser(user);
        applications = OIDCUtilTest.initApplications();
        createApplications(applications);
        configureSPToSkipConsent();
        client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();

        mockApplicationServer = new MockApplicationServer();
        mockApplicationServer.start();
    }

    @AfterClass(alwaysRun = true)
    public void clearObjects() throws Exception {

        deleteObjects();
        clear();
        mockApplicationServer.stop();
    }

    private void deleteObjects() throws Exception {

        deleteUser(user);
        deleteApplications(applications);
    }

    private void configureSPToSkipConsent() throws Exception {

        OIDCApplication oidcApplication = applications.get(OIDCUtilTest.PLAYGROUND_APP_TWO_APP_NAME);
        ApplicationPatchModel applicationPatch = new ApplicationPatchModel();
        applicationPatch.setAdvancedConfigurations(new AdvancedApplicationConfiguration().skipLoginConsent(true));
        updateApplication(oidcApplication.getApplicationId(), applicationPatch);
    }

    @Test(groups = "wso2.is", description = "Test authz endpoint before creating a valid session")
    public void testCreateUserSession() throws Exception {

        testSendAuthenticationRequest(applications.get(OIDCUtilTest.PLAYGROUND_APP_ONE_APP_NAME), true,
                client, cookieStore);
        testAuthentication();
    }

    @Test(groups = "wso2.is", description = "Initiate authentication request from playground.apptwo")
    public void testInitiateLoginRequestForAlreadyLoggedUser() throws Exception {

        testSendAuthenticationRequest(applications.get(OIDCUtilTest.PLAYGROUND_APP_TWO_APP_NAME), false,
                client, cookieStore);
    }

    private void testAuthentication() throws Exception {

        HttpResponse response = sendLoginPost(client, sessionDataKey);
        EntityUtils.consume(response.getEntity());
    }

    private void createApplications(Map<String, OIDCApplication> applications) throws Exception {

        for (Map.Entry<String, OIDCApplication> entry : applications.entrySet()) {
            createApplication(entry.getValue());
        }
    }

    private void deleteApplications(Map<String, OIDCApplication> applications) throws Exception {

        for (Map.Entry<String, OIDCApplication> entry : applications.entrySet()) {
            deleteApplication(entry.getValue());
        }
    }
}
