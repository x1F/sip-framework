package one.x1f.sip.foundation;

import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("one.x1f.sip.foundation.core.declarative.annotation.*")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class SIPAnnotationProcessor extends AbstractProcessor {

  private final Map<String, String> expectedParentClasses =
      Map.of(
          "one.x1f.sip.foundation.core.declarative.annotation.InboundConnector",
              "one.x1f.sip.foundation.core.declarative.connector.InboundConnectorBase",
          "one.x1f.sip.foundation.core.declarative.annotation.OutboundConnector",
              "one.x1f.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase",
          "one.x1f.sip.foundation.core.declarative.annotation.IntegrationScenario",
              "one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioBase",
          "one.x1f.sip.foundation.core.declarative.annotation.CompositeProcess",
              "one.x1f.sip.foundation.core.declarative.process.CompositeProcessBase",
          "one.x1f.sip.foundation.core.declarative.annotation.ConnectorGroup",
              "one.x1f.sip.foundation.core.declarative.connectorgroup.ConnectorGroupBase");

  @SuppressWarnings("java:S3516")
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      return false;
    }

    for (TypeElement annotation : annotations) {
      validateAnnotatedClassParent(roundEnv, annotation);
    }
    return false;
  }

  private void validateAnnotatedClassParent(RoundEnvironment roundEnv, TypeElement annotation) {
    String annotationName = annotation.getQualifiedName().toString();
    String expectedParent = expectedParentClasses.get(annotationName);
    if (expectedParent == null) return;

    for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
      if (element.getKind() != ElementKind.CLASS) continue;

      TypeElement classElement = (TypeElement) element;
      if (element.getKind() == ElementKind.CLASS && !extendsClass(classElement, expectedParent)) {
        processingEnv
            .getMessager()
            .printMessage(
                Diagnostic.Kind.ERROR,
                String.format(
                    "Class %s annotated with @%s must extend %s",
                    classElement.getQualifiedName(), annotation.getSimpleName(), expectedParent),
                element);
      }
    }
  }

  private boolean extendsClass(TypeElement type, String expectedParent) {
    Types typeUtils = processingEnv.getTypeUtils();
    Elements elementUtils = processingEnv.getElementUtils();
    TypeElement expectedParentElement = elementUtils.getTypeElement(expectedParent);
    if (expectedParentElement == null) return false;

    TypeMirror expectedMirror = expectedParentElement.asType();
    TypeMirror current = type.getSuperclass();

    while (current.getKind() != TypeKind.NONE) {
      if (typeUtils.isSameType(current, expectedMirror)) {
        return true;
      }
      TypeElement parent = (TypeElement) typeUtils.asElement(current);
      current = parent.getSuperclass();
    }
    return false;
  }
}
