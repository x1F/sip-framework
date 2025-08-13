package one.x1f.sip.foundation.mvnplugin.model;

import java.nio.file.Path;
import java.util.List;

/** Holds the matches that were found within a single source file. */
public final class BannedImportRecords {
  private final Path sourceFile;
  private final List<ImportStatement> matchedImports;

  public BannedImportRecords(Path sourceFile, List<ImportStatement> matchedImports) {
    this.sourceFile = sourceFile;
    this.matchedImports = matchedImports;
  }

  /**
   * The java source file containing the matches.
   *
   * @return The java source file.
   */
  public Path getSourceFile() {
    return this.sourceFile;
  }

  /**
   * The matches found in this file.
   *
   * @return The matches.
   */
  public List<ImportStatement> getMatchedImports() {
    return this.matchedImports;
  }
}
