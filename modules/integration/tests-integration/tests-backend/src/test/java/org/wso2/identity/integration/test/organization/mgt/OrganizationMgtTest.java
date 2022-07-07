package org.wso2.identity.integration.test.organization.mgt;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.report.LevelResolverFactory;
import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.UserProfileMgtServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import javax.xml.xpath.XPathExpressionException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.assertNotBlank;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.extractApplicationIdFromLocationHeader;

public class OrganizationMgtTest extends ISIntegrationTest {

    protected static final String SERVICES = "/services";
    private static final String API_WEB_APP_ROOT = File.separator + "repository"
            + File.separator + "deployment" + File.separator + "server" + File.separator + "webapps" + File
            .separator + "api" + File.separator + "WEB-INF" + File.separator
            + "lib" + File.separator;

    private static final String JAR_EXTENSION = ".jar";

    OpenApiValidationFilter openApiValidationFilter;
    private ServerConfigurationManager scm;

    private String orgId;

    @BeforeClass
    protected void init() throws Exception {

        super.init(TestUserMode.TENANT_ADMIN);

        changeISConfiguration();
        super.init(TestUserMode.TENANT_ADMIN);

        String swaggerDefinition = getAPISwaggerDefinition("org.wso2.carbon.identity.organization.management.endpoint", "org.wso2.carbon.identity.organization.management.yaml");

        String basePathInSwagger = String.format("/o/\\{organization-domain\\}/api/server/%s", "v1");
        String basePath = String.format("/o/%s/api/server/%s", "root4a8d-113f-4211-a0d5-efe36b082211", "v1");

        RestAssured.baseURI = backendURL.replace(SERVICES, "");
        openApiValidationFilter = initValidationFilter(swaggerDefinition, basePathInSwagger, basePath);
    }

    @Test
    public void testCreateApplication() throws Exception {

        String body = readResource("create-organization.json", this.getClass());
        Response responseOfPost = getResponseOfPost("/o/root4a8d-113f-4211-a0d5-efe36b082211/api/server/v1/organizations", body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = responseOfPost.getHeader(HttpHeaders.LOCATION);
        orgId = extractApplicationIdFromLocationHeader(location);
        assertNotBlank(orgId);
        System.out.println("============orgId: " + orgId);
    }

    @Test
    public void testGetAllOrganizations() throws Exception {

        Response response = getResponseOfGet("/o/root4a8d-113f-4211-a0d5-efe36b082211/api/server/v1/organizations");
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    protected Response getResponseOfGet(String endpointUri) {

        return given().auth().preemptive().basic("admin", "admin")
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().ifValidationFails()
                .filter(openApiValidationFilter)
                .when()
                .get(endpointUri);
    }

    protected Response getResponseOfPost(String endpointUri, String body) {

        return given().auth().preemptive().basic("admin", "admin")
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .body(body)
                .log().ifValidationFails()
                .filter(openApiValidationFilter)
                .log().ifValidationFails()
                .when()
                .log().ifValidationFails()
                .post(endpointUri);
    }

    protected OpenApiValidationFilter initValidationFilter(String swaggerDefinition,String basePathInSwagger, String basePath)
            throws RemoteException {

        String swagger = replaceInSwaggerDefinition(swaggerDefinition, basePathInSwagger, basePath);
        OpenApiInteractionValidator openAPIValidator = OpenApiInteractionValidator
                .createForInlineApiSpecification(swagger)
                .withLevelResolver(LevelResolverFactory.withAdditionalPropertiesIgnored())
                .build();
        return new OpenApiValidationFilter(openAPIValidator);
    }

    private String replaceInSwaggerDefinition(String content, String find, String replace) {

        content = content.replaceAll(find, replace);
        return content;
    }

    /**
     * Read the Swagger Definition from the .jar file in the "api" webapp
     *
     * @param jarName         .jar name
     * @param swaggerYamlName .yaml name
     * @return content of the specified swagger definition
     * @throws IOException
     */
    protected static String getAPISwaggerDefinition(String jarName, String swaggerYamlName) throws IOException {

        File dir = new File(Utils.getResidentCarbonHome() + API_WEB_APP_ROOT);
        File[] files = dir.listFiles((dir1, name) -> name.startsWith(jarName) && name.endsWith(JAR_EXTENSION));
        JarFile jarFile = new JarFile(files[0]);
        JarEntry entry = jarFile.getJarEntry(swaggerYamlName);
        InputStream input = jarFile.getInputStream(entry);
        String content = getString(input);
        jarFile.close();
        return convertYamlToJson(content);
    }

    /**
     * Build an String from InputStream
     *
     * @param inputStream input stream
     * @return
     * @throws IOException
     */
    private static String getString(InputStream inputStream) throws IOException {

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    /**
     * Convert swagger definition from .yaml to .json
     *
     * @param yaml swagger definition as string
     * @return json converted swagger definition as string
     * @throws IOException
     */
    private static String convertYamlToJson(String yaml) throws IOException {

        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Object obj = yamlReader.readValue(yaml, Object.class);
        ObjectMapper jsonWriter = new ObjectMapper();
        return jsonWriter.writeValueAsString(obj);
    }

    public static String readResource(String filename, Class cClass) throws IOException {

        try (InputStream resourceAsStream = cClass.getResourceAsStream(filename);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(resourceAsStream)) {
            StringBuilder resourceFile = new StringBuilder();

            int character;
            while ((character = bufferedInputStream.read()) != -1) {
                char value = (char) character;
                resourceFile.append(value);
            }

            return resourceFile.toString();
        }
    }

    private void changeISConfiguration() throws IOException,
            XPathExpressionException, AutomationUtilException {

        log.info("Replacing the deployment.toml file");
        String carbonHome = Utils.getResidentCarbonHome();
        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File configuredTomlFile = new File
                (getISResourceLocation() + File.separator + "organization" + File.separator + "mgt" +
                        File.separator + "non-domain-tenants.toml");
        scm = new ServerConfigurationManager(isServer);
        scm.applyConfigurationWithoutRestart(configuredTomlFile, defaultTomlFile, true);
        scm.restartForcefully();
    }
}
