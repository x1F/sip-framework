package one.x1f.sip.foundation.mvnplugin.model;

import java.util.*;
import java.util.regex.Pattern;

/** Holds the information of what imports should be banned and from which base package. */
public final class BannedImportGroup {
  private final String basePackage;
  private final List<String> bannedImports;

  public BannedImportGroup(String basePackage, List<String> bannedImports) {
    this.basePackage = basePackage.trim();
    this.bannedImports = bannedImports.stream().map(String::trim).toList();
  }

  public String getBasePackages() {
    return this.basePackage;
  }

  public boolean isImportBanned(String importName) {
    return bannedImports.stream().anyMatch(bannedImport -> regexMatch(bannedImport, importName));
  }

  public boolean matches(String packageName) {
    return regexMatch(this.getBasePackages(), packageName);
  }

  public boolean regexMatch(String pattern, String matchString) {
    return Pattern.compile(pattern).matcher(matchString).find();
  }
}
