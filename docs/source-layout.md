# Source folders

The source root is now `src/main`. The folders that were inside
`src/main/java/com/ivofade/findthed` were moved directly into it:

```text
src/main/
    Main.java
    enums/
    exception/
    model/
    repository/
    service/
    ui/
    util/
lib/
```

Packages and imports now match these simple folders: `package model;`,
`import model.Dorm;`, `package ui.landlord;`, and so on. The package/import
changes to teammate files are mechanical; their behavior was not redesigned.

From the project root, compile with:

```powershell
powershell -ExecutionPolicy Bypass -File build.ps1
```

This calls plain `javac`, includes the JARs in `lib`, and writes classes to
ignored `out`. No Maven or Gradle is required. The existing Main/UI placeholders
still need their frontend implementation; this change does not build screens.

In VS Code's personal settings, replace the old Java source paths with:

```json
"java.project.sourcePaths": ["src/main"],
"java.project.referencedLibraries": ["lib/**/*.jar"]
```

Run Java: Clean Java Language Server Workspace if VS Code retains old packages.
Tests and stubs were removed/omitted by the team's explicit decision. The old
untracked BackendCoreCheck source was backed up under ignored
`target/before-simple-layout-20261109` before removing src/test.

Full compilation is currently blocked by pre-existing teammate errors that
Dylan explicitly requested we leave unchanged. See `docs/landlord-guide.md`.
