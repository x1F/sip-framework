package one.x1f.sip.foundation.mvnplugin.model;

import static one.x1f.sip.foundation.mvnplugin.ImportStatementParser.STATIC_PREFIX;

/** Represents an import statement that has been discovered while parsing a source file. */
public final class ImportStatement {

  public static final String STATIC_PREFIX = "static ";

  private final String importName;
  private final int line;
  private final boolean staticImport;

  public ImportStatement(String importName, int line, boolean staticImport) {
    this.importName = importName;
    this.line = line;
    this.staticImport = staticImport;
  }

  /**
   * The physical line within the source file in which the import has occurred. Number is always
   * 1-based!
   *
   * @return The line number of the matched imports.
   */
  public int getLine() {
    return line;
  }

  /**
   * Returns the import name including the 'static ' prefix if this represents a static import.
   *
   * @return The full import name.
   */
  public String getImportName() {
    if (staticImport) {
      return STATIC_PREFIX + importName;
    }
    return importName;
  }
}
