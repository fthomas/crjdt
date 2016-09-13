# crjdt: a conflict-free replicated JSON datatype in Scala
[![Build Status](https://travis-ci.org/fthomas/crjdt.svg?branch=master)](https://travis-ci.org/fthomas/crjdt)
[![codecov](https://codecov.io/gh/fthomas/crjdt/branch/master/graph/badge.svg)](https://codecov.io/gh/fthomas/crjdt)
[![Join the chat at https://gitter.im/fthomas/crjdt](https://badges.gitter.im/fthomas/crjdt.svg)](https://gitter.im/fthomas/crjdt?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Scaladex](https://index.scala-lang.org/fthomas/crjdt/crjdt-core/latest.svg?color=blue)][scaladex]

This is an implementation of the data structures and algorithms described
in the paper [A Conflict-Free Replicated JSON Datatype][paper] by
**[Martin Kleppmann][kleppmann]** and **[Alastair R. Beresford][beresford]**.

The goal of this project is to provide a high-level API to the CRDT described
in the paper that integrates well with other JSON libraries for Scala.

## Getting Started

crjdt is currently available for Scala and [Scala.js][scala.js], version 2.11.

To get started with sbt, add the following to your `build.sbt` file:

```sbt
resolvers += Resolver.bintrayRepo("fthomas", "maven")

libraryDependencies += "eu.timepit" %% "crjdt-core" % "0.0.3"
```

Instructions for Maven and other build tools are available on the
[Scaladex][scaladex] page.

## Contributors and participation

crjdt is currently maintained by [Frank Thomas][fst9000].

The crjdt project supports the [Typelevel][typelevel] [code of conduct][typelevel-coc]
and wants all of its channels (Gitter, GitHub, etc.) to be welcoming environments for
everyone.

## License

crjdt is licensed under the [Apache License, Version 2.0][apache2]
(the "License"); you may not use this software except in compliance with
the License.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[apache2]: http://www.apache.org/licenses/LICENSE-2.0
[beresford]: https://www.cl.cam.ac.uk/~arb33/
[fst9000]: https://twitter.com/fst9000
[kleppmann]: https://martin.kleppmann.com/
[paper]: http://arxiv.org/pdf/1608.03960.pdf
[scala.js]: http://www.scala-js.org/
[scaladex]: https://index.scala-lang.org/fthomas/crjdt/crjdt-core
[typelevel]: http://typelevel.org/
[typelevel-coc]: http://typelevel.org/conduct.html
