# crjdt: a conflict-free replicated JSON datatype in Scala
[![Build Status](https://travis-ci.org/fthomas/crjdt.svg?branch=master)](https://travis-ci.org/fthomas/crjdt)
[![codecov](https://codecov.io/gh/fthomas/crjdt/branch/master/graph/badge.svg)](https://codecov.io/gh/fthomas/crjdt)
[![Join the chat at https://gitter.im/fthomas/crjdt](https://badges.gitter.im/fthomas/crjdt.svg)](https://gitter.im/fthomas/crjdt?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Scaladex](https://index.scala-lang.org/fthomas/crjdt/crjdt-core/latest.svg?color=blue)][scaladex]
[![Scaladoc](https://www.javadoc.io/badge/eu.timepit/crjdt-core_2.12.svg?color=blue&label=Scaladoc)](https://www.javadoc.io/doc/eu.timepit/crjdt-core_2.12)

This is an implementation of the data structures and algorithms described
in the paper [A Conflict-Free Replicated JSON Datatype][paper.abs]
([PDF][paper.pdf]) by **[Martin Kleppmann][kleppmann]** and
**[Alastair R. Beresford][beresford]**.

The goal of this project is to provide a high-level API to the CRDT described
in the paper that integrates well with other JSON libraries for Scala.

## Getting Started

crjdt is currently available for Scala and [Scala.js][scala.js],
version 2.11 and 2.12.

To get started with sbt, add the following to your `build.sbt` file:

```sbt
libraryDependencies ++= Seq(
  "eu.timepit" %% "crjdt-core"  % "0.0.7",
  "eu.timepit" %% "crjdt-circe" % "0.0.7" // optional
)
```
For Scala.js just replace `%%` with `%%%` above.

Instructions for Maven and other build tools are available on the
[Scaladex][scaladex] page.

## Contributors and participation

* [Frank S. Thomas](https://github.com/fthomas) ([@fthomas](https://github.com/fthomas))
* [Jan](https://github.com/Tamriel) ([@Tamriel](https://github.com/Tamriel))
* [Yusuke Yasuda](https://github.com/TanUkkii007) ([@TanUkkii007](https://github.com/TanUkkii007))

The crjdt project supports the [Typelevel][typelevel]
[code of conduct][typelevel-coc] and wants all of its channels (Gitter,
GitHub, etc.) to be welcoming environments for everyone.

## Other implementations

Currently crjdt is the only public implementation of the JSON CRDT described
in the [paper][paper.abs] by **Kleppmann** and **Beresford**. We will list
other implementations here as soon as they become available.

If you know an implementation that is not listed here, please submit a PR!

## License

Copyright 2016 Frank S. Thomas

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
[paper.abs]: http://arxiv.org/abs/1608.03960
[paper.pdf]: http://arxiv.org/pdf/1608.03960.pdf
[scala.js]: http://www.scala-js.org/
[scaladex]: https://index.scala-lang.org/fthomas/crjdt/crjdt-core
[typelevel]: http://typelevel.org/
[typelevel-coc]: http://typelevel.org/conduct.html
