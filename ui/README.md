# Web-based Spoon (aka webSpoon)

This is an attempt to run Spoon as a web app.
Spoon relies on SWT for UI widgets, which is great for being OS agnostic, but it only runs as a desktop app.
RWT provides web UIs with SWT API, so replacing SWT with RWT allows Spoon to run as a web app with a little code change.
Having said that, some APIs are not implemented; hence, a little more code change is required than it sounds.

These are the major changes so far:

- Add org.pentaho.di.ui.spoon.BasicApplication, which configures web app.
- Modify ui/ivy.xml in order to add RWT-related dependencies and remove SWT.
- Many comment-outs/deletions to avoid compile errors due to RWT/SWT difference.

## Coding philosophy

1. Minimize the difference from the original Spoon.
2. Decide RWT or webSpoon to be modified so that the change can be minimized.

# Compiling

## Git clone

Since the repository is heavy, it is recommened to clone only the latest commit of the branch.

```bash
$ git clone -b webspoon --depth 1 http://${path_to_repo}.git 
$ cd pentaho-kettle/ui/
```

## Resolve dependencies

After downloading depended libraries, some jar files for RAP/RWT should be replaced with the modified version.

```bash
$ ant resolve
$ cp ${path_to_lib}/org.eclipse.rap.filedialog-3.1.0.jar lib/
$ cp ${path_to_lib}/org.eclipse.rap.jface-3.1.0.jar lib/
$ cp ${path_to_lib}/org.eclipse.rap.rwt-3.1.0.jar lib/
```

##  Build

You will get a WAR file, which can be deployed onto Tomcat, in dist/ folder.

```bash
$ ant war
```
