import javax.servlet.ServletContext

import _root_.akka.actor.{ActorSystem, Props}
import com.mchange.v2.c3p0.ComboPooledDataSource
import com.tomliddle.com.tomliddle.Worker
import com.tomliddle.{User, ResourceController, SecureController, SessionsController}
import org.scalatra._
import com.tomliddle.Tables._
import scala.slick.driver.H2Driver.simple._

import scala.slick.jdbc.JdbcBackend.Database.dynamicSession


import scala.slick.jdbc.JdbcBackend.Database

class ScalatraBootstrap extends LifeCycle {

	private val cpds = new ComboPooledDataSource
	private val db = Database.forDataSource(cpds)

	private val system = ActorSystem("actor_system")
	private val myActor = system.actorOf(Props[Worker])

	override def init(context: ServletContext) {
		context.mount(new SecureController(db, system, myActor), "/*")
		context.mount(new SessionsController(db), "/sessions/*")
		context.mount(new ResourceController, "/resource/*")
	}

	override def destroy(context: ServletContext) {
		super.destroy(context)
		system.shutdown() // shut down the actor system
		cpds.close
	}


	private def createData() = {
		db.withSession { implicit session =>
			(users.ddl ++ images.ddl).create
			users += User("tom", "test")
		}
	}
}

