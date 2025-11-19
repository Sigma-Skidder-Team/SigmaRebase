# Sigma Rebase

> [!IMPORTANT]
> Sigma Rebase is in development.

## Instructions

### Base steps

1. Clone this repo and open it in IntelliJ IDEA
2. Set up the project (libraries, natives)
3. Open Project Structure with CTRL + Alt + Shift + S, we need to mark some directories
4. Find the "Modules" tab and click `SigmaRebase` (this project's name)
5. Open the "Sources" tab within the `SigmaRebase` module
6. Expand the "src" directory
7. Expand the "main" directory
8. Mark the `java` directory as `Sources` (you need to mark so IntelliJ knows where to look and what to compile).
9. Mark the `res` directory as `Resources`
   (you need to mark this so IntelliJ includes this and Minecraft won't crash when trying to look for assets).

### I want to run Sigma Rebase within IntelliJ IDEA

> [!NOTE]
> You don't need to do this, since commit 295ffb83759f4163d04b40c5894941dc060a5a17
> introduced a run configuration in `.run/Sigma Rebase.run.xml`

1. Press CTRL + SHIFT + A to open the Actions menu.
2. Search for "Edit configurations" and press enter / click on the result in the "Run" category
3. Press INSERT or click "Add new..."
4. Find "Application" and press ENTER or click it
5. Set the main class to "Start" in the default class
6. **OPTIONAL**: Give the application a name (e.g. "Sigma Rebase" / "Start Client")
7. **OPTIONAL**: Enable or disable "Store as project file"
8. **OPTIONAL**: make some other tweaks if you want to.
9. Now that you've configured the Application, you can run it.
   1. Shift + F9 for debugging (if you want hot reload)
   2. Shift + F10 for running (no hot reload)

### I want a runnable JAR for the Minecraft launcher!

> [!IMPORTANT]
> This seems to be broken for now.
> Hopefully I can fix it later.

1. Open the project structure with Ctrl + Alt + Shift + S;
2. Find and click on the Artifacts tab;
3. Press + -> JAR -> Empty;
4. In the Available Elements tab, right-click on Libraries -> Extract into Output Root;
5. In the same tab, right-click on your compile output icon -> Put into Output Root
6. Give your artifact a name (optional);
7. Close the project structure window and find the Build tab -> Build artifacts and double-click on your created artifact;
This jar will be then compiled in your project's directory/out/artifacts/artifact name/artifact name.jar

## Contributors

- [DataModel](https://github.com/DataM0del/)
- [Mark](https://github.com/MarkGG8181)
- [Away](https://github.com/AwayXD)
- [Gato](https://github.com/gatov2)
- [lamzvam](https://github.com/lamzvam)
- [Graph](https://github.com/ccfeeX)
- [StormingMoon](https://github.com/StormingMoon)
- [Richy](https://github.com/richylotl)
