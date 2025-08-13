package one.x1f.sip.foundation.mvnplugin.model;

import java.nio.file.Path;
import java.util.List;

/**
 * Holds the matches that were found within a single source file.
 */
public record BannedImportRecords(Path sourceFile, List<ImportStatement> matchedImports) {

  /**
   * The java source file containing the matches.
   *
   * @return The java source file.
   */
  @Override
  public Path sourceFile() {
    return this.sourceFile;
  }

  /**
   * The matches found in this file.
   *
   * @return The matches.
   */
  @Override
  public List<ImportStatement> matchedImports() {
    return this.matchedImports;
  }
}
