package one.x1f.sip.foundation.soap.declarative.connector;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.XMLStreamReader;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConverter;
import org.apache.camel.converter.jaxb.JaxbDataFormat;

/** Custom implementation of {@link JaxbDataFormat} which supports payload as stream */
public class SIPJaxbDataFormat extends JaxbDataFormat {

  private CamelContext camelContext;

  @Override
  public void setCamelContext(CamelContext camelContext) {
    super.setCamelContext(camelContext);
    this.camelContext = camelContext;
  }

  public SIPJaxbDataFormat(JAXBContext jaxbContext) {
    super(jaxbContext);
  }

  @Override
  public Object unmarshal(Exchange exchange, Object body) throws Exception {
    TypeConverter typeConverter = camelContext.getTypeConverter();
    try {
      InputStream is =
          getCamelContext()
              .getTypeConverter()
              .mandatoryConvertTo(InputStream.class, exchange, body);
      XMLStreamReader xmlReader = typeConverter.convertTo(XMLStreamReader.class, exchange, is);
      Object answer = createUnmarshaller().unmarshal(xmlReader);

      if (answer instanceof JAXBElement && isIgnoreJAXBElement()) {
        answer = ((JAXBElement<?>) answer).getValue();
      }
      return answer;
    } catch (JAXBException e) {
      throw new IOException(e);
    }
  }
}
