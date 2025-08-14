package one.x1f.sip.foundation.soap.utils;

import static org.apache.camel.language.constant.ConstantLanguage.constant;

import one.x1f.sip.foundation.core.declarative.model.MarshallerDefinition;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.spi.DataFormat;
import org.apache.commons.lang3.StringUtils;

public interface OutboundSOAPMarshallerDefinition extends MarshallerDefinition {
  static MarshallerDefinition forDataFormatWithOperationAndAddress(
      final DataFormat dataFormat, String operationName, String address) {
    return route -> {
      if (StringUtils.isNotEmpty(address)) {
        route.setHeader(CxfConstants.DESTINATION_OVERRIDE_URL, constant(address));
      }
      forDataFormatWithOperation(dataFormat, operationName).accept(route);
    };
  }

  static MarshallerDefinition forDataFormatWithOperation(
      final DataFormat dataFormat, String operationName) {
    return route ->
        route.setHeader(CxfConstants.OPERATION_NAME, constant(operationName)).marshal(dataFormat);
  }
}
