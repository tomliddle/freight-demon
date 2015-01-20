import javax.servlet.ServletContext

import _root_.akka.actor.{Props, ActorSystem}
import com.mchange.v2.c3p0.ComboPooledDataSource
import com.tomliddle.com.tomliddle.Worker
import com.tomliddle.{SessionsController, SecureController}
import scala.slick.jdbc.JdbcBackend.Database
import org.scalatra._

class ScalatraBootstrap extends LifeCycle {

	val cpds = new ComboPooledDataSource

	private val system = ActorSystem("actor_system")
	private val myActor = system.actorOf(Props[Worker])

	override def init(context: ServletContext) {
		val db = Database.forDataSource(cpds)
		context.mount(new SecureController(db, system, myActor), "/*")
		context.mount(new SessionsController, "/sessions/*")
	}

	override def destroy(context: ServletContext) {
		super.destroy(context)
		system.shutdown() // shut down the actor system
		cpds.close
	}
}

