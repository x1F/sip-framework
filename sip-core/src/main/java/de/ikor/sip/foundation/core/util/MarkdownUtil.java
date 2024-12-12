package de.ikor.sip.foundation.core.util;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

@UtilityClass
@Slf4j
public class MarkdownUtil {

  private static final String MARKDOWN_IMAGE_REGEX =
      "!\\[(?<alttext>[^\\]]*)\\]"
          + // Captures alt text
          "\\("
          + // Opening parenthesis
          "(?<uri>[^\\\"\\)]+?)"
          + // Captures uri (up to " or ))
          "(?:\\s*\"(?<optionalpart>[^\"]*?)\")?"
          + // Captures optional title
          "\\)"; // Closing parenthesis

  private static final Pattern MARKDOWN_IMAGE_PATTERN = Pattern.compile(MARKDOWN_IMAGE_REGEX);

  public String readMarkdownFileAndEmbedLocalImages(final ClassPathResource markdownResource)
      throws IOException {
    final var markdownWithEmbedded = new StringBuilder();
    final var content = markdownResource.getContentAsString(StandardCharsets.UTF_8);
    final var matcher = MARKDOWN_IMAGE_PATTERN.matcher(content);
    while (matcher.find()) {
      final var imageUri = URI.create(matcher.group("uri"));
      if (imageUri.isAbsolute()) {
        log.debug(
            "Skipping embedding of absolute/remote image at path '{}' for markdown-file at {}",
            imageUri,
            markdownResource);
        appendOriginalMatch(matcher, markdownWithEmbedded);
      } else {
        final var imagePath = Path.of(imageUri.getPath());
        final var imageResource =
            imagePath.isAbsolute()
                ? new FileSystemResource(imagePath)
                : markdownResource.createRelative(imagePath.toString());
        if (!imageResource.isReadable()) {
          log.warn(
              "Won't embed image at path '{}' for markdown-file at {}: Resource is not readable",
              imageResource,
              markdownResource);
          appendOriginalMatch(matcher, markdownWithEmbedded);
        } else {
          final var altText = matcher.group("alttext");
          final var mime =
              Optional.ofNullable(imageResource.getURL().openConnection().getContentType())
                  .orElse("application/octet-stream");
          final var base64Tag =
              String.format(
                  "![%s](data:%s;base64,%s)",
                  altText,
                  mime,
                  Base64.getEncoder().encodeToString(imageResource.getContentAsByteArray()));
          matcher.appendReplacement(markdownWithEmbedded, base64Tag);
        }
      }
    }
    matcher.appendTail(markdownWithEmbedded);
    return markdownWithEmbedded.toString();
  }

  private static void appendOriginalMatch(Matcher matcher, StringBuilder markdownWithEmbedded) {
    matcher.appendReplacement(markdownWithEmbedded, Matcher.quoteReplacement(matcher.group(0)));
  }
}
