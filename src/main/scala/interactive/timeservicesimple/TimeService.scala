package interactive.timeservicesimple

import loci._
import loci.transmitter.rescala._
import loci.serializer.upickle._
import loci.communicator.tcp._

import rescala.default._

import java.util.Date
import java.util.Calendar
import java.text.SimpleDateFormat


@multitier object TimeService {
  @peer type Peer
  @peer type Server <: Peer { type Tie <: Multiple[Client] }
  @peer type Client <: Peer { type Tie <: Single[Server] }

  val time = on[Server] { Var(0L) }

  def main(): Unit on Peer =
    (on[Server] {
      while (true) {
        time set Calendar.getInstance.getTimeInMillis
        Thread sleep 1000
      }
    }) and
    (on[Client] {
      val format = new SimpleDateFormat("h:m:s")

      val display = Signal { format format new Date(time.asLocal()) }

      display.changed observe println
    })
}


object Server extends App {
  multitier start new Instance[TimeService.Server](
    listen[TimeService.Client] { TCP(43053) })
}

object Client extends App {
  multitier start new Instance[TimeService.Client](
    connect[TimeService.Server] { TCP("localhost", 43053) })
}
