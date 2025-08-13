package one.x1f.sip.foundation.mvnplugin.model;

import java.nio.file.Path;
import java.util.Collection;

/**
 * Represents a source file that has been parsed for import statements.
 */
public record ParsedJavaFile(Path path, String fqcn, Collection<ImportStatement> imports) {

  public ParsedJavaFile(Path path, String fqcn, Collection<ImportStatement> imports) {
    this.path = path;
    this.fqcn = fqcn.trim();
    this.imports = imports;
  }
}
