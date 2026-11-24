# Building Plus Ultra into a .jar

Requirements: JDK 21 and an internet connection. Nothing else - the Gradle wrapper is included.

    ./gradlew build          (Windows: gradlew.bat build)

Output: build/libs/plusultra-0.1.0.jar -> copy into your .minecraft/mods folder
(NeoForge 1.21.1, version 21.1.172 or newer).

No local tools? Push this folder to a GitHub repo; the included workflow
(.github/workflows/build.yml) builds the jar and attaches it under Actions > latest run > Artifacts.

To test without installing: ./gradlew runClient
