package de.ikor.sip.foundation.core.util;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.io.Resource;

/** Utility class for building Checksums */
@UtilityClass
public class ChecksumHelper {

  /** Comparator for {@link Resource}s that uses {@link java.net.URI}s for comparison. */
  public static final LambdaHelper.ThrowingComparator<Resource> DEFAULT_RESOURCE_COMPARATOR =
      (res1, res2) -> res1.getURI().compareTo(res2.getURI());

  /**
   * Builds a reproducible hash over the given <code>resources</code> with the selected hashing-
   * <code>algorithm</code>.
   *
   * <p>To make the hashes reproducible, the given resources are sorted first using the given <code>
   * comparator</code> before being fed into the hash digest. Therefore, two hashes on the same set
   * of resources result only in the same, reproducible hash if the same algorithm and comparator is
   * also used.
   *
   * @param resources Resources to use for creating the hash code
   * @param algorithm Algorith to use (see @{@link
   *     org.apache.commons.codec.digest.MessageDigestAlgorithms})
   * @param comparator (Optional) comparator to use. If non is given, {@link
   *     #DEFAULT_RESOURCE_COMPARATOR} is used
   * @return Hash code
   * @throws IOException Exception while reading the given resources
   */
  public static String calcualteReproducibleHashForResources(
      final Collection<Resource> resources,
      final String algorithm,
      final Optional<Comparator<Resource>> comparator)
      throws IOException {
    final var digestResources =
        resources.stream().sorted(comparator.orElse(DEFAULT_RESOURCE_COMPARATOR)).toList();
    final var digest = DigestUtils.getDigest(algorithm);
    for (var resource : digestResources) {
      DigestUtils.updateDigest(digest, resource.getInputStream());
    }
    return Hex.encodeHexString(digest.digest());
  }
}
