package one.x1f.sip.foundation.mvnplugin.model;

import java.nio.file.Path;
import java.util.Collection;

/** Represents a source file that has been parsed for import statements. */
public final class ParsedJavaFile {

  private final Path path;
  private final String fqcn;
  private final Collection<ImportStatement> imports;

  public ParsedJavaFile(Path path, String fqcn, Collection<ImportStatement> imports) {
    this.path = path;
    this.fqcn = fqcn.trim();
    this.imports = imports;
  }

  public Path getPath() {
    return path;
  }

  public Collection<ImportStatement> getImports() {
    return imports;
  }

  public String getFqcn() {
    return fqcn;
  }
}
