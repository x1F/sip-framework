package one.x1f.sip.foundation.mvnplugin;

import static one.x1f.sip.foundation.mvnplugin.DeclarativeStructureCheckMojo.X1F_SIP_GROUP;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import java.util.*;
import one.x1f.sip.foundation.mvnplugin.model.ClassAnalysisResult;

public class DeclarativeClassAnalyser {

  private final Map<String, String> expectedParentInterfaces =
      Map.of(
          "InboundConnector",
          "one.x1f.sip.foundation.core.declarative.connector.InboundConnectorDefinition",
          "OutboundConnector",
          "one.x1f.sip.foundation.core.declarative.connector.OutboundConnectorDefinition",
          "IntegrationScenario",
          "one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition",
          "CompositeProcess",
          "one.x1f.sip.foundation.core.declarative.process.CompositeProcessDefinition",
          "ConnectorGroup",
          "one.x1f.sip.foundation.core.declarative.connectorgroup.ConnectorGroupDefinition");

  private final Map<String, String> expectedAnnotationClasses =
      Map.of(
          "one.x1f.sip.foundation.core.declarative.connector.InboundConnectorDefinition",
          "InboundConnector",
          "one.x1f.sip.foundation.core.declarative.connector.OutboundConnectorDefinition",
          "OutboundConnector",
          "one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition",
          "IntegrationScenario",
          "one.x1f.sip.foundation.core.declarative.process.CompositeProcessDefinition",
          "CompositeProcess",
          "one.x1f.sip.foundation.core.declarative.connectorgroup.ConnectorGroupDefinition",
          "ConnectorGroup");

  public ClassAnalysisResult doAnalysis(ClassOrInterfaceDeclaration clazz) {
    var annotations = clazz.getAnnotations();
    var resolved = clazz.resolve();
    List<ResolvedReferenceType> ancestors = new ArrayList<>();
    resolved.getAncestors().forEach(ancestor -> addResolvableAncestor(ancestor, ancestors));

    return analyseDeclarativeStructure(resolved, annotations, ancestors);
  }

  private void addResolvableAncestor(
      ResolvedReferenceType current, List<ResolvedReferenceType> ancestors) {
    try {
      String name = current.getQualifiedName();

      if (!name.startsWith(X1F_SIP_GROUP)) return;

      ancestors.add(current);
      current
          .getTypeDeclaration()
          .ifPresent(
              typeDeclaration ->
                  typeDeclaration
                      .getAncestors()
                      .forEach(ancestor -> addResolvableAncestor(ancestor, ancestors)));
    } catch (Exception e) {
      // skip because non resolvable
    }
  }

  private ClassAnalysisResult analyseDeclarativeStructure(
      ResolvedReferenceTypeDeclaration resolved,
      NodeList<AnnotationExpr> annotations,
      List<ResolvedReferenceType> ancestors) {
    List<String> annotationNames = annotations.stream().map(NodeWithName::getNameAsString).toList();
    ClassAnalysisResult result = analyseAnnotations(resolved, ancestors, annotationNames);
    if (result != null) return result;
    result = analyseAncestors(resolved, ancestors, annotationNames);
    if (result != null) return result;

    return new ClassAnalysisResult(false, resolved.getQualifiedName());
  }

  private ClassAnalysisResult analyseAncestors(
      ResolvedReferenceTypeDeclaration resolved,
      List<ResolvedReferenceType> ancestors,
      List<String> annotationNames) {
    List<String> invalidAncestorMatches =
        ancestors.stream()
            .map(ResolvedReferenceType::getQualifiedName)
            .filter(expectedAnnotationClasses::containsKey)
            .filter(
                ancestor -> !doesImplementedClassHaveMatchingAnnotation(annotationNames, ancestor))
            .toList();
    if (!invalidAncestorMatches.isEmpty()) {
      String ancestorName = invalidAncestorMatches.get(0);
      String second =
          String.format(
              "Class %s which implements %s must be annotated with @%s",
              resolved.getQualifiedName(),
              ancestorName,
              expectedAnnotationClasses.get(ancestorName));
      return new ClassAnalysisResult(true, second);
    }
    return null;
  }

  private ClassAnalysisResult analyseAnnotations(
      ResolvedReferenceTypeDeclaration resolved,
      List<ResolvedReferenceType> ancestors,
      List<String> annotationNames) {
    List<String> annotationMatches =
        annotationNames.stream().filter(expectedParentInterfaces::containsKey).toList();
    if (!annotationMatches.isEmpty()) {
      String annotationName = annotationMatches.get(0);
      if (!doesAnnotatedClassHaveMatchingParent(ancestors, annotationName)) {
        String message =
            String.format(
                "Class %s annotated with @%s must implement %s",
                resolved.getQualifiedName(),
                annotationName,
                expectedParentInterfaces.get(annotationName));
        return new ClassAnalysisResult(true, message);
      }
    }
    return null;
  }

  private boolean doesAnnotatedClassHaveMatchingParent(
      List<ResolvedReferenceType> ancestors, String annotationName) {
    return ancestors.stream()
        .anyMatch(i -> i.getQualifiedName().equals(expectedParentInterfaces.get(annotationName)));
  }

  private boolean doesImplementedClassHaveMatchingAnnotation(
      List<String> annotations, String interfaceName) {
    return annotations.stream()
        .anyMatch(
            annotationName -> annotationName.equals(expectedAnnotationClasses.get(interfaceName)));
  }
}
