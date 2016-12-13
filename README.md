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

# How to use (end-user perspective)

Please refer to the [wiki](https://github.com/HiromuHota/pentaho-kettle/wiki) and [issues](https://github.com/HiromuHota/pentaho-kettle/issues).

# How to deploy & config (admin perspective)

## Compatibiliy

Here is a list of versions of Java and OS that were verified to be compatible with webSpoon.
webSpoon does not work with Java 7 since the dependent library (Eclipse RAP/RWT) is not compatible with Java 7 (see [here](http://www.eclipse.org/rap/noteworthy/3.1/) for the details).

### Java

- Oracle Java SE 8
- OpenJDK 8

### Operating systems

- Ubuntu 14.04
- Mac OS X El Capitan
- Windows 7 Professional
- Windows 10 Professional

## Deploy

The following procedures assume that Pentaho BI server (CE or EE) has already been installed and webSpoon is deployed to the Apache Tomcat shipped with them, but webSpoon can also be deployed to other Java Application server (e.g., Jetty).

1. Download the latest `spoon.war` from [here](https://github.com/HiromuHota/pentaho-kettle/releases).
2. Copy the downloaded `spoon.war` to the `webapps` folder, e.g., `biserver-ce/tomcat/webapps` for CE, `Pentaho/server/biserver-ee/tomcat/webapps` for EE.
3. (Re)start the BI server.

WebSpoon will sit next to the Pentaho User Console (i.e., `http://address:8080/spoon` when PUC is `http://address:8080/pentaho`).

## Config

It is strongly recommended to use webSpoon with a Repository (can be Pentaho Repository, Kettle Database Repository, or Kettle File Repository), otherwise opening/saving files does not function as you would expect.
The steps to connect to a Repository is described [here](https://help.pentaho.com/Documentation/6.1/0J0/0C0/015) and [here](https://help.pentaho.com/Documentation/6.1/0L0/0Y0/040).

A progress bar will appear when clicking a OK button after selecting a connection, typing username and password.
Please remember to move the progress bar, otherwise it will not close.

One can also automatically login to a repository by setting environment variables.
The details are described [here](http://wiki.pentaho.com/display/EAI/.01+Introduction+to+Spoon#.01IntroductiontoSpoon-Repository).

## Plugins

Place plugins into either one of the following two folders:

1. `biserver-ce/pentaho-solutions/system/kettle/plugins` for CE or `Pentaho/server/biserver-ee/pentaho-solutions/system/kettle/plugins` for EE.
2. `$HOME/.kettle/plugins`

## JDBC drivers

Place jar files into either one of the following folders:

1. `biserver-ce/tomcat/lib` for CE or `Pentaho/server/biserver-ee/tomcat/lib` for EE.
2. `webapps/spoon/WEB-INF/lib`, but not recommended because this folder is overwritten when upgrading `spoon.war`.

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
- Make singleton objects (e.g., `PropsUI`, `GUIResource`) "session aware" (see [here](http://www.eclipse.org/rap/developers-guide/devguide.php?topic=singletons.html) for the details).

## Branches and Versioning

I started this project in the webspoon branch, branched off from the branch 6.1 of between 6.1.0.5-R and 6.1.0.6-R.
Soon I realized that I should have branched off from one of released versions.
So I decided to make two branches: webspoon-6.1 and webspoon-7.0, each of which was rebased onto 6.1.0.1-R and 7.0.0.0-R, respectively.
I made the branch webspoon-6.1 as the default one for this git repository as the branch webspoon-7.0 currently cannot use the marketplace plugin.

webSpoon uses 4 digits versioning with the following rules:

- The 1st digit is always 0 (never be released as a separate software).
- The 2nd and 3rd digits represent the base Kettle version, e.g., 6.1, 7.0.
- The last digit represents the minor revisions within webSpoon.

As a result, the next (pre-)release version will be 0.6.1.4, meaning it is from the branch webspoon-6.1 and the 4th minor revision.

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
## Testing

`TestContext` has been added to some test cases to simulate the environment that RAP UI code normally runs. `TestContext` is in the bundle org.eclipse.rap.rwt.testfixture, which is not hosted by the Maven Repository. So please download it from [here](https://github.com/HiromuHota/rap/releases/download/webspoon%2F0.0.0.1/org.eclipse.rap.rwt.testfixture-3.1.1-SNAPSHOT.jar) and copy it to the `test-lib` directory.

```
$ ant resolve
$ cp ${path_to_lib}/org.eclipse.rap.rwt.testfixture-3.1.1-SNAPSHOT.jar test-lib/
$ ant test
```

## Develop in Eclipse IDE

It is recommened to install the RAP Tools to your Eclipse IDE.
Please refer to the [developer's guide for RAP](http://www.eclipse.org/rap/developers-guide/) for how to install.
Once installed, follow these instructions.

Resolve dependencies, create a classpath file, and copy resources

```bash
$ ant create-dot-classpath compile.res_copy
```

Change output directory from `bin` to `bin/classes` to align with the Ant compile task.


```bash
$ sed -i "" "s/bin/bin\/classes/" .classpath
```

Finally import the project (pentaho-kettle/ui) into Eclipse IDE.

# Notices

- Pentaho is a registered trademark of Pentaho, Inc.
- Oracle and Java are registered trademarks of Oracle and/or its affiliates.
- Ubuntu is a registered trademark of Canonical Ltd.
- Mac and OS X are trademarks of Apple Inc., registered in the U.S. and other countries.
- Windows is a registered trademark of Microsoft Corporation in the U.S. and other countries.
- Eclipse is a registered trademark of the Eclipse Foundation, Inc. in the US and/or other countries.
- Other company and product names mentioned in this document may be the trademarks of their respective owners.