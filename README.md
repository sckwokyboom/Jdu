# Jdu

Jdu is a file tree utility that shows you how much space the files in a given root directory take up.

## Build

_Requires maven installed!_

To build the program into a JAR, enter this command in your terminal:

```
mvn clean install
```


The JAR with all the necessary dependencies and ready to run will be located in the `/target` folder.

## Starting the program:

To run the program, enter this command:

```
java -jar jdu-1.0-jar-with-dependencies.jar [options] your_target_dir
```

> _your_target_dir_ — root scan directory (default current directory).

*For the normal operation of the program, the installation of maven 3.9.0 and java 19 is required.*

## Options:

> `-depth n` — recursion depth (default limit is 1024);

> `-L` — follow symlinks;

> `-limit n` — show n heaviest files and/or directories at each level of the tree. (default limit 1024).

## Result of work:

A tree-like ordered display of files and directories in a given directory.

## Example:

```
java -jar jdu-1.0-jar-with-dependencies.jar -depth 2
```

```
jduInStream [7.91 MB] [directory]
├─ target [7.56 MB] [directory]
│   ├─ jdu-1.0-jar-with-dependencies.jar [7.4 MB] [regular]
│   ├─ classes [49.19 KB] [directory]
│   ├─ test-classes [38.52 KB] [directory]
│   ├─ surefire-reports [37.88 KB] [directory]
│   ├─ jdu-1.0.jar [32.62 KB] [regular]
│   ├─ maven-status [4.3 KB] [directory]
│   ├─ maven-archiver [52 B] [directory]
│   ├─ archive-tmp [0 B] [directory]
│   ├─ generated-sources [0 B] [directory]
│   ╰─ generated-test-sources [0 B] [directory]
├─ .git [222.31 KB] [directory]
│   ├─ objects [178.62 KB] [directory]
│   ├─ hooks [22.89 KB] [directory]
│   ├─ logs [16.12 KB] [directory]
│   ├─ index [3.76 KB] [regular]
│   ├─ config [293 B] [regular]
│   ├─ info [240 B] [directory]
│   ├─ FETCH_HEAD [94 B] [regular]
│   ├─ COMMIT_EDITMSG [93 B] [regular]
│   ├─ refs [82 B] [directory]
│   ├─ description [73 B] [regular]
│   ├─ ORIG_HEAD [41 B] [regular]
│   ╰─ HEAD [21 B] [regular]
├─ src [67.88 KB] [directory]
│   ├─ test [39.85 KB] [directory]
│   ╰─ main [28.03 KB] [directory]
├─ .idea [37.15 KB] [directory]
│   ├─ workspace.xml [22.17 KB] [regular]
│   ├─ uiDesigner.xml [8.71 KB] [regular]
│   ├─ artifacts [1.72 KB] [directory]
│   ├─ jarRepositories.xml [1.05 KB] [regular]
│   ├─ remote-targets.xml [874 B] [regular]
│   ├─ compiler.xml [704 B] [regular]
│   ├─ inspectionProfiles [557 B] [directory]
│   ├─ misc.xml [547 B] [regular]
│   ├─ encodings.xml [267 B] [regular]
│   ├─ modules.xml [253 B] [regular]
│   ├─ vcs.xml [185 B] [regular]
│   ├─ .gitignore [184 B] [regular]
│   ╰─ .name [16 B] [regular]
├─ Тесты.txt [20.96 KB] [regular]
├─ pom.xml [4.66 KB] [regular]
├─ README.md [1.93 KB] [regular]
├─ .gitignore [604 B] [regular]
├─ jdu.iml [367 B] [regular]
├─ errors.log [0 B] [regular]
├─ filenameTest [0 B] [regular]
├─ 1234 [unknown file format]
╰─ testSymlink [unknown file format]
```
