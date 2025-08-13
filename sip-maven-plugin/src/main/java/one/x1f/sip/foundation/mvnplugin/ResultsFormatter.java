package one.x1f.sip.foundation.mvnplugin;

import java.util.List;
import one.x1f.sip.foundation.mvnplugin.model.AnalyzeResult;
import one.x1f.sip.foundation.mvnplugin.model.BannedImportRecords;
import one.x1f.sip.foundation.mvnplugin.model.ImportStatement;

/** Formats the info about found cross dependencies for presentation purpose. */
class ResultsFormatter {
  /** Formats the info about found cross dependencies for presentation purpose. */
  public String formatMatches(AnalyzeResult analyzeResult) {
    final StringBuilder b = new StringBuilder();

    b.append("SIP connectors cross-dependencies detected. Please decouple connector packages:\n");

    if (analyzeResult.bannedImportsInCompileCode()) {
      b.append("\nDependable connectors in main folder:\n\n");

      final List<BannedImportRecords> srcMatchesByGroup = analyzeResult.srcBanMatches();
      formatGroupedMatches(b, srcMatchesByGroup);
    }

    if (analyzeResult.bannedImportsInTestCode()) {
      b.append("\nDependable connectors in test folder:\n\n");
      final List<BannedImportRecords> testMatchesByGroup = analyzeResult.testBanMatches();
      formatGroupedMatches(b, testMatchesByGroup);
    }

    return b.toString();
  }

  private void formatGroupedMatches(StringBuilder b, List<BannedImportRecords> matches) {
    matches.forEach(
        fileMatch -> {
          b.append("\tin file").append(": ").append(fileMatch.getSourceFile()).append("\n");
          fileMatch.getMatchedImports().forEach(match -> appendMatch(match, b));
        });
  }

  private void appendMatch(ImportStatement match, StringBuilder b) {
    b.append("\t\t")
        .append("Line: ")
        .append(match.getLine())
        .append("\t")
        .append(match.getImportName())
        .append("\n");
  }
}
