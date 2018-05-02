package batch.pipeline

import rescala._

import upickle.default._

import loci._
import loci.transmitter.rescala._
import loci.communicator.tcp._
import loci.serializer.upickle._

import scala.language.higherKinds


case class Author(name: String)

object Author {
  implicit val taskSerializer: ReadWriter[Author] = macroRW[Author]
}

case class Tweet(tags: Set[String], author: Author) {
  def hasHashtag(tag: String) = tags contains tag
}

object Tweet {
  implicit val taskSerializer: ReadWriter[Tweet] = macroRW[Tweet]
}

@multitier
object Pipeline {
  trait Input extends Peer { type Tie <: Single[Filter] }
  trait Filter extends Peer { type Tie <: Single[Mapper] with Single[Input] }
  trait Mapper extends Peer { type Tie <: Single[Folder] with Single[Filter] } 
  trait Folder extends Peer { type Tie <: Single[Mapper] }

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
    def connect = connect[Pipeline.Mapper] { TCP(1095).firstConnection }
  }

  multitier setup new Pipeline.Mapper {
    def connect =
      connect[Pipeline.Folder] { TCP("localhost", 1095) } and
      connect[Pipeline.Filter] { TCP(1096).firstConnection }
  }

  multitier setup new Pipeline.Filter {
    def connect =
      connect[Pipeline.Mapper] { TCP("localhost", 1096) } and
      connect[Pipeline.Input] { TCP(1097).firstConnection }
  }

  multitier setup new Pipeline.Input {
    def connect = connect[Pipeline.Filter] { TCP("localhost", 1097) }
  }
}
