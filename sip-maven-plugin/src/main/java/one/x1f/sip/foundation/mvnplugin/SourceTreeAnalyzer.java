package one.x1f.sip.foundation.mvnplugin;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import one.x1f.sip.foundation.mvnplugin.model.AnalyzeResult;
import one.x1f.sip.foundation.mvnplugin.model.BannedImportGroups;
import one.x1f.sip.foundation.mvnplugin.model.BannedImportRecords;
import org.apache.commons.io.FilenameUtils;

/** Performing file analysis on a folder level by navigating through the folder tree structure. */
final class SourceTreeAnalyzer {
  private static final String JAVA = "java";
  private final ImportAnalyzer importAnalyzer;
  private final ImportStatementParser fileParser;
  private final Collection<Path> mainFolders;
  private final Collection<Path> testFolders;

  SourceTreeAnalyzer(Collection<Path> srcDirs, Charset projectCharset) {
    this.importAnalyzer = new ImportAnalyzer();
    this.mainFolders = filterDirs(srcDirs, "main");
    this.testFolders = filterDirs(srcDirs, "test");
    this.fileParser = new ImportStatementParser(projectCharset);
  }

  /**
   * Lists files found under given folder.
   *
   * @param root {@link Path} of the folder where files are searched for.
   * @return Stream pointing to the {@link Path} of detected files.
   */
  public static Stream<Path> listFiles(Path root) {
    try {
      if (!Files.exists(root)) {
        return Stream.empty();
      }
      return Files.find(
          root,
          Integer.MAX_VALUE,
          (path, bfa) ->
              JAVA.equalsIgnoreCase(FilenameUtils.getExtension(path.getFileName().toString())));
    } catch (final IOException e) {
      throw new UncheckedIOException("Encountered IOException while listing files of " + root, e);
    }
  }

  /**
   * Checks the files in enclosed folder for not allowed import statements.
   *
   * @param groups {@link BannedImportGroups} contains the info about banned imports and base
   *     packages from which they are banned.
   * @return {@link AnalyzeResult} that reports of found matches.
   */
  public AnalyzeResult analyze(BannedImportGroups groups) {
    final List<BannedImportRecords> mainRecords = analyzeFolders(mainFolders, groups);
    final List<BannedImportRecords> testRecords = analyzeFolders(testFolders, groups);
    return new AnalyzeResult(mainRecords, testRecords);
  }

  private List<Path> filterDirs(Collection<Path> folders, String discriminator) {
    return folders.stream().filter(path -> path.toString().contains(discriminator)).toList();
  }

  private List<BannedImportRecords> analyzeFolders(
      Collection<Path> directories, BannedImportGroups groups) {
    return directories.stream()
        .flatMap(path -> analyzeFolder(path, groups))
        .collect(Collectors.toList());
  }

  private Stream<BannedImportRecords> analyzeFolder(Path srcDir, BannedImportGroups groups) {
    try (Stream<Path> sourceFiles = listFiles(srcDir)) {
      return sourceFiles.map(fileParser::parse).toList().stream()
          .map(parsedFile -> importAnalyzer.checkFile(parsedFile, groups))
          .filter(Optional::isPresent)
          .map(Optional::get);
    }
  }
}
