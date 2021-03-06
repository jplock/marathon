package mesosphere.marathon.event.http

import akka.actor.ActorRef
import com.google.common.eventbus.{ EventBus, Subscribe }
import mesosphere.marathon.event.{ EventModule, EventSubscriber, MarathonEvent }
import org.apache.log4j.Logger
import javax.inject.{ Named, Inject }

class HttpCallbackEventSubscriber @Inject() (
  @Named(HttpEventModule.StatusUpdateActor) val actor: ActorRef,
  @Named(EventModule.busName) val eventBus: Option[EventBus])
    extends EventSubscriber[HttpEventConfiguration, HttpEventModule] {

  require(eventBus.nonEmpty, "Tried to bind HTTP event publishing without " +
    "event bus.")
  eventBus.get.register(this)

  val log = Logger.getLogger(getClass.getName)

  @Subscribe
  def handleMarathonEvent(event: MarathonEvent) {
    log.info("Received message from bus:" + event)
    actor ! event
  }

  def configuration() = {
    classOf[HttpEventConfiguration]
  }

  def module() = {
    Some(classOf[HttpEventModule])
  }
}
