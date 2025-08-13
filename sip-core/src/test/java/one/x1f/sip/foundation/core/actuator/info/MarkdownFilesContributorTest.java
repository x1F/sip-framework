package one.x1f.sip.foundation.core.actuator.info;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.info.Info;

class MarkdownFilesContributorTest {

  private static final String BUILD_KEY = "build";
  private static final String FILES_KEY = "files";

  private static final String FILES_TARGET_TESTING_DIRECTORY = "target/test-classes/documents/";
  private static final String README_MD = "readme.md";
  private static final String CHANGELOG_MD = "changelog.md";
  private static final String FILENAME_TXT = "test.txt";
  private static final String FILE_CONTENT = "Test data";

  private MarkdownFilesContributor subject;
  private Info.Builder builder;

  @BeforeEach
  void setup() {
    subject = new MarkdownFilesContributor();
    builder = new Info.Builder();
  }

  @Test
  void Given_markdownAndTextFilesInDocumentsFolder_When_contribute_Then_returnOnlyMarkdownFiles()
      throws Exception {
    // arrange
    Path directory = Paths.get(FILES_TARGET_TESTING_DIRECTORY);
    Files.createDirectories(directory);

    File file1 = createFile(FILES_TARGET_TESTING_DIRECTORY, README_MD, FILE_CONTENT);
    File file2 = createFile(FILES_TARGET_TESTING_DIRECTORY, CHANGELOG_MD, FILE_CONTENT);
    File file3 = createFile(FILES_TARGET_TESTING_DIRECTORY, FILENAME_TXT, FILE_CONTENT);

    // act
    subject.contribute(builder);

    // Keep environment clean
    file1.delete();
    file2.delete();
    file3.delete();

    // assert
    @SuppressWarnings("unchecked")
    LinkedHashMap<String, Object> buildInfoResult =
        (LinkedHashMap<String, Object>) builder.build().get(BUILD_KEY, LinkedHashMap.class);
    @SuppressWarnings("unchecked")
    List<MarkdownObject> resultFiles = (List<MarkdownObject>) buildInfoResult.get(FILES_KEY);

    assertThat(buildInfoResult).hasSize(1);
    assertThat(resultFiles).hasSize(2);
  }

  @Test
  void Given_noMarkdownFiles_When_contribute_Then_returnMissingMdFilesLog() {
    // arrange
    Logger logger =
        (Logger)
            LoggerFactory.getLogger(
                "one.x1f.sip.foundation.core.actuator.info.MarkdownFilesContributor");
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
    List<ILoggingEvent> logsList = listAppender.list;

    // act
    subject.contribute(builder);

    // assert
    assertThat(logsList.get(0).getMessage()).isEqualTo("sip.core.actuator.info.missingmdfiles");
  }

  private File createFile(String directory, String fileName, String fileContent)
      throws IOException {
    File file = new File(directory + fileName);
    FileWriter writer = new FileWriter(file);
    writer.write(fileContent);
    writer.close();
    return file;
  }
}
