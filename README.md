# Web-based Spoon (aka webSpoon)

This is an attempt to run Spoon as a web app.
You can create and run Kettle transformation/job files from your favorite web browser.

## Use cases

### PDI on-the-go

- Access from smartphone/tablet

### Security

- Keep sensitive data where they should be

### No installation

- Kick-start hands-on
- Be nice to thin clients

### Seamless user experience

- Edit Kettle queries in CDE
- Edit Kettle endpoints in App Builder (aka Sparkl)

# How to deploy & config (admin perspective)

## Deploy

The following procedures assume that Pentaho BI server (CE or EE) has already been installed and webSpoon is deployed to the Apache Tomcat shipped with them, but webSpoon can also be deployed to other Java Application server (e.g., Jetty).

1. Download the latest `spoon.war` from [here](https://github.com/HiromuHota/pentaho-kettle/releases).
2. Copy the downloaded `spoon.war` to the `webapps` folder, e.g., `biserver-ce/tomcat/webapps` for CE, `Pentaho/server/biserver-ee/tomcat/webapps/` for EE.
3. (Re)start the BI server.

WebSpoon will sit next to the Pentaho User Console (i.e., `http://address:8080/spoon` when PUC is `http://address:8080/pentaho`).

## Config

It is strongly recommended to use webSpoon with a Repository (can be Pentaho Repository, Kettle Database Repository, or Kettle File Repository), otherwise opening/saving files does not function as you would expect.
The steps to connect to a Repository is described [here](https://help.pentaho.com/Documentation/6.1/0J0/0C0/015) and [here](https://help.pentaho.com/Documentation/6.1/0L0/0Y0/040).
Since webSpoon currently doesn't have the menubar, the repository dialog cannot be opened from the menu.
Luckily there is another way to open the dialog: enabling the "Show this dialog at startup" in the `$HOME/.kettle/.spoonrc` (if `user` runs the Java Application server, `$HOME` is `/home/user`) as follows:

```
ShowRepositoriesAtStartup=Y
```

A progress bar will appear when clicking a OK button after selecting a connection, typing username and password.
Please remember to move the progress bar, otherwise it will not close.

One can also automatically login to a repository by setting environment variables.
The details are described [here](http://wiki.pentaho.com/display/EAI/.01+Introduction+to+Spoon#.01IntroductiontoSpoon-Repository).

# How to use (end-user perspective)

The RAP (the underlying technology for webSpoon) supports most of the latest web browsers.
webSpoon can also be used from smartphone/tablet.
The detailed compatibility is described [here](http://www.eclipse.org/rap/noteworthy/3.0/).

## Known issues

- MenuBar is missing.
- Welcome page does not show anything.
- Icons in Job are not drawn in Canvas.
- "Run" button does not function once another person starts using webSpoon (not really multi-user friendly).
- App area is not always aligned with the browser window size.
- Moving a note causes an error sometimes.
- "Please Wait" dialog after the "Repository Connection" dialog won't close until it is manually moved or closed.
- "Run Options" dialog after the "Run" button won't popup until the canvas area is clicked.

# How to develop (developer perspective)

Spoon relies on SWT for UI widgets, which is great for being OS agnostic, but it only runs as a desktop app.
RAP/RWT provides web UIs with SWT API, so replacing SWT with RAP/RWT allows Spoon to run as a web app with a little code change.
Having said that, some APIs are not implemented; hence, a little more code change is required than it sounds.

## Coding philosophy

1. Minimize the difference from the original Spoon.
2. Decide RWT or webSpoon to be modified so that the change can be minimized.

These are the major changes so far:

- Add org.pentaho.di.ui.spoon.BasicApplication, which configures web app.
- Modify ui/ivy.xml in order to add RWT-related dependencies and remove SWT.
- Many comment-outs/deletions to avoid compile errors due to RWT/SWT difference.

## Build in the command line

Since the repository is heavy, it is recommened to clone only the latest commit of the branch.

```bash
$ git clone -b webspoon --depth 1 https://github.com/HiromuHota/pentaho-kettle.git
```
Change the directory

```bash
$ cd pentaho-kettle/ui/
```

Resolve dependencies

```bash
$ ant resolve
```

Replace some of the dependent libraries with patched one.
The source code of patched libraries can be downloaded from [rap](https://github.com/HiromuHota/rap) and [pentaho-commons-xul](https://github.com/HiromuHota/pentaho-commons-xul).

```bash
$ cp ${path_to_lib}/org.eclipse.rap.filedialog-3.1.0.jar lib/
$ cp ${path_to_lib}/org.eclipse.rap.jface-3.1.0.jar lib/
$ cp ${path_to_lib}/org.eclipse.rap.rwt-3.1.0.jar lib/
$ cp ${path_to_lib}/pentaho-xul-swt-6.1.0.5-231.jar lib/
```

Build and you will get a WAR file in `dist` folder, which can be deployed to the Java Application server of your choice.

```bash
$ ant war
```

## Develop in Eclipse

Resolve dependencies, create a classpath file, and copy resources

```bash
$ ant create-dot-classpath compile.res_copy
```

Change output directory from `bin` to `bin/classes` to align with the Ant compile task.


```bash
$ sed -i "" "s/bin/bin\/classes/" .classpath
```

Finally import the project (pentaho-kettle/ui) into Eclipse.