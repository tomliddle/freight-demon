import javax.servlet.ServletContext

import _root_.akka.actor.{ActorSystem, Props}
import _root_.com.mchange.v2.c3p0.ComboPooledDataSource
import _root_.com.tomliddle.com.tomliddle.Worker
import com.tomliddle._
import org.scalatra._
import _root_.com.tomliddle.Tables._
import scala.slick.driver.H2Driver.simple._

import scala.slick.jdbc.JdbcBackend.Database.dynamicSession


import scala.slick.jdbc.JdbcBackend.Database

class ScalatraBootstrap extends LifeCycle {

	private val cpds = new ComboPooledDataSource
	private val database = Database.forDataSource(cpds)
	createData()
	private val db = new DatabaseSupport(database)

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
		database.withDynSession {
			(users.ddl ++ images.ddl).create
			users += User("tom", "tom@gmail.com", "password")
		}
	}
}

