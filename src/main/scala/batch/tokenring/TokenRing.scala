package batch.tokenring

import rescala._

import loci._
import loci.transmitter.rescala._
import loci.communicator.tcp._
import loci.serializer.upickle._

import java.util.UUID


@multitier
object TokenRing {
  type Id = UUID
  type Token = String

  trait Prev extends Peer { type Tie <: Single[Prev] }
  trait Next extends Peer { type Tie <: Single[Next] }
  trait Node extends Prev with Next { type Tie <: Single[Prev] with Single[Next] }

  val id: Id on Prev = UUID.randomUUID
  val sendToken: Evt[(Id, Token)] localOn Prev = Evt[(Id, Token)]
  val recv: Event[Token] localOn Prev = placed {
    sent.asLocal collect {
      case (receiver, token) if receiver == id => token } }
  val sent: Event[(Id, Token)] on Prev = placed {
    (sent.asLocal \ recv) || sendToken }

  placed[Node].main {
    recv observe { token: Token =>
      println(s"""received:  "$token"\n      on:             $id""")
      multitier.terminate()
    }

    sendToken fire ((id, s"token for $id"))
  }
}


object TokenRingMain extends App {
  val ports = 1095 to 1099

  val Seq(first, middle @ _*) = ports zip ports.tail
  val last = (ports.last, ports.head)

  val requestors =
    (TCP(first._1).firstConnection -> TCP(first._2).firstConnection) +:
    (middle map { middle =>
      TCP("localhost", middle._1) -> TCP(middle._2).firstConnection }) :+
    (TCP("localhost", last._1) -> TCP("localhost", last._2))

  requestors foreach { requestor =>
    multitier setup new TokenRing.Node {
      def connect =
        connect[TokenRing.Prev] { requestor._1 } and
        connect[TokenRing.Next] { requestor._2 }
    }
  }
}
