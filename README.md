# Web-based Spoon (aka webSpoon)

webSpoon is a web-based graphical designer for Pentaho Data Integration with the same look & feel as Spoon.
Kettle transformation/job files can be designed and executed in your favorite web browser.
This is one of the community activities and not supported by Pentaho.

## Use cases

### Data security

- Keep sensitive data where they should be

### Managed Pentaho development environment

- Accessible from any network-connected devices (including thin clients, smartphones/tablets)
- No installation/upgrade/update required (by end-users)
- No undesired plugins or JDBC drivers installed (by end-users)
- Same version, plugins, drivers, etc. among a team (my Kettle files run in your screen without an error)

### Embedded data integration

- Edit Kettle queries in CDE
- Edit Kettle endpoints in App Builder (aka Sparkl)

# How to use

Please refer to the [wiki](https://github.com/HiromuHota/pentaho-kettle/wiki) and [issues](https://github.com/HiromuHota/pentaho-kettle/issues).

# How to deploy with Docker (pre-configured)

The following command gives you webSpoon without plugins:

```
$ docker run -e JAVA_OPTS="-Xms1024m -Xmx2048m" -d -p 8080:8080 hiromuhota/webspoon:latest
```

The following command gives you webSpoon with all the plugins included in the CE distribution:

```
$ docker run -e JAVA_OPTS="-Xms1024m -Xmx2048m" -d -p 8080:8080 -p 9051:9051 hiromuhota/webspoon:latest-full
```

In either way, access `http://address:8080/spoon/spoon` with a browser.

# How to deploy & config

## System requirements

Please refer to the [wiki](https://github.com/HiromuHota/pentaho-kettle/wiki/System-Requirements).

## Deploy

### Deploy to (bare) Tomcat

1. Download the latest `spoon.war` from [here](https://github.com/HiromuHota/pentaho-kettle/releases).
2. Copy the downloaded `spoon.war` to `tomcat/webapps/spoon.war`.
3. (Optional) download and unzip `pdi-ce-7.0.0.0-25.zip`, then copy the `system` and `plugins` folders to `tomcat/system` and `tomcat/plugins`, respectively.
4. (Optional) configure Apache Karaf as below.
5. (Re)start the Tomcat.
6. Access `http://address:8080/spoon/spoon`

### Deploy to Pentaho server

1. Download the latest `spoon.war` from [here](https://github.com/HiromuHota/pentaho-kettle/releases).
2. Copy the downloaded `spoon.war` to `pentaho-server/tomcat/webapps/spoon.war`.
3. (Re)start the Pentaho server.
4. Access `http://address:8080/spoon/spoon`

It is not recommended to place `system` and `plugins` folders along with the Pentaho server due to [#32](https://github.com/HiromuHota/pentaho-kettle/issues/32) and [#35](https://github.com/HiromuHota/pentaho-kettle/issues/35).

## Config

### User authentication

Edit `WEB-INF/web.xml` to uncomment/enable user authentication.

```
  <!-- Uncomment the followings to enable login page for webSpoon
  <context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>
      /WEB-INF/spring/*.xml
    </param-value>
  </context-param>

  <filter>
    <filter-name>springSecurityFilterChain</filter-name>
    <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
  </filter>
  <filter-mapping>
    <filter-name>springSecurityFilterChain</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>

  <listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
  </listener>
  -->
```

Edit `WEB-INF/spring/security.xml` to manage users.
The following example shows how to assign <i>user</i> with password of <i>password</i> to <i>USER</i> role.

```
<b:beans>
  <user-service>
    <user name="user" password="password" authorities="ROLE_USER" />
  </user-service>
</b:beans>
```

It would be possible to use LDAP as an authentication provider.
See [here](http://docs.spring.io/spring-security/site/docs/4.1.x/reference/html/ns-config.html) for more details.
webSpoon uses the same framework for user authentication: Spring Security, as Pentaho User Console.
Thus, it would also be possible to use Microsoft Active Directory as described in Pentaho's official documentation for [User Security](https://help.pentaho.com/Documentation/7.0/0P0/Setting_Up_User_Security).

### Repository

It is strongly recommended to use webSpoon with a Repository (can be Pentaho Repository, Kettle Database Repository, or Kettle File Repository), otherwise opening/saving files does not function as you would expect.
The steps to connect to a Repository is described [here](https://help.pentaho.com/Documentation/6.1/0J0/0C0/015) and [here](https://help.pentaho.com/Documentation/6.1/0L0/0Y0/040).

One can also automatically login to a repository by setting environment variables.
The details are described [here](http://wiki.pentaho.com/display/EAI/.01+Introduction+to+Spoon#.01IntroductiontoSpoon-Repository).

### (Optional) Apache Karaf

- Firewall / port forward

Please make sure that a client can access the OSGI Service Port of the server (e.g., 9051).
It is known that the marketplace does not work properly when the port is not accessible.
The port seems to be automatically assigned and sometimes changes, so it is needed to check which port is actually used.
The port information like below can be found in the Tomcat log: `tomcat/logs/catalina.out`.

```
*******************************************************************************
*** Karaf Instance Number: 1 at /XXX/biserver-ce/tomcat/bin/./syst ***
***   em/karaf/caches/webspoonservletcontextlistener/data-1                 ***
*** Karaf Port:8802                                                         ***
*** OSGI Service Port:9051                                                  ***
*******************************************************************************
```

## Plugins

A comma separated list of plugin folders can be defined by `KETTLE_PLUGIN_BASE_FOLDERS`.
If not defined, the following folders are used:

1. `$DI_HOME/plugins` (`$DI_HOME` is defined in `start-pentaho.{sh|bat}` and `pentaho-solutions/system/kettle` is the default)
2. `$HOME/.kettle/plugins`
3. `$CUR_DIR/plugins ` (the current folder: `$CUR_DIR` depends on how and where webSpoon is running, e.g., `biserver-ce/tomcat/bin` for CE and `Pentaho` for EE)

### Replace some plugins with patched ones

Some of the plugins are not compatible with webSpoon.
Please replace jar files with patched ones and delete the Karaf cache directory if necessary.
The patched jar files are [pdi-platform-utils-plugin](https://github.com/HiromuHota/pdi-platform-utils-plugin/releases), [big-data-plugin](https://github.com/HiromuHota/big-data-plugin/releases), [repositories-plugin](https://github.com/HiromuHota/pentaho-kettle/releases) (only 7.0.0.0-25 is supported).

```
$ cp ${path_to_lib}/pdi-platform-utils-plugin-7.0.0.0-25.jar plugins/platform-utils-plugin/
$ cp ${path_to_lib}/pentaho-big-data-legacy-7.0.0.0-25.jar plugins/pentaho-big-data-plugin/
$ cp ${path_to_lib}/pentaho-big-data-kettle-plugins-common-ui-7.0.0.0-25.jar system/karaf/system/pentaho/pentaho-big-data-kettle-plugins-common-ui/7.0.0.0-25/
$ cp ${path_to_lib}/repositories-plugin.jar system/karaf/system/pentaho-kettle/repositories-plugin/7.0.0.0-25/repositories-plugin-7.0.0.0-25.jar
$ rm -rf system/karaf/caches/webspoonservletcontextlistener
```

## JDBC drivers

Place jar files into either one of the following folders:

1. `biserver-ce/tomcat/lib` for CE or `Pentaho/server/biserver-ee/tomcat/lib` for EE.
2. `webapps/spoon/WEB-INF/lib`, but not recommended because this folder is overwritten when upgrading `spoon.war`.

# How to develop

Spoon relies on SWT for UI widgets, which is great for being OS agnostic, but it only runs as a desktop app.
RAP/RWT provides web UIs with SWT API, so replacing SWT with RAP/RWT allows Spoon to run as a web app with a little code change.
Having said that, some APIs are not implemented; hence, a little more code change is required than it sounds.

## Coding philosophy

1. Minimize the difference from the original Spoon.
2. Decide RWT or webSpoon to be modified so that the change can be minimized.

These are the major changes so far:

- Add org.pentaho.di.ui.spoon.WebSpoon, which configures web app.
- Modify ui/ivy.xml in order to add RWT-related dependencies and remove SWT.
- Many comment-outs/deletions to avoid compile errors due to RWT/SWT difference.
- Make singleton objects (e.g., `PropsUI`, `GUIResource`) session-unique (see [here](http://www.eclipse.org/rap/developers-guide/devguide.php?topic=singletons.html) for the details).

## Branches and Versioning

I started this project in the webspoon branch, branched off from the branch 6.1 of between 6.1.0.5-R and 6.1.0.6-R.
Soon I realized that I should have branched off from one of released versions.
So I decided to make two branches: webspoon-6.1 and webspoon-7.0, each of which was rebased onto 6.1.0.1-R and 7.0.0.0-R, respectively.
I made the branch webspoon-6.1 as the default one for this git repository as the branch webspoon-7.0 currently cannot use the marketplace plugin.

webSpoon uses 4 digits versioning with the following rules:

- The 1st digit is always 0 (never be released as a separate software).
- The 2nd and 3rd digits represent the base Kettle version, e.g., 6.1, 7.0.
- The last digit represents the patch version.

As a result, the next (pre-)release version will be 0.6.1.4, meaning it is based on the Kettle version 6.1 with the 4th patch.
There could be a version of 0.7.0.4, which is based on the Kettle version 7.0 with (basically) the same patch.

## Build and locally publish dependent libraries

Please build and locally-publish the following dependent libraries.

- pentaho-xul-swt
- org.eclipse.rap.rwt
- org.eclipse.rap.jface
- org.eclipse.rap.filedialog
- org.eclipse.rap.rwt.testfixture

### pentaho-commons-xul

```
$ git clone -b webspoon-7.0 https://github.com/HiromuHota/pentaho-commons-xul.git
$ cd pentaho-commons-xul/pentaho-xul-swt
$ ant clean-all resolve publish-local
```

### rap

```
$ git clone -b webspoon-3.1-maintenance https://github.com/HiromuHota/rap.git
$ cd rap
$ mvn clean install -N
$ mvn clean install -pl bundles/org.eclipse.rap.rwt -am
$ mvn clean install -pl bundles/org.eclipse.rap.jface -am
$ mvn clean install -pl bundles/org.eclipse.rap.filedialog -am
$ mvn clean install -pl tests/org.eclipse.rap.rwt.testfixture -am
```

## Build in the command line

**Make sure patched dependent libraries have been published locally.**

Build and locally publish `kettle-ui-swt-7.0.0.0-25-X.jar`, which will be copied to `~/.ivy2/local/pentaho-kettle/kettle-ui-swt/`

```bash
$ git clone -b webspoon-7.0 https://github.com/HiromuHota/pentaho-kettle.git
$ cd pentaho-kettle/ui/
$ ant clean-all resolve publish-local
```

Change directory and build a war file.
The published jar file will be picked up on the way.

```bash
$ cd pentaho-kettle/assembly
$ ant clean-all resolve war
```

## Testing

**Make sure patched dependent libraries have been published locally.**

```
$ cd pentaho-kettle/ui/
$ ant clean-all resolve test
```

### UI testing using Selenium

Currently, only Google Chrome browser has been tested for when running UI test cases.
The tests run in headless mode unless a parameter `-Dheadless.unittest=false` is passed.
To run tests in headless mode, the version of Chrome should be higher than 59 (only available in the beta channel as of writing).

The default url is `http://localhost:8080/spoon`.
Pass a parameter like below if webSpoon is deployed to a different url.

The following command runs all the unit test cases including UI in non-headless mode.

```
$ ant test -Dtest.baseurl=http://localhost:8080/spoon/spoon -Dheadless.unittest=false
```

## Develop in Eclipse IDE

It is recommened to install the RAP Tools to your Eclipse IDE.
Please refer to the [developer's guide for RAP](http://www.eclipse.org/rap/developers-guide/) for how to install.
Once installed, follow these instructions.

Copy resources (*.xul and laf.properties), resolve dependencies, create a classpath file

```bash
$ cd pentaho-kettle/ui
$ cp -r ../assembly/package-res/ui/* package-res/ui/
$ ant create-dot-classpath
```

Then import the project (pentaho-kettle/ui) into Eclipse IDE and use `package-res` as Source folder.

Configure your Run/Debug configurations as described [here](http://www.eclipse.org/rap/developers-guide/devguide.php?topic=launcher.html&version=3.1#rwt-launcher),
but please make sure to choose <i>Run from web.xml</i> and set `/Kettle UI/WEB-INF/web.xml` for Location.

# Notices

- Pentaho is a registered trademark of Pentaho, Inc.
- Oracle and Java are registered trademarks of Oracle and/or its affiliates.
- Ubuntu is a registered trademark of Canonical Ltd.
- Mac and OS X are trademarks of Apple Inc., registered in the U.S. and other countries.
- Windows and Active Directory are registered trademark of Microsoft Corporation in the U.S. and other countries.
- Eclipse is a registered trademark of the Eclipse Foundation, Inc. in the US and/or other countries.
- Apache Karaf is a trademark of The Apache Software Foundation.
- Google Chrome browser is a trademark of Google Inc.
- Other company and product names mentioned in this document may be the trademarks of their respective owners.
