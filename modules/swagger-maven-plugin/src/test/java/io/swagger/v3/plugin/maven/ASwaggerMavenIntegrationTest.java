package io.swagger.v3.plugin.maven;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class ASwaggerMavenIntegrationTest extends BetterAbstractMojoTestCase {

  protected void setUp() throws Exception {
    // required for mojo lookups to work
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  SwaggerMojo runTest(File pom) throws Exception {
    return runTest(pom, this::validateOpenApiContent);
  }

  SwaggerMojo runTest(File pom, Consumer<List<OpenAPI>> validator) throws Exception {
    assertNotNull(pom);
    assertTrue(pom.exists());

    SwaggerMojo swaggerMojo = (SwaggerMojo) lookupConfiguredMojo(pom, "resolve");
    assertNotNull(swaggerMojo);
    // set random context id to not mix states with multiple tests
    swaggerMojo.setContextId(RandomStringUtils.randomAscii(32));
    if (swaggerMojo.getAdditionalOutputs() != null) {
      for (SingleOutputMojo additionalConfig : swaggerMojo.getAdditionalOutputs()) {
        additionalConfig.setContextId(RandomStringUtils.randomAscii(32));
      }
    }

    swaggerMojo.execute();

    final PlexusConfiguration config = extractPluginConfiguration("swagger-maven-plugin", pom);

    List<OpenAPI> generatedOpenAPIs = new ArrayList<>();

    generatedOpenAPIs.add(getOpenAPIOutput(swaggerMojo, config));
    if (swaggerMojo.getAdditionalOutputs() != null) {
      for (SingleOutputMojo additionalConfig : swaggerMojo.getAdditionalOutputs()) {
        generatedOpenAPIs.add(getOpenAPIOutput(additionalConfig, config));
      }
    }

    validator.accept(generatedOpenAPIs);

    return swaggerMojo;
  }

  private OpenAPI getOpenAPIOutput(SingleOutputMojo swaggerMojo, PlexusConfiguration config) throws IOException {
    String outputPath = swaggerMojo.getOutputPath();
    String outputFile = config.getChild("outputFileName").getValue();
    if (outputFile == null) {
      outputFile = "openapi";
    }
    String format = config.getChild("outputFormat").getValue();
    if (format.toLowerCase().equals("yaml") || format.toLowerCase().equals("jsonandyaml")) {
      Path path = Paths.get(outputPath, outputFile + ".yaml");
      File file = path.toFile();
      assertTrue(Files.isRegularFile(path));
      String content = FileUtils.readFileToString(file, "UTF-8");
      final OpenAPI openAPI = Yaml.mapper().readValue(content, OpenAPI.class);
      assertNotNull(openAPI);
      return openAPI;
    }
    if (format.toLowerCase().equals("json") || format.toLowerCase().equals("jsonandyaml")) {
      Path path = Paths.get(outputPath, outputFile + ".json");
      File file = path.toFile();
      assertTrue(Files.isRegularFile(path));
      String content = FileUtils.readFileToString(file, "UTF-8");
      final OpenAPI openAPI = Json.mapper().readValue(content, OpenAPI.class);
      assertNotNull(openAPI);
      return openAPI;
    }

    fail("No output file");
    return null;
  }

  void validateOpenApiContent(List<OpenAPI> openAPIs) {
    assertEquals(1, openAPIs.size());
    assertEquals(2, openAPIs.get(0).getServers().get(0).getVariables().size());
    assertNotNull(openAPIs.get(0).getInfo());
  }
}
