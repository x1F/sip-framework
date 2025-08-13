package one.x1f.sip.foundation.mvnplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import one.x1f.sip.foundation.mvnplugin.model.*;

/** Collects banned import matches from a single source file. */
class ImportAnalyzer {

  /**
   * Collects all imports that are banned within the given source file.
   *
   * @param sourceFile The parsed file to check for banned imports..
   * @param groups The groups of banned imports to check the file against. From all groups, the one
   *     with the most specific base pattern match is chosen.
   * @return a {@link BannedImportRecords} holds information about the found matches. Returns an
   *     empty optional if no matches were found.
   */
  Optional<BannedImportRecords> checkFile(ParsedJavaFile sourceFile, BannedImportGroups groups) {
    final BannedImportGroup group =
        groups == null ? null : groups.selectGroupFor(sourceFile.getFqcn()).orElse(null);
    if (group == null) {
      return Optional.empty();
    }

    final List<ImportStatement> matches = new ArrayList<>();
    for (final ImportStatement importStmt : sourceFile.getImports()) {
      if (group.isImportBanned(importStmt.getImportName())) {
        matches.add(importStmt);
      }
    }
    if (matches.isEmpty()) {
      return Optional.empty();
    }
    final BannedImportRecords bannedImportRecords =
        new BannedImportRecords(sourceFile.getPath(), matches);
    return Optional.of(bannedImportRecords);
  }
}
