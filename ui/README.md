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
Since SWT-related pentaho libraries were modified, please replace these with the modified ones.

```bash
$ ant resolve
$ cp ${path_to_lib}/org.eclipse.rap.filedialog-3.1.0.jar lib/
$ cp ${path_to_lib}/org.eclipse.rap.jface-3.1.0.jar lib/
$ cp ${path_to_lib}/org.eclipse.rap.rwt-3.1.0.jar lib/
$ cp ${path_to_lib}/pentaho-xul-swt-6.1.0.5-231.jar lib/
```

##  Build

You will get a WAR file, which can be deployed onto Tomcat, in dist/ folder.

```bash
$ ant war
```

# Import the project into Eclipse

Resolve dependencies and create a classpath file.

```bash
$ ant create-dot-classpath
```

Copy resources.

```bash
$ ant compile.res_copy
```

Change output directory from "bin" to "bin/classes" to align with the Ant compile task.


```bash
$ sed -i "" "s/bin/bin\/classes/" .classpath
```

Finally import the project (pentaho-kettle/ui) into Eclipse.

# How to use

## Repository

The steps to connect to the Pentaho Repository is described [here](https://help.pentaho.com/Documentation/6.1/0J0/0C0/015).
However, webSpoon currently doesn't have the menubar, hence the repository dialog cannot be opened from the menu.
Luckily there is another way to open the dialog: enabling the "Show this dialog at startup" in the .spoonrc as follows:

```
ShowRepositoriesAtStartup=Y
```

A progress bar will appear when clicking a OK button after selecting a connection, typing username and password.
Please remember to move the progress bar, otherwise it will not close.

One can also automatically login to a repository by setting environment variables.
The details are described [here](http://wiki.pentaho.com/display/EAI/.01+Introduction+to+Spoon#.01IntroductiontoSpoon-Repository).
