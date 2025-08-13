package one.x1f.sip.foundation.mvnplugin.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/** Groups multiple {@link BannedImportGroup} objects. */
public final class BannedImportGroups {
  private final List<BannedImportGroup> groups = new LinkedList<>();

  public void addGroup(BannedImportGroup group) {
    groups.add(group);
  }

  /**
   * Selects the {@link BannedImportGroup} with the most specific base package matching the given
   * full qualified class name. If the most specific match also specifies an exclusion pattern for
   * the given fqcn the result will be empty.
   *
   * @param fqcn The full qualified class name to find the group for.
   * @return The group with the most specific base package match.
   */
  public Optional<BannedImportGroup> selectGroupFor(String fqcn) {
    return groups.stream()
        .map(group -> matches(group, fqcn))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .sorted()
        .map(GroupMatch::getGroup)
        .findFirst();
  }

  private Optional<GroupMatch> matches(BannedImportGroup group, String fqcn) {
    return group.matches(fqcn) ? Optional.of(new GroupMatch(group)) : Optional.empty();
  }

  private static class GroupMatch {
    private final BannedImportGroup group;

    public GroupMatch(BannedImportGroup group) {
      this.group = group;
    }

    public BannedImportGroup getGroup() {
      return this.group;
    }
  }
}
