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
