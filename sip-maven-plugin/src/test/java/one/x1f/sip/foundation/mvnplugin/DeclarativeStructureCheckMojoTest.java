package one.x1f.sip.foundation.mvnplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Set;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeclarativeStructureCheckMojoTest {
  private final DeclarativeStructureCheckMojo subject = new DeclarativeStructureCheckMojo();
  private Log log;

  @BeforeEach
  void setUpMocks() {
    MavenProject mavenProject = mock(MavenProject.class);
    log = mock(Log.class);
    subject.setLog(log);
    File sipCore = new File("../sip-core/src/main/java");
    DefaultArtifact artifact =
        new DefaultArtifact(
            "one.x1f.sip.foundation",
            sipCore.getName(),
            "1.0",
            "compile",
            "jar",
            "",
            new DefaultArtifactHandler("jar"));
    artifact.setFile(sipCore);
    when(mavenProject.getArtifacts()).thenReturn(Set.of(artifact));

    subject.setMavenProject(mavenProject);
  }

  @Test
  void When_InvalidDeclarativeStructure_Expect_MojoExecutionException() {
    subject.setSourceDir(new File("src/test/java"));
    MojoExecutionException ex = assertThrows(MojoExecutionException.class, subject::execute);
    assertEquals("Error analysing declarative structure.", ex.getMessage());

    verify(log, times(1)).error(anyString());
    verify(log, times(1)).warn(anyString());
    verify(log)
        .error(
            contains(
                "Class one.x1f.sip.foundation.connectors.con1.NoParentConnector annotated with @InboundConnector does not extend the required base type."));
    verify(log)
        .warn(
            contains(
                "Class one.x1f.sip.foundation.connectors.con1.NoAnnotationConnector might need to be annotated with @OutboundConnector to match the required base class or made abstract."));
  }
}
