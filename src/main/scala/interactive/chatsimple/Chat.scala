package interactive.chatsimple

import loci._
import loci.transmitter.rescala._
import loci.serializer.upickle._
import loci.communicator.tcp._

import rescala._


@multitier
object Chat {
  trait Server extends Peer { type Tie <: Multiple[Client] }
  trait Client extends Peer { type Tie <: Single[Server] }

  val message = placed[Client] { Evt[String] }

  val publicMessage = placed[Server].sbj { client: Remote[Client] =>
    message.asLocalFromAllSeq collect {
      case (remote, message) if remote != client => message
    }
  }

  placed[Client].main {
    publicMessage.asLocal observe println

    for (line <- scala.io.Source.stdin.getLines)
      message fire line
  }
}


object Server extends App {
  multitier setup new Chat.Server {
    def connect = listen[Chat.Client] { TCP(43053) }
  }
}

object Client extends App {
  multitier setup new Chat.Client {
    def connect = connect[Chat.Server] { TCP("localhost", 43053) }
  }
}
