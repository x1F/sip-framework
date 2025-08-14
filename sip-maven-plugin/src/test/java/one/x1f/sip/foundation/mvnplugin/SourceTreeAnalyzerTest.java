package one.x1f.sip.foundation.mvnplugin;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class SourceTreeAnalyzerTest {

  @Test
  void when_listFilesOfUnExistingDir_then_returnEmptyStream() {
    // arrange
    Path path = Path.of("unexisting", "folder");
    // act
    Stream<Path> pathStream = SourceTreeAnalyzer.listFiles(path);
    // assert
    assertThat(pathStream.count()).isZero();
  }
}
