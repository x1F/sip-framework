package one.x1f.sip.foundation.mvnplugin;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import one.x1f.sip.foundation.mvnplugin.model.AnalyzeResult;
import one.x1f.sip.foundation.mvnplugin.model.BannedImportGroup;
import one.x1f.sip.foundation.mvnplugin.model.BannedImportGroups;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Mojo implementing connectors-cross-dependencies-check goal, executing on {@link
 * LifecyclePhase#VALIDATE} phase.
 */
@Mojo(name = "connectors-cross-dependencies-check", defaultPhase = LifecyclePhase.VALIDATE)
@SuppressWarnings("unchecked")
public class ConnectorsCrossDependenciesMojo extends AbstractMojo {
  protected static String sourceFolder = "main";

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  MavenProject mavenProject;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    Collection<Path> foldersUnderCheck = getFoldersUnderCheck(mavenProject);
    SourceTreeAnalyzer analyzer =
        new SourceTreeAnalyzer(foldersUnderCheck, getSourceFileCharset(mavenProject));

    BannedImportGroups bannedImportGroups = createBanGroups(getMainCompileSourceRoot(mavenProject));

    AnalyzeResult analyzeResult = analyzer.analyze(bannedImportGroups);
    this.validate(analyzeResult);
  }

  private void validate(AnalyzeResult analyzeResult) throws MojoExecutionException {
    if (analyzeResult.bannedImportsFound()) {
      throw new MojoExecutionException(new ResultsFormatter().formatMatches(analyzeResult));
    } else {
      getLog().info("No cross dependencies detected.");
    }
  }

  private String getMainCompileSourceRoot(MavenProject mavenProject) throws MojoFailureException {
    return (getCompileSourceRoots(mavenProject))
        .stream()
            .filter(path -> path.contains(sourceFolder))
            .findFirst()
            .orElseThrow(() -> new MojoFailureException("main folder not found"));
  }

  private BannedImportGroups createBanGroups(String mainJavaFolderPath) {
    Set<String> sipConnectorPackagePatterns = getSIPConnectorPackages(mainJavaFolderPath);
    if (sipConnectorPackagePatterns.isEmpty()) {
      return null;
    }
    BannedImportGroups bannedImportGroups = new BannedImportGroups();
    populateGroups(bannedImportGroups, sipConnectorPackagePatterns);
    return bannedImportGroups;
  }

  private void populateGroups(
      BannedImportGroups bannedGroups, Set<String> sipConnectorPackagePatterns) {
    LinkedList<String> mutableList = new LinkedList<>(List.copyOf(sipConnectorPackagePatterns));
    for (int i = 0; i < mutableList.size(); i += 1) {
      String basePackage = mutableList.remove(i);
      bannedGroups.addGroup(new BannedImportGroup(basePackage, new LinkedList<>(mutableList)));
      mutableList.add(i, basePackage);
    }
  }

  Set<String> getSIPConnectorPackages(String folderPath) {
    List<Path> javaFiles = SourceTreeAnalyzer.listFiles(Paths.get(folderPath)).toList();
    return javaFiles.stream()
        .filter(path -> path.toString().contains("connectors"))
        .filter(path -> path.toString().contains("java"))
        .map(this::pathToConnectorPattern)
        .collect(Collectors.toSet());
  }

  private String pathToConnectorPattern(Path path) {
    StringBuilder returnVal = new StringBuilder();
    int i = startingPackagePosition(path);

    while (!isConnectors(path, i) && hasNext(path, i)) {
      i += 1;
      returnVal.append(path.getName(i)).append('.');
    }

    returnVal
        .append(path.getName(i + 1))
        .append(".*"); // add first package after "connectors", and append .* pattern
    return returnVal.toString();
  }

  private boolean hasNext(Path path, int i) {
    return i < path.getNameCount();
  }

  private boolean isConnectors(Path path, int i) {
    return path.getName(i).toString().equals("connectors");
  }

  private int startingPackagePosition(Path path) {
    int i = 0;
    for (Path path1 : path) {
      if (path1.toString().equals("java")) {
        break;
      }
      i++;
    }
    return i;
  }

  public Collection<Path> mapToFilePaths(Iterable<String> pathNames) {
    Collection<Path> returnVal = new LinkedList<>();
    pathNames.forEach(pathName -> returnVal.add(Paths.get(pathName)));
    return returnVal;
  }

  private Charset getSourceFileCharset(MavenProject mavenProject) {
    final String mavenCharsetName =
        (String) mavenProject.getProperties().get("project.build.sourceEncoding");
    if (mavenCharsetName != null) {
      return Charset.forName(mavenCharsetName);
    }
    return Charset.defaultCharset();
  }

  void setMavenProject(MavenProject mavenProject) {
    this.mavenProject = mavenProject;
  }

  private Collection<Path> getFoldersUnderCheck(MavenProject mavenProject) {
    List<String> compileSourceRoots = mavenProject.getCompileSourceRoots();
    List<String> compileTestSourceRoots = mavenProject.getTestCompileSourceRoots();
    Iterable<String> combinedIterables =
        CollectionUtils.union(compileSourceRoots, compileTestSourceRoots);

    return mapToFilePaths(combinedIterables);
  }

  private List<String> getCompileSourceRoots(MavenProject mavenProject) {
    return mavenProject.getCompileSourceRoots();
  }
}
