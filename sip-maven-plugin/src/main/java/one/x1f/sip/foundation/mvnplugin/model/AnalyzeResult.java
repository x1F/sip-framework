package one.x1f.sip.foundation.mvnplugin.model;

import java.util.List;
import lombok.AllArgsConstructor;

/** Final result of analyzing the code base for banned imports. */
@AllArgsConstructor
public final class AnalyzeResult {

  private final List<BannedImportRecords> srcMatches;
  private final List<BannedImportRecords> testMatches;

  /**
   * @return discovered {@link BannedImportGroup} in the source code files
   */
  public List<BannedImportRecords> srcBanMatches() {
    return srcMatches;
  }

  /**
   * @return discovered {@link BannedImportGroup} in the test code files
   */
  public List<BannedImportRecords> testBanMatches() {
    return testMatches;
  }

  /**
   * Returns whether at least one banned import has been found within the analyzed compile OR test
   * source files.
   *
   * @return Whether a banned import has been found.
   */
  public boolean bannedImportsFound() {
    return !srcMatches.isEmpty() || !testMatches.isEmpty();
  }

  /**
   * Returns whether at least one banned import has been found within the analyzed compile source
   * code.
   *
   * @return Whether a banned import has been found.
   */
  public boolean bannedImportsInCompileCode() {
    return !srcMatches.isEmpty();
  }

  /**
   * Returns whether at least one banned import has been found within the analyzed test source code.
   *
   * @return Whether a banned import has been found.
   */
  public boolean bannedImportsInTestCode() {
    return !testMatches.isEmpty();
  }
}
