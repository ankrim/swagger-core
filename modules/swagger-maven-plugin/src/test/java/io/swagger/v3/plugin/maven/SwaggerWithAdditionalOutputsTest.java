package io.swagger.v3.plugin.maven;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class SwaggerWithAdditionalOutputsTest extends ASwaggerMavenIntegrationTest {

  public void testResolve() throws Exception {
    File pom = getTestFile("src/test/resources/pom.resolveToFileWithAdditionalOutputs.xml");
    checkOutput(runTest(pom, openAPIs -> {
      assertEquals(2, openAPIs.size());

      assertEquals(2, openAPIs.get(0).getServers().get(0).getVariables().size());
      assertNotNull(openAPIs.get(0).getInfo());

      assertEquals(2, openAPIs.get(1).getServers().get(0).getVariables().size());
      assertNotNull(openAPIs.get(1).getInfo());
    }));
  }

  private void checkOutput(SwaggerMojo mojo) {
    assertNull(mojo.getConfigurationFilePath());
  }
}
