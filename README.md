# [ScalaLoci](http://scala-loci.github.io): Simple Examples

This repository contains simple showcases of ScalaLoci using small examples.


## Examples

* [Simple Chat](src/main/scala/interactive/chatsimple) implements a public chat with a
  single chat room, to which every participant can send messages.

* [Simple Time Service](src/main/scala/interactive/timeservicesimple) displays the current
  time which is sent from a server.

* [Time Service](src/main/scala/interactive/timeservice) displays the current time which is
  sent from a server and supports client-side user interaction by allowing the
  user to change the time format.

* [Pipeline](src/main/scala/batch/pipeline) shows how the operators in a processing
  pipeline can be placed on different peers to count the tweets that each author produces
  in a tweet stream.

* [Master–Worker](src/main/scala/batch/masterworker) shows an implementation of the
  master–worker pattern where a master node dispatches tasks to workers. 

* [Token Ring](src/main/scala/batch/tokenring) models a token ring, where every node in the
  ring can send a token for another node. Multiple tokens can circulate in the ring
  simultaneously until they reach their destination.


## Building the Examples

The examples require the *[DSL Paradise](http://github.com/pweisenburger/dslparadise)*
compiler plugin. Clone the *DSL Paradise* repository and publish the project locally using:

```scala
sbt publishLocal
```
