package one.x1f.sip.foundation.core.actuator.info;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

/** POJO object for storing the markdown file */
@Getter
@Slf4j
public class MarkdownObject {

  private String name;
  @Setter private String content;

  public MarkdownObject(String name) {
    this.name = name;
  }

  /**
   * Creating a {@link MarkdownObject} from a Resource object.
   *
   * @param resource Resource.
   */
  public static Optional<MarkdownObject> createMdObject(Resource resource) {
    MarkdownObject mdObject = new MarkdownObject(resource.getFilename());

    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
      mdObject.setContent(FileCopyUtils.copyToString(reader));
    } catch (Exception e) {
      log.warn("sip.core.actuator.info.filebadcontent_{}", mdObject.getName());
    }
    return Optional.of(mdObject);
  }
}
