package interactive.timeservice

import loci._
import loci.rescalaTransmitter._
import loci.serializable.upickle._
import loci.tcp._

import rescala._

import scala.io
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
    val format = Var(new SimpleDateFormat("h:m:s"))

    val display = Signal { format() format new Date(time.asLocal()) }

    val show = Evt[Unit]

    (show snapshot display).changed observe println

    println("type `show` or specify a time format, e.g., `h:m:s`")

    while (multitier.running)
      io.StdIn.readLine match {
        case "show" =>
          show.fire
        case "exit" =>
          multitier.terminate
        case line =>
          try format set new SimpleDateFormat(line)
          catch { case e: IllegalArgumentException => println(e.getMessage) }
      }
  }
}


object Server extends App {
  multitier setup new TimeService.Server {
    def connect = listen[TimeService.Client] { TCP(43053) }
  }
}

object Client extends App {
  multitier setup new TimeService.Client {
    def connect = request[TimeService.Server] { TCP("localhost", 43053) }
  }
}
