package one.x1f.sip.foundation.mvnplugin;

import static java.lang.String.format;
import static one.x1f.sip.foundation.mvnplugin.model.ImportStatement.STATIC_PREFIX;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaSource;
import com.thoughtworks.qdox.model.impl.DefaultJavaClass;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import one.x1f.sip.foundation.mvnplugin.model.ImportStatement;
import one.x1f.sip.foundation.mvnplugin.model.ParsedJavaFile;

/** Parses a source file into a {@link ParsedJavaFile} representation. */
public final class ImportStatementParser {
  private static final String IMPORT_PREFIX = "import ";
  private final LineReader lineReader;

  /**
   * Constructor just for testing purposes.
   *
   * @param charset used by te analysed project.
   */
  ImportStatementParser(Charset charset) {
    this.lineReader = new LineReader(charset);
  }

  /**
   * Converts files into more suitable {@link ParsedJavaFile} format.
   *
   * @param sourceFilePath path of the given file
   * @return instance of {@link ParsedJavaFile} containing encapsulated information of matter about
   *     the file.
   */
  public ParsedJavaFile parse(Path sourceFilePath) {
    try {
      JavaSource src = parseAsJavaSource(sourceFilePath);
      List<ImportStatement> importStatements = parseImportLines(sourceFilePath, src.getImports());
      String fullyQualifiedName = new DefaultJavaClass(src).getFullyQualifiedName();
      return new ParsedJavaFile(sourceFilePath, fullyQualifiedName, importStatements);
    } catch (IOException e) {
      throw new UncheckedIOException(
          format("Encountered IOException while analyzing %s for banned imports", sourceFilePath),
          e);
    }
  }

  private List<ImportStatement> parseImportLines(Path sourceFilePath, List<String> importStrings)
      throws IOException {
    List<ImportStatement> imports = new LinkedList<>();
    try (final Stream<String> lines = this.lineReader.lines(sourceFilePath).stream()) {
      int row = 1;
      for (final Iterator<String> it = lines.map(String::trim).iterator(); it.hasNext(); ++row) {
        final String line = it.next();

        // Implementation note: We check for empty lines here
        // so that we are able to keep track of correct line numbers.
        if (line.isEmpty()) {
          continue;
        }
        if (importStrings.stream().anyMatch(line::contains)) {
          final List<ImportStatement> importStatements = parseImport(line, row);
          imports.addAll(importStatements);
        }
      }
      return imports;
    }
  }

  private JavaSource parseAsJavaSource(Path sourceFilePath) throws FileNotFoundException {
    JavaProjectBuilder builder = new JavaProjectBuilder();
    builder.addSource(new FileReader(sourceFilePath.toString()));
    return builder.getSources().stream()
        .findFirst()
        .orElseThrow(
            () -> new RuntimeException(format("qdox source parsing failed: %s", sourceFilePath)));
  }

  public List<ImportStatement> parseImport(String line, int lineNumber) {
    if (!isImport(line)) {
      return Collections.emptyList();
    }

    // There can be multiple import statements within the same line, so
    // we simply split them at their ';'
    final String[] parts = line.split(";");
    return Arrays.stream(parts)
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(s -> s.substring(IMPORT_PREFIX.length()))
        .map(importName -> toImportStatement(importName, lineNumber))
        .toList();
  }

  private ImportStatement toImportStatement(String importName, int lineNumber) {
    String trimmedImportName = importName.replace(STATIC_PREFIX, "").trim();
    boolean isStaticImport = importName.startsWith(STATIC_PREFIX);

    return new ImportStatement(trimmedImportName, lineNumber, isStaticImport);
  }

  private boolean isImport(String line) {
    return line.startsWith(IMPORT_PREFIX) && line.endsWith(";");
  }

  /**
   * Supplies lines but skips every encountered comment. Block comments that span multiple lines
   * will be replaced by the same amount of empty lines.
   */
  private static class LineReader {

    private final Charset charset;

    public LineReader(Charset charset) {
      this.charset = charset;
    }

    public Collection<String> lines(Path path) throws IOException {
      final Reader fileReader = Files.newBufferedReader(path, charset);
      try (final BufferedReader lineReader = new BufferedReader(fileReader)) {
        return lineReader.lines().toList();
      }
    }
  }
}
