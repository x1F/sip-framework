package one.x1f.sip.foundation.mvnplugin;

import static org.mockito.Mockito.*;

import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

class ConnectorsCrossDependenciesMojoTest {

  private MavenProject mavenProject;

  private final ConnectorsCrossDependenciesMojo subject = new ConnectorsCrossDependenciesMojo();

  @BeforeEach
  void setUp() {
    mavenProject = mock(MavenProject.class, Answers.RETURNS_DEEP_STUBS);
    when(mavenProject.getCompileSourceRoots()).thenReturn(List.of("src/test/java/"));
    when(mavenProject.getProperties().get("project.build.sourceEncoding")).thenReturn("UTF-8");
    when(mavenProject.getTestCompileSourceRoots()).thenReturn(List.of("src\\test\\java"));
    subject.setMavenProject(mavenProject);
  }

  @Test
  void when_ExecutePluginWithCrossedDependenciesInTestFolder_then_ExceptionIsThrown() {
    // Directing plugin to 'test' instead of 'main' folder for the source code
    ConnectorsCrossDependenciesMojo.sourceFolder = "test";
    Assertions.assertThrows(MojoExecutionException.class, subject::execute);
  }

  @Test
  void when_ExecutePluginWithNoCrossedDependencies_then_InfoMessageLogged()
      throws MojoExecutionException, MojoFailureException {
    // arrange
    when(mavenProject.getCompileSourceRoots()).thenReturn(List.of("src/main/java/"));
    Log mock = mock(Log.class);
    subject.setLog(mock);

    // act
    subject.execute();

    // assert
    verify(mock).info("No cross dependencies detected.");
  }
}
