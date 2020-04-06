# emoflon-neo
A Neo4j-based implementation of eMoflon

[![codebeat badge](https://codebeat.co/badges/3c0e1804-9cfb-4551-ac76-6456a2e06c6d)](https://codebeat.co/projects/github-com-emoflon-emoflon-neo-master)

# Steps required to run the development workspace:
- Download the latest Eclipse IDE for *Java and DSL* Developers from https://www.eclipse.org/downloads/packages/  (make sure you choose the correct Eclipse package: *Java and DSL*!)
- Start Eclipse and install PlantUML (Select Help -> Install new Software). Use http://hallvard.github.io/plantuml/ as the download site.
- To display PlantUML in the perspective of your choice (we recommend Java), select Window -> Show view -> other -> PlantUML
- Import all dev workspace projects via -> Import -> Team -> Team-Project, using this URL to access the relevant project set file: https://github.com/eMoflon/emoflon-neo/raw/master/projectSetDev.psf
- Check the text-file encoding of your workspace. Make sure it is set to UTF-8 (Window->Preference->General->Workspace).
- Execute all \*.mwe2 files. Located at `emoflon-neo/org.emoflon.neo.emsl/src/org/emoflon/neo/emsl/`
  * If you cannot do this then you probably installed the wrong Eclipse package.
  * In case of errors in your repository: check your installed Java SDK and install the Java SDK, version 1.12 (higher versions might work, but are not tested yet). Make sure Eclipse is running with the correct Java version (including the JDK Compliance for each project, accessable via -> Project -> Properties -> Java compiler). 
- Install Neo4j from https://neo4j.com/download/ and establish a connection to eMoflon:
  * Start Neo4j Desktop and create a new database (Add Graph -> Create a local graph -> Set Password -> Create)
  * Within Neo4j Desktop, start the database and wait until it is marked as "Active". Start Neo4j Browser.
  * Copy username and connection URI
- Start a runtime Eclipse workspace (choose any project and click "Run As/Eclipse Application")
- In your runtime workspace import projectSetRuntime.psf or use the direct URL https://github.com/eMoflon/emoflon-neo/raw/master/projectSetRuntime.psf
- Set up the connection to Neo4j via -> Window -> Preferences -> eMoflon::Neo -> Neo4J Preferences by entering the copied Connection URI and User and the assigned Password.
