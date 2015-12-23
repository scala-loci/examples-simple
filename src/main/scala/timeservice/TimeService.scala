package timeservice

import retier._
import retier.rescalaTransmitter._
import retier.serializable.upickle._
import retier.tcp._

import rescala.Var
import rescala.Signal
import rescala.events.ImperativeEvent
import makro.SignalMacro.{SignalM => Signal}

import scala.io
import java.util.Date
import java.util.Calendar
import java.text.SimpleDateFormat

@multitier
object TimeService {
  class Server extends Peer {
    type Connection <: Multiple[Client]
    def connect = listen[Client] { TCP(43053) }
  }

  class Client extends Peer {
    type Connection <: Single[Server]
    def connect = request[Server] { TCP("localhost", 43053) }
  }

  val time = placed[Server] { Var(0l) }

  placed[Server].main {
    while (true) {
      time() = Calendar.getInstance.getTimeInMillis
      Thread sleep 1000
    }
  }

  placed[Client].main {
    val format = Var(new SimpleDateFormat("h:m:s"))

    val display = Signal { format() format new Date(time.asLocal()) }

    val show = new ImperativeEvent[Unit]

    (display snapshot show).changed += println

    println("type `show` or specify a time format, e.g., `h:m:s`")

    while (multitier.running)
      io.StdIn.readLine match {
        case "show" =>
          show(())
        case "exit" =>
          multitier.terminate
        case line =>
          try format() = new SimpleDateFormat(line)
          catch { case e: IllegalArgumentException => println(e.getMessage) }
      }
  }
}

object Server extends App {
  multitier.run[TimeService.Server]
}

object Client extends App {
  multitier.run[TimeService.Client]
}
