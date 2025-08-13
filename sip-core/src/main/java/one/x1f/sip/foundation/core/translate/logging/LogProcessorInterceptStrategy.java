package one.x1f.sip.foundation.core.translate.logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.core.proxies.AddProxyInterceptStrategy;
import one.x1f.sip.foundation.core.translate.SIPTranslateMessageService;
import one.x1f.sip.foundation.core.util.CamelHelper;
import org.apache.camel.*;
import org.apache.camel.processor.LogProcessor;
import org.apache.camel.spi.InterceptStrategy;
import org.apache.camel.spi.LogListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Used for implicit message translations with LogProcessor (.log()). Implements {@link Ordered}
 * interface in order to be executed before {@link AddProxyInterceptStrategy}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogProcessorInterceptStrategy implements InterceptStrategy, Ordered {
  private final SIPTranslateMessageService translateMessageService;

  @Override
  public Processor wrapProcessorInInterceptors(
      CamelContext context, NamedNode definition, Processor target, Processor nextTarget) {
    // Target can be a Camel's wrapped processor (WrapProcessor)
    Processor originalProcessor = CamelHelper.unwrapProcessor(target);
    if (originalProcessor instanceof LogProcessor logProcessor) {
      target =
          (logProcessor.getExpression() == null)
              ? target
              : this.createNewLogProcessorWithTranslatedExpression(context, logProcessor);
    }
    return target;
  }

  private Processor createNewLogProcessorWithTranslatedExpression(
      CamelContext context, LogProcessor logProcessor) {
    Expression translatedExpression =
        this.translateExpression(context, logProcessor.getExpression());
    Set<LogListener> listeners = context.getCamelContextExtension().getLogListeners();

    LogProcessor newLogProcessor =
        new LogProcessor(
            translatedExpression,
            logProcessor.getLogger(),
            logProcessor.getLogFormatter(),
            listeners);
    newLogProcessor.setId(logProcessor.getId());
    newLogProcessor.setRouteId(logProcessor.getRouteId());
    newLogProcessor.start();
    return newLogProcessor;
  }

  private Expression translateExpression(CamelContext context, Expression oldExp) {
    return oldExp == null
        ? null
        : context.resolveLanguage("simple").createExpression(this.translateMsg(oldExp.toString()));
  }

  private String translateMsg(String message) {
    // Expected message format is message_key agr1 agr2 ... argn. We split by blank space (" ") to
    // isolate message
    // key and message arguments
    List<String> args = new ArrayList<>();
    Collections.addAll(args, message.trim().split(" "));
    List<String> arguments =
        args.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
    String messageKey = arguments.remove(0);

    String translatedMessage =
        translateMessageService.getTranslatedMessage(messageKey, arguments.toArray());
    return translatedMessage.equals(messageKey) ? message : translatedMessage;
  }

  @Override
  public int getOrder() {
    return 1;
  }
}
