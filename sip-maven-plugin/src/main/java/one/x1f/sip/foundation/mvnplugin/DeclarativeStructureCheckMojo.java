package one.x1f.sip.foundation.mvnplugin;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import one.x1f.sip.foundation.mvnplugin.model.ClassAnalysisResult;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(
    name = "declarative-structure-check",
    defaultPhase = LifecyclePhase.COMPILE,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DeclarativeStructureCheckMojo extends AbstractMojo {

  public static final String X1F_SIP_GROUP = "one.x1f.sip";
  public static final String JAVA_EXTENSION = ".java";
  public static final String JAR_EXTENSION = ".jar";
  public static final String APACHE_CAMEL_GROUP = "org.apache.camel";

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  MavenProject mavenProject;

  @Parameter(defaultValue = "${project.basedir}/src/main/java")
  private File sourceDir;

  private final DeclarativeClassAnalyser declarativeClassAnalyser = new DeclarativeClassAnalyser();

  @Override
  public void execute() throws MojoExecutionException {
    try {
      CombinedTypeSolver typeSolver = new CombinedTypeSolver();
      typeSolver.add(new ReflectionTypeSolver());
      typeSolver.add(new JavaParserTypeSolver(sourceDir));

      for (Artifact dep : mavenProject.getArtifacts()) {
        if (!dep.getGroupId().startsWith(X1F_SIP_GROUP)
            && !dep.getGroupId().startsWith(APACHE_CAMEL_GROUP)) continue;
        File file = dep.getFile();
        if (file != null && file.exists() && file.getName().endsWith(JAR_EXTENSION)) {
          typeSolver.add(new JarTypeSolver(file));
        } else if (file != null && file.exists() && file.getAbsolutePath().contains("sip-core")) {
          typeSolver.add(new JavaParserTypeSolver(file));
        }
      }

      ParserConfiguration config =
          new ParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));

      JavaParser parser = new JavaParser(config);

      try (Stream<Path> pathStream = Files.walk(sourceDir.toPath())) {
        var results =
            pathStream
                .filter(p -> p.toString().endsWith(JAVA_EXTENSION))
                .map(path -> analyseClass(parser, path))
                .toList();
        validate(results);
      }
    } catch (Exception e) {
      throw new MojoExecutionException("Error analysing declarative structure.", e);
    }
  }

  private ClassAnalysisResult analyseClass(JavaParser parser, Path path) {
    try {
      Optional<CompilationUnit> result = parser.parse(path).getResult();
      if (result.isEmpty()) {
        return new ClassAnalysisResult(false, null);
      }
      for (ClassOrInterfaceDeclaration clazz :
          result.get().findAll(ClassOrInterfaceDeclaration.class)) {
        var classAnalysisResult = declarativeClassAnalyser.doAnalysis(clazz);
        if (classAnalysisResult.error()) {
          return classAnalysisResult;
        }
      }
    } catch (Exception e) {
      getLog().warn("Failed to parse " + path + ": " + e.getMessage());
    }
    return new ClassAnalysisResult(false, null);
  }

  private void validate(List<ClassAnalysisResult> analyseResult) throws MojoExecutionException {
    boolean hasErrors = false;

    for (ClassAnalysisResult res : analyseResult) {
      if (res.error()) {
        getLog().error(res.message());
        hasErrors = true;
      }
    }

    if (hasErrors) {
      throw new MojoExecutionException("Validation failed.");
    } else {
      getLog().info("Declarative Structure is valid.");
    }
  }

  protected void setMavenProject(MavenProject mavenProject) {
    this.mavenProject = mavenProject;
  }

  protected void setSourceDir(File sourceDir) {
    this.sourceDir = sourceDir;
  }
}
