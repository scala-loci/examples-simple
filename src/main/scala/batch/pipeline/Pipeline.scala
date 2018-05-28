package batch.pipeline

import loci._
import loci.rescalaTransmitter._
import loci.serializable.upickle._
import loci.tcp._

import rescala._


@multitier
object Pipeline {
  trait Input extends Peer { type Tie <: Single[Filter] }
  trait Filter extends Peer { type Tie <: Single[Mapper] with Single[Input] }
  trait Mapper extends Peer { type Tie <: Single[Folder] with Single[Filter] } 
  trait Folder extends Peer { type Tie <: Single[Mapper] }

  case class Author(name: String)

  case class Tweet(tags: Set[String], author: Author) {
    def hasHashtag(tag: String) = tags contains tag
  }

  val tweetStream: Evt[Tweet] on Input = Evt[Tweet]

  val filtered: Event[Tweet] on Filter = placed {
    tweetStream.asLocal filter { tweet => tweet.hasHashtag("multitier") } }

  val mapped: Event[Author] on Mapper = placed {
    filtered.asLocal map { tweet => tweet.author } }

  val folded: Signal[Map[Author, Int]] on Folder = placed {
    mapped.asLocal.fold(Map.empty[Author, Int].withDefaultValue(0)) {
      (map, author) => map + (author -> (map(author) + 1)) } }

  placed[Input].main {
    tweetStream fire Tweet(Set("distributed", "multitier"), Author("Some author"))
    tweetStream fire Tweet(Set("distributed", "reactive"), Author("Some other author"))
    tweetStream fire Tweet(Set("distributed", "multitier", "reactive"), Author("Some author"))
  }

  placed[Folder].main {
    var count = 0
    folded observe { result =>
      println(s"result: ${
        if (result.isEmpty)
          "(none)"
        else
          result map { case (author, count) => s"${author.name} ($count)" } mkString ","
      }")

      count += 1
      if (count > 2)
        multitier.terminate()
    }
  }
}


object PipelineMain extends App {
  multitier setup new Pipeline.Folder {
    def connect = request[Pipeline.Mapper] { TCP(1095).firstRequest }
  }

  multitier setup new Pipeline.Mapper {
    def connect =
      request[Pipeline.Folder] { TCP("localhost", 1095) } and
      request[Pipeline.Filter] { TCP(1096).firstRequest }
  }

  multitier setup new Pipeline.Filter {
    def connect =
      request[Pipeline.Mapper] { TCP("localhost", 1096) } and
      request[Pipeline.Input] { TCP(1097).firstRequest }
  }

  multitier setup new Pipeline.Input {
    def connect = request[Pipeline.Filter] { TCP("localhost", 1097) }
  }
}
