package de.ikor.sip.foundation.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.*;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

class ChecksumHelperTest {

  private static List<Resource> RESOURCES;

  @BeforeAll
  static void setUpResources() {
    RESOURCES =
        List.of(
            new ClassPathResource("checksum/countries.json"),
            new ClassPathResource("checksum/document.pdf"),
            new ClassPathResource("checksum/image.jpg"));
  }

  @Test
  void GIVEN_list_of_static_resources_VERIFY_all_exist_and_are_readable() {
    assertThat(RESOURCES).allSatisfy(resource -> assertThat(resource.isReadable()).isTrue());
  }

  @Test
  void GIVEN_list_of_static_resources_WHEN_changing_order_VERIFY_checksum_remains_equal()
      throws IOException {
    final var reverse = Lists.reverse(RESOURCES);

    final var checksumOriginal =
        ChecksumHelper.calcualteReproducibleHashForResources(
            RESOURCES, MessageDigestAlgorithms.MD5, Optional.empty());
    final var checksumReverse =
        ChecksumHelper.calcualteReproducibleHashForResources(
            reverse, MessageDigestAlgorithms.MD5, Optional.empty());

    assertThat(RESOURCES).allMatch(resource -> reverse.contains(resource)).isNotEqualTo(reverse);
    assertThat(checksumOriginal).isNotBlank().isEqualTo(checksumReverse);
  }

  @Test
  void GIVEN_a_static_resource_WHEN_making_small_modifications_VERIFY_checksums_differ()
      throws IOException {

    final var initialResource = RESOURCES.stream().findFirst().orElseThrow();
    final var initialContent = initialResource.getContentAsByteArray();
    final var differentResourceEqualContent = new ByteArrayResource(initialContent);
    final var differentResourceModifiedContent =
        new ByteArrayResource(Arrays.copyOfRange(initialContent, 1, initialContent.length));

    final var checksumInitial =
        ChecksumHelper.calcualteReproducibleHashForResources(
            Collections.singleton(initialResource), MessageDigestAlgorithms.MD5, Optional.empty());
    final var checksumEqualContent =
        ChecksumHelper.calcualteReproducibleHashForResources(
            Collections.singleton(differentResourceEqualContent),
            MessageDigestAlgorithms.MD5,
            Optional.empty());
    final var checksumModifiedContent =
        ChecksumHelper.calcualteReproducibleHashForResources(
            Collections.singleton(differentResourceModifiedContent),
            MessageDigestAlgorithms.MD5,
            Optional.empty());

    assertThat(checksumInitial).isNotBlank().isEqualTo(checksumEqualContent);
    assertThat(checksumModifiedContent).isNotBlank().isNotEqualTo(checksumInitial);
  }

  @Test
  void GIVEN_non_existant_resource_VERIFY_exception_is_triggered() throws IOException {
    final var resources = new ArrayList<>(RESOURCES);
    resources.add(new ClassPathResource("does/not/exist.here"));

    assertThatException()
        .isThrownBy(
            () ->
                ChecksumHelper.calcualteReproducibleHashForResources(
                    resources, MessageDigestAlgorithms.MD5, Optional.empty()))
        .withCauseInstanceOf(IOException.class);
  }
}
