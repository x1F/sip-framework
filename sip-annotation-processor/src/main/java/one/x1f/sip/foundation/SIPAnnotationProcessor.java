package one.x1f.sip.foundation;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes("one.x1f.sip.foundation.core.declarative.annotation.InboundConnector")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class SIPAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }
        Types typeUtils = processingEnv.getTypeUtils();
        Elements elementUtils = processingEnv.getElementUtils();
        for (TypeElement annotation : annotations) {
            TypeElement parentClass = elementUtils.getTypeElement("one.x1f.sip.foundation.core.declarative.connector.InboundConnectorBase");
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (element.getKind() == ElementKind.CLASS) {
                    TypeMirror classType = element.asType();

                    if (typeUtils.isSubtype(classType, parentClass.asType())) {
                        processingEnv.getMessager().printMessage(
                                Diagnostic.Kind.NOTE,
                                element.getSimpleName() + " extends InboundConnectorBase"
                        );
                    } else {
                        processingEnv.getMessager().printMessage(
                                Diagnostic.Kind.ERROR,
                                element.getSimpleName() + " must extend InboundConnectorBase"
                        );
                    }

                }
            }
        }
        return false;
    }
}
