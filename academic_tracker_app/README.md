# Academic Tracker

Select a module row to load its name, credits and mark into the form.
Choose **Update Selected** to save changes or **Delete Selected** to delete it after confirmation.
Choose **New / Clear** to cancel editing and add another module. The weighted average updates after every change.

## Windows desktop app

Double-click **Install-Desktop.cmd** after building the Windows image. This installs the app for your Windows account and creates **Academic Tracker** shortcuts on your desktop and Start menu. No administrator access, IntelliJ or separately installed Java is needed to run the packaged app. Keep the whole app directory together; the EXE relies on its bundled `app` and `runtime` folders.

The installer copies your original project `grades.db` on first installation using SQLite's backup operation. The original stays intact. Current grades are stored in `%LOCALAPPDATA%\Academic Tracker\grades.db`, separate from program files. Future installs and IntelliJ runs use this same location and never overwrite an existing database. Back up this file while the app is closed. Old launchers in the legacy `installer` folder still use the old database; use the new desktop shortcut.

If you previously used another copy of `grades.db` outside this project, keep that copy; it is not automatically merged.

## Build or update

Requires Windows, JDK 25 (`JAVA_HOME`) and Maven (the included wrapper downloads Maven if needed):

```powershell
.\mvnw.cmd clean verify
.\scripts\Build-Windows.ps1 -SkipBuild
.\scripts\Install-Desktop.ps1
```

Or run `.\scripts\Build-Windows.ps1` to build, test and package in one step.
Close the old app before installing an update. Program versions are retained under `%LOCALAPPDATA%\Programs\Academic Tracker`; the newest desktop shortcut points to the latest installation.
The path of the most recently built self-contained image is saved in `target\windows\latest-path.txt`.
Packaging uses the JDK's [jpackage tool](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jpackage.html).
`Install-Desktop.cmd` can be run again to recreate the shortcuts.

For development: `.\mvnw.cmd javafx:run`. For isolated testing, set the Java system property `academic.tracker.database` to a temporary database path; this disables automatic migration.

To remove the desktop version, close it, remove its desktop/Start menu shortcuts and the `%LOCALAPPDATA%\Programs\Academic Tracker` folder. Grades remain in `%LOCALAPPDATA%\Academic Tracker`.
