package one.x1f.sip.foundation.mvnplugin.model;

import java.nio.file.Path;
import java.util.List;

/** Holds the matches that were found within a single source file. */
public record BannedImportRecords(Path sourceFile, List<ImportStatement> matchedImports) {}
