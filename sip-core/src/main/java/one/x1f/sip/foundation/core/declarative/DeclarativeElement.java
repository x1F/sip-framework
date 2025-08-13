package one.x1f.sip.foundation.core.declarative;

/**
 * Common interface for Declarative elements (connectors, scenarios, ...) to include their common
 * functions.
 */
public interface DeclarativeElement {

  /**
   * Returns the ID of the Declarative Element. Must be unique within the scope of the adapter.
   *
   * @return ID of the element
   */
  String getId();

  /**
   * Returns the path to the documentation resource for this Declarative Element. The documentation
   * resource is a file that contains documentation for the Element.
   *
   * @return Path to the documentation resource
   */
  String getPathToDocumentationResource();
}
