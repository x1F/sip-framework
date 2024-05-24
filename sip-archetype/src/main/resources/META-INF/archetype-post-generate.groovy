import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files
import java.io.File

// the path where the project got generated
Path projectPath = Paths.get(request.outputDirectory, request.artifactId)

// the properties available to the archetype
Properties properties = request.getProperties()
String generateOptionalFile = properties.get("createDemoAdapter")

String packageName = properties.get("package")
String projectName = properties.get("artifactId")
String connectorGroup1 = properties.get("connectorGroup1")
String connectorGroup2 = properties.get("connectorGroup2")
String basePackage = packageName.replace(".", "/")

String packagePath = projectName + "/src/main/java/" + basePackage
def projectRoot = new File(".").canonicalFile

def filePaths = [packagePath + "/connectorgroups/" + connectorGroup1 + "/connectors",
                 packagePath + "/connectorgroups/" + connectorGroup1 + "/models",
                 packagePath + "/connectorgroups/" + connectorGroup2 + "/connectors",
                 packagePath + "/scenarios/definitions",
                 packagePath + "/scenarios/processes",
                 packagePath + "/scenarios/models",
                 packagePath + "/config"
]
def modelDirs = [packagePath + "/scenarios/models/common",
                 packagePath + "/scenarios/models/response",
                 packagePath + "/scenarios/models/laureate",
                 packagePath + "/scenarios/models/nobelprize"
]
if (generateOptionalFile != "y" && generateOptionalFile != "Y") {
    filePaths.each { element ->
        def sourceDir = new File(projectRoot, element)
        if (sourceDir.exists()) {
            def files = sourceDir.listFiles()
            // Iterate over each file and delete it
            files.each { file ->
                if (file.isFile() && !file.getName().contains(".keep")) {
                    file.delete()
                }
            }
        }
    }
    modelDirs.each { element ->
        def sourceDir = new File(projectRoot, element)
        if (sourceDir.exists()) {
            def files = sourceDir.listFiles()
            // Iterate over each file and delete it
            files.each { file ->
                if (file.isFile() && !file.getName().contains(".keep")) {
                    file.delete()
                }
            }
            sourceDir.delete()
        }
    }
    Files.deleteIfExists projectPath.resolve("src/test/java/" + basePackage + "/TestKitTest.java")
}
else {
    filePaths.each { element ->
        def sourceDir = new File(projectRoot, element)
        if (sourceDir.exists()) {
            def files = sourceDir.listFiles()
            // Iterate over each file and delete it
            files.each { file ->
                if (file.isFile() && file.getName().contains(".keep")) {
                    file.delete()
                }
            }
        }
    }
    modelDirs.each { element ->
        def sourceDir = new File(projectRoot, element)
        if (sourceDir.exists()) {
            def files = sourceDir.listFiles()
            // Iterate over each file and delete it
            files.each { file ->
                if (file.isFile() && file.getName().contains(".keep")) {
                    file.delete()
                }
            }
        }
    }
}