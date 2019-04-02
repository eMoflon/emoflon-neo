# emoflon-neo
A Neo4j-based implementation of eMoflon

# Steps required to run the development workspace:
1. Download the latest Eclipse IDE for Java and DSL Developers from https://www.eclipse.org/downloads/packages/
2. Unzip and store the Eclipse package on your system
3. Start Eclipse and install PlantUML (Select Help -> Install new Software). Use http://hallvard.github.io/plantuml/ as the download site.
4. To display PlantUML in your window perspective, select Window -> Show view -> other -> PlantUML
5. Download the latest project source file projectSetDev.psf 
in the emoflon-neo package
6. Import the project by selecting the projectSetDev.psf in File -> Import -> Team -> Team-Project or use the direct URL https://raw.githubusercontent.com/eMoflon/emoflon-neo/master/projectSetDev.psf?token=AGAsSYoiKYaJq6RkppgjN9Gax9xcceuIks5cmidOwA%3D%3D
7. Check text-file encoding. Set it to UTF-8. (Window->Preference->General->Workspace)
8. Execute all *.mwe2 files. Located at emoflon-neo/org.emoflon.neo.emsl/src/org/emoflon/neo/emsl/

In case of errors in your repository: check your installed Java SDK (Java 1.8 is not supported) and install the latest Java SDK (version 1.10 / 1.11 and higher). Make sure Eclipse is running with the correct Java version and set the default Java Compiler in all projects to the latest version.

9. Start a runtime Eclipse workspace (choose any project and click "Run As/Eclipse Application")
10. In your runtime workspace import projectSetRuntime.psf or use the direct URL https://raw.githubusercontent.com/eMoflon/emoflon-neo/master/projectSetRuntime.psf?token=AGAsSVS5kj7OtfWyLFEBPnf46nKnhcizks5crKcvwA%3D%3D
