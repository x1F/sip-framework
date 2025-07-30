package de.ikor.sip.foundation.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class MarkdownUtilTest {

  @Test
  void GIVEN_classpath_markdown_WHEN_embedding_image_files_VERIFY_result_contains_embedded_image()
      throws IOException {
    final var markdownString =
        MarkdownUtil.readMarkdownFileAndEmbedLocalImages(
            new ClassPathResource("markdown/markdown-embed.md"));

    assertThat(markdownString)
        .isNotBlank()
        .contains("Karte_Odenwaldexpress.png", "/this/file/does/not/exist.jpg")
        .doesNotContain("OdenwaldexpressMudau.jpg");
  }
}
