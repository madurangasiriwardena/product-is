/*
*  Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.application.mgt;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.extensions.servers.carbonserver.MultipleServersManager;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.xsd.Claim;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceIdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderInfoDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.CarbonTestServerManager;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.application.mgt.bean.OIDCApplication;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.IdentityConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class AbstractIdentityFederationTestCase extends ISIntegrationTest {

    private Map<Integer, ApplicationManagementServiceClient> applicationManagementServiceClients;
    private Map<Integer, IdentityProviderMgtServiceClient> identityProviderMgtServiceClients;
    private Map<Integer, SAMLSSOConfigServiceClient> samlSSOConfigServiceClients;
    private Map<Integer, RemoteUserStoreManagerServiceClient> remoteUserStoreManagerServiceClients;
    private Map<Integer, OauthAdminClient> oauthAdminClients;
    protected Map<Integer, AutomationContext> automationContextMap;
    private Map<Integer, Tomcat> tomcatServers;
    private HttpClient httpClient;
    private MultipleServersManager manager;
    protected static final int DEFAULT_PORT = CommonConstants.IS_DEFAULT_HTTPS_PORT;

    public void initTest() throws Exception {
        super.init();

        applicationManagementServiceClients = new HashMap<Integer, ApplicationManagementServiceClient>();
        identityProviderMgtServiceClients = new HashMap<Integer, IdentityProviderMgtServiceClient>();
        samlSSOConfigServiceClients = new HashMap<Integer, SAMLSSOConfigServiceClient>();
        remoteUserStoreManagerServiceClients = new HashMap<Integer, RemoteUserStoreManagerServiceClient>();
        oauthAdminClients = new HashMap<Integer, OauthAdminClient>();
        automationContextMap = new HashMap<Integer, AutomationContext>();
        httpClient = new DefaultHttpClient();
        tomcatServers = new HashMap<Integer, Tomcat>();
        manager = new MultipleServersManager();

        automationContextMap.put(0, isServer);
    }

    public void startCarbonServer(int portOffset, AutomationContext context, Map<String, String> startupParameters)
            throws Exception {

        automationContextMap.put(portOffset, context);
        CarbonTestServerManager server = new CarbonTestServerManager(context, System.getProperty("carbon.zip"),
                                                                      startupParameters);
        manager.startServers(server);
    }

    public void stopCarbonServer(int portOffset) throws Exception {
        manager.stopAllServers();
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void stopHttpClient() {
        httpClient.getConnectionManager().shutdown();
    }

    public void createServiceClients(int portOffset, String sessionCookie,
                                     IdentityConstants.ServiceClientType[] adminClients)
            throws Exception {

        if (adminClients == null) {
            return;
        }

        String serviceUrl = getSecureServiceUrl(portOffset,
                                                automationContextMap.get(portOffset).getContextUrls()
                                                        .getSecureServiceUrl());

        if (sessionCookie == null) {
            AuthenticatorClient authenticatorClient = new AuthenticatorClient(serviceUrl);

            sessionCookie = authenticatorClient.login(automationContextMap.get(portOffset).getSuperTenant().getTenantAdmin()
                                                              .getUserName(),
                                                      automationContextMap.get(portOffset).getSuperTenant()
                                                              .getTenantAdmin().getPassword(),
                                                      automationContextMap.get(portOffset).getDefaultInstance()
                                                              .getHosts().get("default"));
        }

        if (sessionCookie != null) {
            ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem
                    (null, null);
            for (IdentityConstants.ServiceClientType clientType : adminClients) {
                if (IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT.equals(clientType)) {
                    applicationManagementServiceClients.put(portOffset, new ApplicationManagementServiceClient
                            (sessionCookie, serviceUrl, configContext));
                } else if (IdentityConstants.ServiceClientType.IDENTITY_PROVIDER_MGT.equals(clientType)) {
                    identityProviderMgtServiceClients.put(portOffset, new IdentityProviderMgtServiceClient(sessionCookie,
                                                                                                           serviceUrl));
                } else if (IdentityConstants.ServiceClientType.SAML_SSO_CONFIG.equals(clientType)) {
                    samlSSOConfigServiceClients.put(portOffset, new SAMLSSOConfigServiceClient(serviceUrl, sessionCookie));
                } else if (IdentityConstants.ServiceClientType.OAUTH_CONFIG.equals(clientType)) {
                    oauthAdminClients.put(portOffset, new OauthAdminClient(serviceUrl, sessionCookie));
                } else if (IdentityConstants.ServiceClientType.REMOTE_USERSTORE_MANAGER.equals(clientType)) {
                    remoteUserStoreManagerServiceClients.put(portOffset, new RemoteUserStoreManagerServiceClient
                            (serviceUrl, sessionCookie));
                }
            }
        }
    }

    public void addServiceProvider(int portOffset, String applicationName) throws Exception {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(applicationName);
        serviceProvider.setDescription("This is a test Service Provider");
        applicationManagementServiceClients.get(portOffset).createApplication(serviceProvider);
    }

    public ServiceProvider getServiceProvider(int portOffset, String applicationName)
            throws Exception {
        return applicationManagementServiceClients.get(portOffset).getApplication(applicationName);
    }

    public void updateServiceProvider(int portOffset, ServiceProvider serviceProvider)
            throws Exception {
        applicationManagementServiceClients.get(portOffset).updateApplicationData(serviceProvider);
    }

    public void deleteServiceProvider(int portOffset, String applicationName) throws Exception {
        applicationManagementServiceClients.get(portOffset).deleteApplication(applicationName);
    }

    public void addIdentityProvider(int portOffset, IdentityProvider identityProvider)
            throws Exception {
        identityProviderMgtServiceClients.get(portOffset).addIdP(identityProvider);
    }

    public IdentityProvider getIdentityProvider(int portOffset, String idPName) throws Exception {
        return identityProviderMgtServiceClients.get(portOffset).getIdPByName(idPName);
    }

    public IdentityProvider getResidentIdP(int portOffset) throws Exception {
        return identityProviderMgtServiceClients.get(portOffset).getResidentIdP();
    }

    public void updateResidentIdp(int portOffset, IdentityProvider identityProvider) throws Exception {
        identityProviderMgtServiceClients.get(portOffset).updateResidentIdP(identityProvider);
    }

    public void updateIdentityProvider(int portOffset, String oldIdPName,
                                       IdentityProvider identityProvider) throws Exception {
        identityProviderMgtServiceClients.get(portOffset).updateIdP(oldIdPName, identityProvider);
    }

    public void deleteIdentityProvider(int portOffset, String idPName) throws Exception {
        identityProviderMgtServiceClients.get(portOffset).deleteIdP(idPName);
    }

    public String createSAML2WebSSOConfiguration(int portOffset,
                                                 SAMLSSOServiceProviderDTO samlssoServiceProviderDTO)
            throws Exception {
        samlSSOConfigServiceClients.get(portOffset).addServiceProvider(samlssoServiceProviderDTO);
        SAMLSSOServiceProviderInfoDTO serviceProviders = samlSSOConfigServiceClients.get(portOffset).getServiceProviders();
        if (serviceProviders != null && serviceProviders.getServiceProviders() != null) {
            for (SAMLSSOServiceProviderDTO serviceProvider : serviceProviders.getServiceProviders()) {
                if (samlssoServiceProviderDTO.getIssuer().equals(serviceProvider.getIssuer())) {
                    return serviceProvider.getAttributeConsumingServiceIndex();
                }
            }
        }
        return null;
    }

    public void deleteSAML2WebSSOConfiguration(int portOffset, String issuer) throws Exception {
        samlSSOConfigServiceClients.get(portOffset).removeServiceProvider(issuer);
    }

    public void startTomcat(int port) throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.getService().setContainer(tomcat.getEngine());
        tomcat.setPort(port);
        tomcat.setBaseDir("");

        StandardHost stdHost = (StandardHost) tomcat.getHost();
        stdHost.setAppBase("");
        stdHost.setAutoDeploy(true);
        stdHost.setDeployOnStartup(true);
        stdHost.setUnpackWARs(true);
        tomcat.setHost(stdHost);

        setSystemProperties();
        tomcatServers.put(port, tomcat);
        tomcat.start();
    }

    public void stopTomcat(int port) throws LifecycleException, InterruptedException {
        tomcatServers.get(port).stop();
        tomcatServers.get(port).destroy();
        tomcatServers.remove(port);
        Thread.sleep(10000);
    }

    public void addWebAppToTomcat(int port, String webAppUrl, String webAppPath)
            throws LifecycleException {
        tomcatServers.get(port).addWebapp(tomcatServers.get(port).getHost(), webAppUrl, webAppPath);
    }

    public String extractValueFromResponse(HttpResponse response, String key, int token)
            throws IOException {
        String value = null;
        String line = null;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains(key)) {
                String[] tokens = line.split("'");
                value = tokens[token];
                break;
            }
        }
        bufferedReader.close();
        return value;
    }

    public Map<String, String> extractValuesFromResponse(HttpResponse response,
                                                         Map<String, Integer> keyMap)
            throws IOException {
        Map<String, String> values = new HashMap<String, String>();
        String line = null;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        while ((line = bufferedReader.readLine()) != null && keyMap.size() > 0) {
            Iterator iterator = keyMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iterator.next();
                if (line.contains(entry.getKey())) {
                    String[] tokens = line.split("'");
                    values.put(entry.getKey(), tokens[entry.getValue()]);
                    iterator.remove();
                }
            }
        }
        bufferedReader.close();
        return values;
    }

    public String getHeaderValue(HttpResponse response, String headerName) {
        Header[] headers = response.getAllHeaders();
        String headerValue = null;
        for (Header header : headers) {
            if (headerName.equals(header.getName())) {
                headerValue = header.getValue();
                break;
            }
        }
        return headerValue;
    }

    public boolean validateSAMLResponse(HttpResponse response, String userName) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            buffer.append(line);
        }
        bufferedReader.close();
        return buffer.toString().contains("You are logged in as " + userName);
    }

    public void closeHttpConnection(HttpResponse response) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        bufferedReader.close();
    }

    public void addUser(int portOffset, String username, String password, String[] roleList, ClaimValue[] claimValues,
                        String profileName, boolean requirePasswordChange) throws
            RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException, UserStoreException {

        remoteUserStoreManagerServiceClients.get(portOffset).addUser(username, password, roleList, claimValues,
                profileName,
                requirePasswordChange);
    }

    public void removeUser(int portOffset, String username) throws
            RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException, UserStoreException {

        remoteUserStoreManagerServiceClients.get(portOffset).deleteUser(username);
    }

    public OIDCApplication createOIDCApplication(int portOffset, String name, String callBackURL, List<String> requiredClaims)
            throws Exception {

        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setApplicationName(name);
        appDTO.setCallbackUrl(callBackURL);
        appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO.setGrantTypes("refresh_token urn:ietf:params:oauth:grant-type:saml2-bearer");

        oauthAdminClients.get(portOffset).registerOAuthApplicationData(appDTO);
        OAuthConsumerAppDTO[] appDtos = oauthAdminClients.get(portOffset).getAllOAuthApplicationData();

        OIDCApplication oidcApplication = null;
        for (OAuthConsumerAppDTO appDto : appDtos) {
            if (appDto.getApplicationName().equals(name)) {
                oidcApplication = new OIDCApplication();
                oidcApplication.setClientId(appDto.getOauthConsumerKey());
                oidcApplication.setClientSecret(appDto.getOauthConsumerSecret());
                break;
            }
        }
        if (oidcApplication == null) {
            throw new Exception("OAuth application is not created properly.");
        }

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(name);
        serviceProvider.setDescription(name);
        applicationManagementServiceClients.get(portOffset).createApplication(serviceProvider);

        serviceProvider = applicationManagementServiceClients.get(portOffset).getApplication(name);

        ClaimConfig claimConfig = null;
        if (!requiredClaims.isEmpty()) {
            claimConfig = new ClaimConfig();
            for (String claimUri : requiredClaims) {
                Claim claim = new Claim();
                claim.setClaimUri(claimUri);
                ClaimMapping claimMapping = new ClaimMapping();
                claimMapping.setRequested(true);
                claimMapping.setLocalClaim(claim);
                claimMapping.setRemoteClaim(claim);
                claimConfig.addClaimMappings(claimMapping);
                claimConfig.setLocalClaimDialect(true);
            }
        }

        serviceProvider.setClaimConfig(claimConfig);
        serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());
        List<InboundAuthenticationRequestConfig> authRequestList = new ArrayList<>();

        if (oidcApplication.getClientId() != null) {
            InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig = new
                    InboundAuthenticationRequestConfig();
            inboundAuthenticationRequestConfig.setInboundAuthKey(oidcApplication.getClientId());
            inboundAuthenticationRequestConfig.setInboundAuthType(OAuth2Constant.OAUTH_2);
            if (StringUtils.isNotBlank(oidcApplication.getClientSecret())) {
                Property property = new Property();
                property.setName(OAuth2Constant.OAUTH_CONSUMER_SECRET);
                property.setValue(oidcApplication.getClientSecret());
                Property[] properties = {property};
                inboundAuthenticationRequestConfig.setProperties(properties);
            }
            authRequestList.add(inboundAuthenticationRequestConfig);
        }

        if (authRequestList.size() > 0) {
            serviceProvider.getInboundAuthenticationConfig().setInboundAuthenticationRequestConfigs(authRequestList
                    .toArray(new InboundAuthenticationRequestConfig[authRequestList.size()]));
        }

        applicationManagementServiceClients.get(portOffset).updateApplicationData(serviceProvider);

        return oidcApplication;
    }

    private void setSystemProperties() {
        URL resourceUrl = getClass().getResource(File.separator + "keystores" + File.separator + "products" + File.separator + "wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStore", resourceUrl.getPath());
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
    }

    private String getSecureServiceUrl(int portOffset, String baseUrl) {
        return baseUrl.replace("9853", String.valueOf(DEFAULT_PORT + portOffset)) + "/";
    }

}
