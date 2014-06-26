package mesosphere.marathon.upgrade

import akka.testkit.{ TestActorRef, TestKit }
import akka.actor.{ Props, ActorSystem }
import mesosphere.marathon.{ AppStopCanceledException, SchedulerActions, MarathonSpec }
import org.scalatest.{ BeforeAndAfterAll, Matchers }
import org.scalatest.mock.MockitoSugar
import org.apache.mesos.SchedulerDriver
import mesosphere.marathon.api.v1.AppDefinition
import mesosphere.marathon.state.PathId
import scala.concurrent.{ Await, Promise }
import mesosphere.marathon.event.MesosStatusUpdateEvent
import org.mockito.Mockito._
import scala.concurrent.duration._
import mesosphere.marathon.tasks.TaskTracker
import scala.collection.mutable
import mesosphere.marathon.Protos.MarathonTask

class AppStopActorTest
    extends TestKit(ActorSystem("System"))
    with MarathonSpec
    with Matchers
    with BeforeAndAfterAll
    with MockitoSugar {

  var driver: SchedulerDriver = _
  var scheduler: SchedulerActions = _
  var taskTracker: TaskTracker = _

  before {
    driver = mock[SchedulerDriver]
    scheduler = mock[SchedulerActions]
    taskTracker = mock[TaskTracker]
  }

  test("Stop App") {
    val app = AppDefinition(id = PathId("app"), instances = 2)
    val promise = Promise[Unit]()
    val tasks = mutable.Set(marathonTask("task_a"), marathonTask("task_b"))

    when(taskTracker.fetchApp(app.id)).thenReturn(new TaskTracker.App(app.id, tasks, false))

    val ref = TestActorRef[AppStopActor](
      Props(
        new AppStopActor(
          driver,
          scheduler,
          taskTracker,
          system.eventStream,
          app,
          promise
        ))
    )
    watch(ref)

    system.eventStream.publish(MesosStatusUpdateEvent("", "task_a", "TASK_KILLED", app.id, "", Nil, app.version.toString))
    system.eventStream.publish(MesosStatusUpdateEvent("", "task_b", "TASK_KILLED", app.id, "", Nil, app.version.toString))

    Await.result(promise.future, 5.seconds)

    verify(scheduler).stopApp(driver, app)
    expectTerminated(ref)
  }

  test("Failed") {
    val app = AppDefinition(id = PathId("app"), instances = 2)
    val promise = Promise[Unit]()
    val tasks = mutable.Set(marathonTask("task_a"), marathonTask("task_b"))

    when(taskTracker.fetchApp(app.id)).thenReturn(new TaskTracker.App(app.id, tasks, false))

    val ref = TestActorRef[AppStopActor](
      Props(
        new AppStopActor(
          driver,
          scheduler,
          taskTracker,
          system.eventStream,
          app,
          promise
        ))
    )
    watch(ref)

    ref.stop()

    intercept[AppStopCanceledException] {
      Await.result(promise.future, 5.seconds)
    }

    verify(scheduler).stopApp(driver, app)
    expectTerminated(ref)
  }

  def marathonTask(name: String): MarathonTask = {
    MarathonTask
      .newBuilder
      .setId(name)
      .build()
  }
}