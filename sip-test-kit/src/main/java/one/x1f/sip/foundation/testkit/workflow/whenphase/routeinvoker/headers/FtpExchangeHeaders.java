package one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.headers;

/** Enum with Camel FTP, FTPS, SFTP exchange headers which we can set programmatically */
public enum FtpExchangeHeaders {
  CAMEL_FILE_ABSOLUTE("CamelFileAbsolute"),
  CAMEL_FILE_HOST("CamelFileHost"),
  CAMEL_FILE_RELATIVE_PATH("CamelFileRelativePath"),
  CAMEL_FILE_ABSOLUTE_PATH("CamelFileAbsolutePath");

  FtpExchangeHeaders(String headerKey) {
    this.headerKey = headerKey;
  }

  private final String headerKey;

  /**
   * Getter for header key
   *
   * @return String
   */
  public String getValue() {
    return this.headerKey;
  }
}
