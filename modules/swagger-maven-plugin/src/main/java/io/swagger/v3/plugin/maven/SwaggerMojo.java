package io.swagger.v3.plugin.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.util.List;

@Mojo(
  name = "resolve",
  requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
  defaultPhase = LifecyclePhase.COMPILE,
  threadSafe = true,
  configurator = "include-project-dependencies"
)
public class SwaggerMojo extends SingleOutputMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    super.execute();

    if (isCollectionNotBlank(additionalOutputs)) {
      for (SingleOutputMojo additionalConfig : additionalOutputs) {
        additionalConfig.execute();
      }
    }
  }

  @Parameter(property = "resolve.additionalOutputs")
  private List<SingleOutputMojo> additionalOutputs;


  public List<SingleOutputMojo> getAdditionalOutputs() {
    return additionalOutputs;
  }

  public void setAdditionalOutputs(List<SingleOutputMojo> additionalOutputs) {
    this.additionalOutputs = additionalOutputs;
  }
}
