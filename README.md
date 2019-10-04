# emoflon-neo
A Neo4j-based implementation of eMoflon

[![codebeat badge](https://codebeat.co/badges/3c0e1804-9cfb-4551-ac76-6456a2e06c6d)](https://codebeat.co/projects/github-com-emoflon-emoflon-neo-master)

# Steps required to run the development workspace:
- Download the latest Eclipse IDE for *Java and DSL* Developers from https://www.eclipse.org/downloads/packages/  (make sure you choose the correct Eclipse package: *Java and DSL*!)
- Start Eclipse and install PlantUML (Select Help -> Install new Software). Use http://hallvard.github.io/plantuml/ as the download site.
- To display PlantUML in the perspective of your choice (we recommend Java), select Window -> Show view -> other -> PlantUML
- Import all dev workspace projects via -> Import -> Team -> Team-Project, using this URL to access the relevant project set file: https://github.com/eMoflon/emoflon-neo/raw/master/projectSetDev.psf
- Check the text-file encoding of your workspace. Make sure it is set to UTF-8 (Window->Preference->General->Workspace).
- Execute all \*.mwe2 files. Located at emoflon-neo/org.emoflon.neo.emsl/src/org/emoflon/neo/emsl/  
  * If you cannot do this then you probably installed the wrong Eclipse package.
  * In case of errors in your repository: check your installed Java SDK (Java 1.8 is not supported) and install the latest Java SDK (version 1.10 / 1.11 and higher). Make sure Eclipse is running with the correct Java version.
- Start a runtime Eclipse workspace (choose any project and click "Run As/Eclipse Application")
- In your runtime workspace import projectSetRuntime.psf or use the direct URL https://github.com/eMoflon/emoflon-neo/raw/master/projectSetRuntime.psf
