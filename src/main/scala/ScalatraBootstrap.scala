import javax.servlet.ServletContext

import _root_.akka.actor.{ActorSystem, Props}
import _root_.com.mchange.v2.c3p0.ComboPooledDataSource
import _root_.com.tomliddle.Tables._
import _root_.com.tomliddle.com.tomliddle.Worker
import com.tomliddle.controllers.{ResourceController, SecureController, SessionsController}
import com.tomliddle.solution.Depot
import com.tomliddle.{DBSolution, DatabaseSupport, User}
import org.scalatra._

import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import scala.slick.jdbc.meta.MTable

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
			if (!MTable.getTables.list.exists(_.name.name == "USERS")) {
				(users.ddl).create
				users += User("tom", "tom@gmail.com", "password")
			}
			if (!MTable.getTables.list.exists(_.name.name == "TRUCKS")) {
				(trucks.ddl).create
			}
			if (!MTable.getTables.list.exists(_.name.name == "STOPS")) {
				(stops.ddl).create
			}
			if (!MTable.getTables.list.exists(_.name.name == "DEPOTS")) {
				(depots.ddl).create
				//val userId = (users returning users.map(_.id)) += User(None, "Stefan", "Zeiger")
				depots += Depot("depot", BigDecimal(0), BigDecimal(51.48), "Greenwich", 1)
			}
			if (!MTable.getTables.list.exists(_.name.name == "SOLUTIONS")) {
				(solutions.ddl).create
				solutions += DBSolution("Solution" , 1, Some(1))
			}
		}
	}
}

