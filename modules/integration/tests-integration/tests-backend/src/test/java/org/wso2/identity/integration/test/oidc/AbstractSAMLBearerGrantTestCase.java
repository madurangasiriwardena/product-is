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

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.wso2.carbon.identity.application.common.model.idp.xsd.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.xsd.Claim;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.identity.integration.test.application.mgt.AbstractIdentityFederationTestCase;
import org.wso2.identity.integration.test.application.mgt.bean.OIDCApplication;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.IdentityConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AbstractSAMLBearerGrantTestCase extends AbstractIdentityFederationTestCase {

    protected static final String playgroundAppOneAppName = "playground.appone";
    protected static final int TOMCAT_PORT = 8490;
    protected static final String playgroundAppOneAppCallBackUri = "http://localhost:" + TOMCAT_PORT + "/playground" +
            ".appone/oauth2client";

    protected static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    protected static final String FIRST_NAME_CLAIM_URI = "http://wso2.org/claims/givenname";
    protected static final String LAST_NAME_CLAIM_URI = "http://wso2.org/claims/lastname";

    protected static final String OIDC_EMAIL_CLAIM_URI = "email";
    protected static final String OIDC_FIRST_NAME_CLAIM_URI = "given_name";
    protected static final String OIDC_LAST_NAME_CLAIM_URI = "family_name";

    protected static final String APPLICATION_NAME = "SAML-SSO-TestApplication";
    protected static final String INBOUND_AUTH_TYPE = "samlsso";
    protected static final String ATTRIBUTE_CS_INDEX_VALUE = "1239245949";
    protected static final String ATTRIBUTE_CS_INDEX_NAME = "attrConsumServiceIndex";
    protected static final String ACS_URL = "http://localhost:8490/%s/home.jsp";
    protected static final String NAMEID_FORMAT =
            "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    protected static final String LOGIN_URL = "/carbon/admin/login.jsp";
    protected static final String SAML_SSO_LOGIN_URL =
            "http://localhost:8490/%s/samlsso?SAML2.HTTPBinding=%s";
    protected static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    protected static final String COMMON_AUTH_URL = "https://localhost:9853/commonauth";
    protected static final String SAML_SSO_URL = "https://localhost:9853/samlsso";
    protected static final String TOKEN_ENDPOINT = "https://localhost:%s/oauth2/token";
    protected static final int PORT_OFFSET_0 = 0;
    protected static final int PORT_OFFSET_1 = 1;
    protected static final String ID_TOKEN = "id_token";
    protected static final String REFRESH_TOKEN = "refresh_token";

    protected HttpClient httpClient = new DefaultHttpClient();
    protected OIDCApplication oidcApplication;
    protected Tomcat tomcatServer;

    protected String username = "samlbeareruser";
    protected String password = "samlbeareruser";

    protected String firstname = "firstname1";
    protected String lastname = "lastname1";
    protected String emailClaim = "email1";


    protected static final String TRAVELOCITY_ARTIFACT = "travelocity.com";

    protected OIDCApplication createOIDCApplication(int portOffset, String playgroundAppOneAppName,
                                                    String playgroundAppOneAppCallBackUri) throws Exception {

        List<String> requiredClaims = new ArrayList<>();
        requiredClaims.add(EMAIL_CLAIM_URI);
        requiredClaims.add(FIRST_NAME_CLAIM_URI);
        requiredClaims.add(LAST_NAME_CLAIM_URI);

        return super.createOIDCApplication(portOffset, playgroundAppOneAppName, playgroundAppOneAppCallBackUri,
                requiredClaims);
    }

    protected void deployTomcatServer(String artifact) throws LifecycleException {

        tomcatServer = Utils.getTomcat(getClass());

        URL resourceUrl = getClass()
                .getResource(File.separator + "samples" + File.separator + artifact + ".war");
        Utils.startTomcat(tomcatServer, "/" + artifact, resourceUrl.getPath());
    }

    protected JSONObject sendRefreshGrantRequest(String refresh_token, String tokenEndpoint, String clientId, String
            clientSecret) throws IOException {

        HttpResponse response;
        JSONObject obj;
        List<NameValuePair> urlParameters;
        HttpPost request;
        BufferedReader rd;

        urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME, "refresh_token"));
        urlParameters.add(new BasicNameValuePair("refresh_token", refresh_token));

        request = new HttpPost(tokenEndpoint);
        request.setHeader(CommonConstants.USER_AGENT_HEADER, OAuth2Constant.USER_AGENT);
        request.setHeader(OAuth2Constant.AUTHORIZATION_HEADER, OAuth2Constant.BASIC_HEADER + " " + Base64
                .encodeBase64String((clientId + ":" + clientSecret)
                        .getBytes()).trim());
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        response = httpClient.execute(request);

        rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        obj = (JSONObject) JSONValue.parse(rd);
        EntityUtils.consume(response.getEntity());
        return obj;
    }

    protected JSONObject sendSAMLBearerGrantRequest(String assertion, String tokenEndpoint, String clientId, String
            clientSecret) throws IOException {

        HttpResponse response;
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME, OAuth2Constant
                .OAUTH2_GRANT_TYPE_SAML2_BEARER));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.SCOPE_PLAYGROUND_NAME, OAuth2Constant
                .OAUTH2_SCOPE_OPENID + " saml"));
        urlParameters.add(new BasicNameValuePair("assertion", Base64.encodeBase64String(assertion
                .getBytes())));

        HttpPost request = new HttpPost(tokenEndpoint);
        request.setHeader(CommonConstants.USER_AGENT_HEADER, OAuth2Constant.USER_AGENT);
        request.setHeader(OAuth2Constant.AUTHORIZATION_HEADER, OAuth2Constant.BASIC_HEADER + " " + Base64
                .encodeBase64String((clientId + ":" + clientSecret)
                        .getBytes()).trim());
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        response = httpClient.execute(request);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        JSONObject obj = (JSONObject) JSONValue.parse(rd);
        EntityUtils.consume(response.getEntity());
        return obj;
    }

    protected String getAssertion(String samlResponse) {

        String patternStr = "<saml2:Assertion[\\s\\S]*?</saml2:Assertion>";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(new String(Base64.decodeBase64(samlResponse)));
        String assertion = null;
        if (matcher.find()) {
            assertion = matcher.group();
        } else {
            Assert.fail();
        }
        return assertion;
    }

    protected String sendSAMLRequest(String artifact, String samlSsoUrl, String commonAuthUrl) throws Exception {

        HttpResponse response;

        response = Utils.sendGetRequest(String.format(SAML_SSO_LOGIN_URL, artifact, "HTTP-POST"),
                USER_AGENT, httpClient);

        String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
        response = sendSAMLMessage(samlSsoUrl, CommonConstants.SAML_REQUEST_PARAM, samlRequest);
        EntityUtils.consume(response.getEntity());

        response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, artifact, httpClient);

        String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
        response = Utils.sendPOSTMessage(sessionKey, samlSsoUrl, USER_AGENT, ACS_URL, artifact,
                username, password, httpClient);
//        EntityUtils.consume(response.getEntity());

//        if (requestMissingClaims(response)) {
//            response = Utils.sendPOSTClaimMessage(response, commonAuthUrl, USER_AGENT, ACS_URL, artifact, httpClient);
//            EntityUtils.consume(response.getEntity());
//        }

        if (requestConsent(response)) {
            String pastrCookie = Utils.getPastreCookie(response);
            Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");

            EntityUtils.consume(response.getEntity());

            response = Utils.sendPOSTConsentMessage(response, commonAuthUrl, USER_AGENT, ACS_URL, httpClient, pastrCookie);
        }
        EntityUtils.consume(response.getEntity());

        String redirectUrl = Utils.getRedirectUrl(response);
        if (StringUtils.isNotBlank(redirectUrl)) {
            response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, artifact, httpClient);
        }
        String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
        EntityUtils.consume(response.getEntity());
        return samlResponse;

    }

    protected ClaimValue[] getUserClaims() {

        ClaimValue[] claimValues;

        claimValues = new ClaimValue[3];

        ClaimValue firstName = new ClaimValue();
        firstName.setClaimURI(FIRST_NAME_CLAIM_URI);
        firstName.setValue(firstname);
        claimValues[0] = firstName;

        ClaimValue lastName = new ClaimValue();
        lastName.setClaimURI(LAST_NAME_CLAIM_URI);
        lastName.setValue(lastname);
        claimValues[1] = lastName;

        ClaimValue email = new ClaimValue();
        email.setClaimURI(EMAIL_CLAIM_URI);
        email.setValue(emailClaim);
        claimValues[2] = email;

        return claimValues;
    }

    protected HttpResponse sendSAMLMessage(String url, String samlMsgKey, String samlMsgValue) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", USER_AGENT);
        urlParameters.add(new BasicNameValuePair(samlMsgKey, samlMsgValue));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    protected boolean requestMissingClaims(HttpResponse response) {

        String redirectUrl = Utils.getRedirectUrl(response);
        return redirectUrl.contains("claims.do");

    }

    protected boolean requestConsent(HttpResponse response) {

        String redirectUrl = Utils.getRedirectUrl(response);
        return redirectUrl.contains("consent.do");

    }

    protected void createSAMLApplication(int portOffset, String applicationName, String[] audiences, String[]
            recipients, String artifact) throws Exception {

        createSAMLSSOConfig(portOffset, audiences, recipients, artifact);

        super.addServiceProvider(portOffset, applicationName);

        ServiceProvider serviceProvider = super.getServiceProvider(portOffset, applicationName);

        InboundAuthenticationRequestConfig requestConfig = new InboundAuthenticationRequestConfig();
        requestConfig.setInboundAuthType(INBOUND_AUTH_TYPE);
        requestConfig.setInboundAuthKey(artifact);

        Property attributeConsumerServiceIndexProp = new Property();
        attributeConsumerServiceIndexProp.setName(ATTRIBUTE_CS_INDEX_NAME);
        attributeConsumerServiceIndexProp.setValue(ATTRIBUTE_CS_INDEX_VALUE);
        requestConfig.setProperties(new Property[]{attributeConsumerServiceIndexProp});

        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(
                new InboundAuthenticationRequestConfig[]{requestConfig});

        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);

        serviceProvider.setClaimConfig(getSPClaimConfig());

        super.updateServiceProvider(portOffset, serviceProvider);
    }

    protected org.wso2.carbon.identity.application.common.model.xsd.ClaimConfig getSPClaimConfig() {

        org.wso2.carbon.identity.application.common.model.xsd.ClaimConfig claimConfig = new org.wso2.carbon.identity
                .application.common.model.xsd.ClaimConfig();
        claimConfig.setLocalClaimDialect(true);

        Claim firstNameClaim = new Claim();
        firstNameClaim.setClaimUri(FIRST_NAME_CLAIM_URI);
        ClaimMapping firstNameClaimMapping = new ClaimMapping();
        firstNameClaimMapping.setRequested(true);
        firstNameClaimMapping.setMandatory(false);
        firstNameClaimMapping.setLocalClaim(firstNameClaim);
        firstNameClaimMapping.setRemoteClaim(firstNameClaim);

        Claim lastNameClaim = new Claim();
        lastNameClaim.setClaimUri(LAST_NAME_CLAIM_URI);
        ClaimMapping lastNameClaimMapping = new ClaimMapping();
        lastNameClaimMapping.setRequested(true);
        lastNameClaimMapping.setMandatory(false);
        lastNameClaimMapping.setLocalClaim(lastNameClaim);
        lastNameClaimMapping.setRemoteClaim(lastNameClaim);

        Claim emailClaim = new Claim();
        emailClaim.setClaimUri(EMAIL_CLAIM_URI);
        ClaimMapping emailClaimMapping = new ClaimMapping();
        emailClaimMapping.setRequested(true);
        emailClaimMapping.setMandatory(false);
        emailClaimMapping.setLocalClaim(emailClaim);
        emailClaimMapping.setRemoteClaim(emailClaim);

        claimConfig.setClaimMappings(new ClaimMapping[]{emailClaimMapping, lastNameClaimMapping,
                firstNameClaimMapping});
        return claimConfig;
    }

    protected void createSAMLSSOConfig(int portOffset, String[] audiences, String[] recipients, String artifact) throws
            Exception {

        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
        samlssoServiceProviderDTO.setIssuer(artifact);
        samlssoServiceProviderDTO.setAssertionConsumerUrls(new String[]{String.format(ACS_URL,
                artifact)});
        samlssoServiceProviderDTO.setDefaultAssertionConsumerUrl(String.format(ACS_URL, "travelocity.com"));
        samlssoServiceProviderDTO.setAttributeConsumingServiceIndex(ATTRIBUTE_CS_INDEX_VALUE);
        samlssoServiceProviderDTO.setNameIDFormat(NAMEID_FORMAT);
        samlssoServiceProviderDTO.setDoSignAssertions(true);
        samlssoServiceProviderDTO.setDoSignResponse(true);
        samlssoServiceProviderDTO.setDoSingleLogout(true);
        samlssoServiceProviderDTO.setLoginPageURL(LOGIN_URL);
        samlssoServiceProviderDTO.setEnableAttributeProfile(true);
        samlssoServiceProviderDTO.setEnableAttributesByDefault(true);
        samlssoServiceProviderDTO.setRequestedAudiences(audiences);
        samlssoServiceProviderDTO.setRequestedRecipients(recipients);

        super.createSAML2WebSSOConfiguration(portOffset, samlssoServiceProviderDTO);
    }

    public static FederatedAuthenticatorConfig getFederatedAuthenticator(FederatedAuthenticatorConfig[]
                                                                                 federatedAuthenticators,
                                                                         String authenticatorName) {

        for (FederatedAuthenticatorConfig authenticator : federatedAuthenticators) {
            if (authenticator.getName().equals(authenticatorName)) {
                return authenticator;
            }
        }

        return null;
    }

    protected void createIdentityProvider(int portOffset, String name) throws Exception {

        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName(name);

        FederatedAuthenticatorConfig saml2SSOAuthnConfig = new FederatedAuthenticatorConfig();
        saml2SSOAuthnConfig.setName("SAMLSSOAuthenticator");
        saml2SSOAuthnConfig.setDisplayName("samlsso");
        saml2SSOAuthnConfig.setEnabled(true);
        saml2SSOAuthnConfig.setProperties(getSAML2SSOAuthnConfigProperties());
        identityProvider.setDefaultAuthenticatorConfig(saml2SSOAuthnConfig);
        identityProvider.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{saml2SSOAuthnConfig});
        identityProvider.setAlias(getTokenEndpoint(portOffset));

        String cert = "MIIDSTCCAjGgAwIBAgIEAoLQ/TANBgkqhkiG9w0BAQsFADBVMQswCQYDVQQGEwJV\n" +
                "UzELMAkGA1UECBMCQ0ExFjAUBgNVBAcTDU1vdW50YWluIFZpZXcxDTALBgNVBAoT\n" +
                "BFdTTzIxEjAQBgNVBAMTCWxvY2FsaG9zdDAeFw0xNzA3MTkwNjUyNTFaFw0yNzA3\n" +
                "MTcwNjUyNTFaMFUxCzAJBgNVBAYTAlVTMQswCQYDVQQIEwJDQTEWMBQGA1UEBxMN\n" +
                "TW91bnRhaW4gVmlldzENMAsGA1UEChMEV1NPMjESMBAGA1UEAxMJbG9jYWxob3N0\n" +
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAluZFdW1ynitztkWLC6xK\n" +
                "egbRWxky+5P0p4ShYEOkHs30QI2VCuR6Qo4Bz5rTgLBrky03W1GAVrZxuvKRGj9V\n" +
                "9+PmjdGtau4CTXu9pLLcqnruaczoSdvBYA3lS9a7zgFU0+s6kMl2EhB+rk7gXluE\n" +
                "ep7lIOenzfl2f6IoTKa2fVgVd3YKiSGsyL4tztS70vmmX121qm0sTJdKWP4HxXyq\n" +
                "K9neolXI9fYyHOYILVNZ69z/73OOVhkh/mvTmWZLM7GM6sApmyLX6OXUp8z0pkY+\n" +
                "vT/9+zRxxQs7GurC4/C1nK3rI/0ySUgGEafO1atNjYmlFN+M3tZX6nEcA6g94Iav\n" +
                "yQIDAQABoyEwHzAdBgNVHQ4EFgQUtS8kIYxQ8UVvVrZSdgyide9OHxUwDQYJKoZI\n" +
                "hvcNAQELBQADggEBABfk5mqsVUrpFCYTZZhOxTRRpGXqoW1G05bOxHxs42Paxw8r\n" +
                "AJ06Pty9jqM1CgRPpqvZa2lPQBQqZrHkdDE06q4NG0DqMH8NT+tNkXBe9YTre3EJ\n" +
                "CSfsvswtLVDZ7GDvTHKojJjQvdVCzRj6XH5Truwefb4BJz9APtnlyJIvjHk1hdoz\n" +
                "qyOniVZd0QOxLAbcdt946chNdQvCm6aUOputp8Xogr0KBnEy3U8es2cAfNZaEkPU\n" +
                "8Va5bU6Xjny8zGQnXCXxPKp7sMpgO93nPBt/liX1qfyXM7xEotWoxmm6HZx8oWQ8\n" +
                "U5aiXjZ5RKDWCCq4ZuXl6wVsUz1iE61suO5yWi8=";

        identityProvider.setCertificate(cert);

        identityProvider.setClaimConfig(getIDPClaimConfig());

        super.addIdentityProvider(portOffset, identityProvider);
    }

    protected ClaimConfig getIDPClaimConfig() {

        ClaimConfig claimConfig = new ClaimConfig();
        claimConfig.setLocalClaimDialect(true);
        return claimConfig;
    }

    private org.wso2.carbon.identity.application.common.model.idp.xsd.Property[] getSAML2SSOAuthnConfigProperties() {

        org.wso2.carbon.identity.application.common.model.idp.xsd.Property[] properties = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property[3];
        org.wso2.carbon.identity.application.common.model.idp.xsd.Property property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID);
        property.setValue("idp.wso2.com");
        properties[0] = property;

        property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.SP_ENTITY_ID);
        property.setValue("idp.wso2.com");
        properties[1] = property;

        property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.SSO_URL);
        property.setValue("https://localhost:9854/samlsso");
        properties[2] = property;

        return properties;
    }

    protected void updateResidentIdpEntityId(int portOffset, String idpEntityId) throws Exception {

        IdentityProvider residentIdP = super.getResidentIdP(portOffset);

        FederatedAuthenticatorConfig samlAuthenticatorConfig =
                getFederatedAuthenticator(residentIdP.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.Authenticator.SAML2SSO.NAME);

        org.wso2.carbon.identity.application.common.model.idp.xsd.Property[] properties = samlAuthenticatorConfig.getProperties();
        for (org.wso2.carbon.identity.application.common.model.idp.xsd.Property property: properties) {
            if ("IdPEntityId".equals(property.getName())) {
                property.setValue(idpEntityId);
            }
        }
        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs = new FederatedAuthenticatorConfig[1];
        federatedAuthenticatorConfigs[0] = samlAuthenticatorConfig;
        residentIdP.setFederatedAuthenticatorConfigs(federatedAuthenticatorConfigs);
        super.updateResidentIdp(portOffset, residentIdP);
    }

    protected String getTokenEndpoint(int offset) {

        return String.format(TOKEN_ENDPOINT, offset + CommonConstants.IS_DEFAULT_HTTPS_PORT);
    }

}
