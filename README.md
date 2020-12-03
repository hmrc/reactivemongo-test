# reactivemongo-test

[ ![Download](https://api.bintray.com/packages/hmrc/releases/reactivemongo-test/images/download.svg) ](https://bintray.com/hmrc/releases/reactivemongo-test/_latestVersion) [![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)  

Test library for reactivemongo

### FailOnUnIndexedQueries

Mixing this trait into your spec will trigger test failure if un-indexed queries are executed. This is achieved by activating Mongo's `notablescan`
option before running the spec (and turning it off afterwards).

### Installing

Include the following dependency in your SBT build

```scala
resolvers += Resolver.bintrayRepo("hmrc", "releases")

libraryDependencies += "uk.gov.hmrc" %% "reactivemongo-test" % "[INSERT_VERSION]"
```

* *For Play 2.6.x (scala 2.11, 2.12) use versions 4.x.x-play-26*
* *For Play 2.5.x (scala 2.11) use versions 4.x.x-play-25*
* *For Java 7 and Play 2.3.x use versions <=1.1.0*
