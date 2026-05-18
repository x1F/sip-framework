package one.x1f.sip.foundation.mvnplugin;

import static org.mockito.Mockito.*;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConnectorsCrossDependenciesMojoTest {

  private MavenProject mavenProject;

  private final ConnectorsCrossDependenciesMojo subject = new ConnectorsCrossDependenciesMojo();

  @BeforeEach
  void setUp() {
    mavenProject = new MavenProject();

    mavenProject.getModel().addProperty("project.build.sourceEncoding", "UTF-8");
    mavenProject.addTestCompileSourceRoot("src\\test\\java");
    subject.setMavenProject(mavenProject);
  }

  @Test
  void when_ExecutePluginWithCrossedDependenciesInTestFolder_then_ExceptionIsThrown() {
    mavenProject.addCompileSourceRoot("src/test/java/");
    // Directing plugin to 'test' instead of 'main' folder for the source code
    ConnectorsCrossDependenciesMojo.sourceFolder = "test";
    Assertions.assertThrows(MojoExecutionException.class, subject::execute);
  }

  @Test
  void when_ExecutePluginWithNoCrossedDependencies_then_InfoMessageLogged()
      throws MojoExecutionException, MojoFailureException {
    // arrange
    mavenProject.addCompileSourceRoot("src/main/java/");
    Log mock = mock(Log.class);
    subject.setLog(mock);

    // act
    subject.execute();

    // assert
    verify(mock).info("No cross dependencies detected.");
  }
}
