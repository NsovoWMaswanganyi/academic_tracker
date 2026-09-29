# Academic Tracker

Select a module row to load its name, credits and mark into the form.
Choose **Update Selected** to save changes or **Delete Selected** to delete it after confirmation.
Choose **New / Clear** to cancel editing and add another module. The weighted average updates after every change.

The **final-year weighted average** uses only modules marked **Final year**, weighted by their credits. On upgrade, the existing ten modules beginning with **Financial Management** (in original entry order) are marked automatically. The selection is saved in the database, so sorting, renaming, adding or deleting rows does not silently substitute another module. Select a row, change **Final-year module**, and click **Update Selected** to adjust membership. Both averages update after saved changes; the final-year summary shows how many modules and credits are included. With no final-year modules selected, the app shows a dash rather than a misleading 0%.

Before upgrading a populated database, the app saves a `grades-before-final-year-*.db` backup next to it. Existing module names, credits and marks are preserved.

## Windows desktop app

Double-click **Install-Desktop.cmd** after building the Windows image. This installs the app for your Windows account and creates **Academic Tracker** shortcuts on your desktop and Start menu. No administrator access, IntelliJ or separately installed Java is needed to run the packaged app. Keep the whole app directory together; the EXE relies on its bundled `app` and `runtime` folders.

The installer copies your original project `grades.db` on first installation using SQLite's backup operation. The original stays intact. Current grades are stored in `%LOCALAPPDATA%\Academic Tracker\grades.db`, separate from program files. Future installs and IntelliJ runs use this same location and never overwrite an existing database. Back up this file while the app is closed. Old launchers in the legacy `installer` folder still use the old database; use the new desktop shortcut.

If you previously used another copy of `grades.db` outside this project, keep that copy; it is not automatically merged.

The per-user installer also keeps a private `app\first-run-grades.db` snapshot in the installed program folder. The desktop launcher uses that snapshot only when its data file does not yet exist, so first-launch migration happens in the user's actual launch context. An existing database, including an intentionally empty one, is never replaced. The snapshot contains your grades; do not share your installed program folder. The build image under `target\windows` does not contain your grade data.

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
