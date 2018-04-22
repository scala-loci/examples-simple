# [ScalaLoci](http://scala-loci.github.io): Simple Examples

This repository contains simple showcases of ScalaLoci using small examples.


## Examples

* [Simple Chat](src/main/scala/chatsimple) implements a public chat with a
  single chat room, to which every participant can send messages.

* [Simple Time Service](src/main/scala/timeservicesimple) displays the current
  time which is sent from a server.

* [Time Service](src/main/scala/timeservice) displays the current time which is
  sent from a server and supports client-side user interaction by allowing the
  user to change the time format.


## Building the Examples

The examples require the *[DSL Paradise](http://github.com/pweisenburger/dslparadise)*
compiler plugin. Clone the *DSL Paradise* repository and publish the project locally using:

```scala
sbt publishLocal
```
