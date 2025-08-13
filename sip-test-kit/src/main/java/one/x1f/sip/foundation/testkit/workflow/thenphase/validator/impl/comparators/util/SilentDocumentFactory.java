package one.x1f.sip.foundation.testkit.workflow.thenphase.validator.impl.comparators.util;

import com.ctc.wstx.shaded.msv_core.verifier.jaxp.DocumentBuilderFactoryImpl;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A custom implementation of {@link DocumentBuilderFactoryImpl} that provides a silent document
 * builder. This factory produces document builders that suppress errors and log them instead of
 * throwing them directly.
 */
public class SilentDocumentFactory extends DocumentBuilderFactoryImpl {
  /**
   * Creates a new instance of a silent document builder.
   *
   * @return a new instance of {@link DocumentBuilder} with a custom error handler.
   * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the
   *     configuration requested.
   */
  @Override
  public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
    DocumentBuilder documentBuilder = super.newDocumentBuilder();
    documentBuilder.setErrorHandler(new SilentErrorHandler());
    return documentBuilder;
  }

  /**
   * A custom error handler that logs SAX parse exceptions silently without throwing them. This
   * handler is intended to be used with the {@link DocumentBuilder} produced by {@link
   * SilentDocumentFactory}.
   */
  @Slf4j
  private static class SilentErrorHandler implements ErrorHandler {
    @Override
    public void warning(SAXParseException exception) throws SAXException {
      log.debug("WARNING: " + exception.getMessage(), exception);
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
      log.debug("ERROR: " + exception.getMessage(), exception);
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
      log.debug("ERROR: " + exception.getMessage(), exception);
      throw exception;
    }
  }
}
