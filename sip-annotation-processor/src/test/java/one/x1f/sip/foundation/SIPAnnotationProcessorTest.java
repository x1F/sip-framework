package one.x1f.sip.foundation;

import static com.google.common.truth.Truth.assertThat;
import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SIPAnnotationProcessorTest {
  JavaFileObject inboundConnectorAnnotation;
  JavaFileObject inboundConnectorParent;

  @BeforeEach
  void setup() {
    inboundConnectorAnnotation =
        JavaFileObjects.forSourceString(
            "one.x1f.sip.foundation.core.declarative.annotation.InboundConnector",
            """
                package one.x1f.sip.foundation.core.declarative.annotation;
                import java.lang.annotation.*;
                @Target(ElementType.TYPE)
                @Retention(RetentionPolicy.SOURCE)
                public @interface InboundConnector {}
            """);
    inboundConnectorParent =
        JavaFileObjects.forSourceString(
            "one.x1f.sip.foundation.core.declarative.connector.InboundConnectorBase",
            """
                package one.x1f.sip.foundation.core.declarative.connector;
                public class InboundConnectorBase {}
            """);
  }

  @Test
  void When_AnnotatedClassDoesNotExtendParent_Expect_CompilationFailure() {
    var source =
        JavaFileObjects.forSourceString(
            "one.x1f.sip.BadClass",
            """
                package one.x1f.sip;
                @one.x1f.sip.foundation.core.declarative.annotation.InboundConnector
                public class BadClass {}
            """);

    Compilation compilation =
        Compiler.javac()
            .withProcessors(new SIPAnnotationProcessor())
            .compile(inboundConnectorAnnotation, inboundConnectorParent, source);

    assertThat(compilation.status()).isEqualTo(Compilation.Status.FAILURE);
    assertThat(compilation)
        .hadErrorContaining(
            "one.x1f.sip.foundation.core.declarative.connector.InboundConnectorBase");
  }

  @Test
  void When_ClassIsValid_Expect_CompilationSuccess() {
    var source =
        JavaFileObjects.forSourceString(
            "one.x1f.sip.GoodClass",
            """
                package one.x1f.sip;
                @one.x1f.sip.foundation.core.declarative.annotation.InboundConnector
                public class GoodClass extends one.x1f.sip.foundation.core.declarative.connector.InboundConnectorBase {}
            """);

    Compilation compilation =
        Compiler.javac()
            .withProcessors(new SIPAnnotationProcessor())
            .compile(inboundConnectorAnnotation, inboundConnectorParent, source);

    assertThat(compilation.status()).isEqualTo(Compilation.Status.SUCCESS);
  }
}
