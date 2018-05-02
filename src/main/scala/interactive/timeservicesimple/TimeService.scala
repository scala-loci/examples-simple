package interactive.timeservicesimple

import loci._
import loci.transmitter.rescala._
import loci.serializer.upickle._
import loci.communicator.tcp._

import rescala._

import java.util.Date
import java.util.Calendar
import java.text.SimpleDateFormat


@multitier
object TimeService {
  trait Server extends Peer { type Tie <: Multiple[Client] }
  trait Client extends Peer { type Tie <: Single[Server] }

  val time = placed[Server] { Var(0l) }

  placed[Server].main {
    while (true) {
      time set Calendar.getInstance.getTimeInMillis
      Thread sleep 1000
    }
  }

  placed[Client].main {
    val format = new SimpleDateFormat("h:m:s")

    val display = Signal { format format new Date(time.asLocal()) }

    display.changed observe println
  }
}


object Server extends App {
  multitier setup new TimeService.Server {
    def connect = listen[TimeService.Client] { TCP(43053) }
  }
}

object Client extends App {
  multitier setup new TimeService.Client {
    def connect = connect[TimeService.Server] { TCP("localhost", 43053) }
  }
}
