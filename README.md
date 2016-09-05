# crjdt: a conflict-free replicated JSON datatype in Scala
[![Build Status](https://travis-ci.org/fthomas/crjdt.svg?branch=master)](https://travis-ci.org/fthomas/crjdt)
[![codecov](https://codecov.io/gh/fthomas/crjdt/branch/master/graph/badge.svg)](https://codecov.io/gh/fthomas/crjdt)
[![Join the chat at https://gitter.im/fthomas/crjdt](https://badges.gitter.im/fthomas/crjdt.svg)](https://gitter.im/fthomas/crjdt?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Scaladex](https://index.scala-lang.org/fthomas/crjdt/crjdt-core/latest.svg)](https://index.scala-lang.org/fthomas/crjdt/crjdt-core)

This is an implementation of the data structures and algorithms described
in the paper [A Conflict-Free Replicated JSON Datatype][paper] by
*Martin Kleppmann and Alastair R. Beresford* in Scala.

## Getting Started

crjdt is currently available for Scala and [Scala.js][scala.js], version 2.11.

To get started with sbt, add the following to your `build.sbt` file:

```sbt
resolvers += Resolver.bintrayRepo("fthomas", "maven")

libraryDependencies += "eu.timepit" %% "crjdt-core" % "0.0.1"
```

[paper]: http://arxiv.org/pdf/1608.03960.pdf
[scala.js]: http://www.scala-js.org/
