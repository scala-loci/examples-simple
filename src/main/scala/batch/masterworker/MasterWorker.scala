package batch.masterworker

import loci._
import loci.rescalaTransmitter._
import loci.serializable.upickle._
import loci.tcp._

import rescala._


@multitier
object MasterWorker {
  trait Master extends Peer { type Tie <: Multiple[Worker] }
  trait Worker extends Peer { type Tie <: Single[Master] }

  case class Task(v: Int) { def exec: Int = 2 * v }

  val taskStream: Evt[Task] localOn Master = Evt[Task]

  val assocs: Signal[Map[Remote[Worker], Task]] localOn Master = placed {
    ((taskStream || taskResult.asLocalFromAllSeq || remote[Worker].joined)
      .fold((Map.empty[Remote[Worker], Task], List.empty[Task]))
       { case ((taskAssocs, taskQueue), taskChanged) =>
         assignTasks(taskAssocs, taskQueue, taskChanged, remote[Worker].connected) }
    map { case (taskAssocs, _) => taskAssocs }) }

  val deployedTask = placed[Master].sbj { worker: Remote[Worker] =>
    Signal { assocs().get(worker) } }
  val taskResult = placed[Worker] {
    deployedTask.asLocal.changed collect { case Some(task) => task.exec } }
  val result = placed[Master] {
    taskResult.asLocalFromAllSeq.fold(0){ case (acc, (_, result)) => acc + result } }

  def assignTasks(
      taskAssocs: Map[Remote[Worker], Task],
      taskQueue: List[Task],
      taskChanged: AnyRef,
      connected: Signal[Seq[Remote[Worker]]]) = placed[Master].local {
    def assignFreeWorker(assocs: Map[Remote[Worker], Task], task: Task) =
      (connected.now filterNot { assocs contains _ }).headOption map { worker =>
        assocs + (worker -> task)
      }

    def assignQueuedTask(assocs: Map[Remote[Worker], Task]) =
      taskQueue.headOption flatMap { task =>
        assignFreeWorker(assocs, task) map { _ -> taskQueue.tail }
      } getOrElse assocs -> taskQueue

    taskChanged match {
      case (worker: Remote[Worker @unchecked], _) =>
        assignQueuedTask(taskAssocs - worker)

      case _: Remote[_] =>
        assignQueuedTask(taskAssocs)

      case task: Task =>
        assignFreeWorker(taskAssocs, task) map { _ -> taskQueue } getOrElse
          taskAssocs -> (taskQueue :+ task)
    }
  }

  placed[Master].main {
    var count = 0
    result observe { result =>
      println(s"current result value: $result")
      count += 1
      if (count > 3)
        multitier.terminate()
    }

    taskStream fire Task(5)
    taskStream fire Task(7)
    taskStream fire Task(9)
  }
}


object MasterWorkerMain extends App {
  multitier setup new MasterWorker.Master {
    def connect = listen[MasterWorker.Worker] { TCP(1095) }
  }

  1 to 2 foreach { _ =>
    multitier setup new MasterWorker.Worker {
      def connect = request[MasterWorker.Master] { TCP("localhost", 1095) }
    }
  }
}
