XNote
=====

A simple and easy-to-use note taking tool based on Eclipse SWT. Works best with Mac OS X.

Prerequisite
------------

* Eclipse SWT standalone binary (http://eclipse.org/swt). You need to download and unzip `swt-x.x.x-xxxxx-xxxx-xxxx.zip`, then copy `swt.jar` into `lib` folder. (The path `lib/swt.jar` has been added to `.gitignore` so that it will not be added to the repository.)
* (optional, may skip for Mac OS X) Java Runtime Environment (JRE) (http://oracle.com/java).
* (optional, may skip for Mac OS X) Ant builder (http://ant.apache.org/bindownload.cgi).

Build
-----

To make `XNote.app` for Mac OS X, type and run these in Terminal:

```bash
$ ant -f build.xml macapp
```

The `XNote.app` will be generated in the `build` folder. Move it to `/Applications` and it's done.

To build `xnote.jar` only, type and run these in Terminal:

```bash
$ ant -f build.xml jar
```

The `xnote.jar` will be generated in the `build` folder. Note that `swt.jar` should always be copied with `xnote.jar` in the same folder.
