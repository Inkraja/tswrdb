tswrdb
======

tswrdb is a project that aims to document and export the contents of rdb data files (.rdbdata) from The Secret World, a MMORPG by Funcom. This project is for *fun*, *curiosity* and for the community to create useful tools and fansites that benefit other TSW fans.

tswrdb is written in Scala.

The project consists of two components:

* `tswrdb-api`: the Scala API for exporting rdb data.
* `tswrdb-cmdui`: the Scala program that enables *users* to export rdb data. It uses `tswrdb-api` client code.

Note: all active development takes place in the `develop` branch. Stable releases are merged into `master`.

Setup
==========

Requirements
* sbt 0.12.4+
* Scala 2.10.0+

tswrdb uses [sbt to build](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html).

Unless wanted for development, there should be no need to fetch Scala aswell. During build, sbt will fetch it for you to run with tswrdb.

Building
========

Fetch the source code (if you have added a [SSH key to github](https://help.github.com/articles/generating-ssh-keys)):

    git clone git@github.com:joakibj/tswrdb.git

Alternatively:

    git clone https://github.com/joakibj/tswrdb.git

Make sure the ``develop`` branch is used (contains the newest version):

    git checkout develop

In the tswrdb root directory (e.g. ``~/dev/tswrdb``), run sbt by typing:

    sbt

It will fetch sbt itself, in addition to scala 2.10.0 and all dependencies.

When in the sbt command shell, to compile:

    compile

And to run tests:

    test

To generate IntelliJ IDEA project files:

    gen-idea

To assemble a "binary", known as a fatjar:

    assembly

The jar can be found in `tswrdb\tswrdb-cmdui\target\scala-2.10`

Usage
=====

**All use of tswrdb is at your own risk!**

#### Using the packaged fatjar

Currently tswrdb is packaged as a self-contained jar. Since tswrdb is in an initial development stage, it is not versioned. However, a [pre-release](https://github.com/joakibj/tswrdb/releases/tag/v0.0.1) can be found.

To use tswrdb you need the following:

1. The [tswrdb-0.0.1.jar](https://github.com/joakibj/tswrdb/releases/tag/v0.0.1) in a directory of your choice. (e.g. `C:\tswrdb`)
2. A shell (cmd.exe or any *nix shell)
3. Java 6 runtime
4. A legal copy of The Secret World by Funcom
    * An installation of TSW ([TestLive](http://forums.thesecretworld.com/showthread.php?t=55882) is recommended)

The following examples assume cmd.exe on a Windows installation. Please note that paths with spaces on Windows must be encapsulated in "quotes".

When in the tswrdb directory (prints version):

    java -jar tswrdb-0.0.1.jar --version

Export all Loading Screen Images to ``./exported/1010042 (Loading Screen Images)``:

    java -jar tswrdb-0.0.1.jar export rdbtype 1010042 --tsw "D:\Programs\TSW TestLive"

Export all german strings to XML files in ``./exported/1030002 (Strings)``:

    java -jar tswrdb-0.0.1.jar export strings --lang de --tsw "D:\Programs\TSW TestLive"

Export all english strings to JSON files in ``./exported/1030002 (Strings)``:

    java -jar tswrdb-0.0.1.jar export strings --lang en --json --tsw "D:\Programs\TSW TestLive"

To use tswrdb with the MSYS or Cygwin shell on Windows, enclose the path in triple double-quotes. e.g.:

    java -jar tswrdb-0.0.1.jar export strings --lang en --json --tsw """D:\Programs\TSW TestLive"""

#### Using sbt

It is possible to run tswrdb in sbt without any additional hassle.

When in the tswrdb root directory (e.g. `~/dev/tswrdb`):

    sbt

*The following commands are all done in the sbt command shell.*

Change to the tswrdb program:

    project tswrdb-cmdui

Run the program (prints version):

    run --version

Usage is the same as using the fatjar, except commands must be executed with the `run` task in sbt. e.g.:

Export all Loading Screen Images to ``<tswrdb folder>/tswrdb-cmdui/exported/1010042 (Loading Screen Images)``:

    run export rdbtype 1010042 --tsw """D:\Programs\TSW TestLive"""

Export all german strings to XML files in ``<tswrdb folder>/tswrdb-cmdui/exported/1030002 (Strings)``:

    run export strings --lang de --tsw """D:\Programs\TSW TestLive"""

Export all english strings to JSON files in ``<tswrdb folder>/tswrdb-cmdui/exported/1030002 (Strings)``:

    run export strings --lang en --json --tsw """D:\Programs\TSW TestLive"""

See the usage below for all available commands.

```
tswrdb 0.0.1
Usage: tswrdb [list|export|index] [options]

  --tsw <directory>
        tsw points to the TSW install directory and is required.

Command: list [options]
Lists the valid rdb types available. Per default and to keep the user sane, only well understood RdbTypes are listed.
  -a | --all
        List all rdbtypes, regardless. Note that some are highly mysterious and/or esoteric. You will have to make sense of them yourself

Command: export [rdbtype|strings] <args>...

Command: export rdbtype <rdbType>
Export any RdbType as they appear in the resource database.
  <rdbType>
        rdbType of the data that is going to be exported.
Command: export strings [options]
Export strings (RdbType 1030002). XML is output per default, this can be overriden with Option --json.
  -l <value> | --lang <value>
        Exports all strings for the language. Valid options are en, fr or de. Required.
  --json
        Strings are exported as JSON.

Command: index [info]

Command: index info
Show information about index file: version, hash, number of entries

  --help
        prints this usage text.
  --version
        prints the version
```

Documentation
=============
**Note: Work in progress!**

Please see the [DOCUMENTATION](docs/DOCUMENTATION.md) file for documentation about the RDB data formats.

Known issues / Troubleshooting
==============================

#### Known issues

* Currently an issue with RdbType 1010008. Need to verify if this actually exist in fresh downloads of Live/TestLive. See #3

#### Troubleshooting

> I get a java.lang.OutOfMemoryError exception

Try running tswrdb with these JVM options: `-Xmx2G -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled`. This means that we want the JVM to set the heap to max 2GB, enable the garbage collector and sweep permanent generation memory (classes and strings).

e.g.:

    java -Xmx2G -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -jar tswrdb-0.0.1.jar --help

Acknowledgements
================

Thanks to:

* Jacob Seidelin for compiling the RDB documentation and software for extracting TSW rdbdata.
* The [scopt](https://github.com/scopt/scopt) project for command line parsing.

Please see [ACKNOWLEDGEMENTS](docs/ACKNOWLEDGEMENTS.md) file for license and copyright notices in verbatim.

License
=======

The source code is licensed under GPLv2. Please refer to the [LICENSE](LICENSE) file for the license text in verbatim.
