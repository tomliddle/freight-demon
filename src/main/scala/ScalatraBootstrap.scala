import javax.servlet.ServletContext

import _root_.akka.actor.{ActorSystem, Props}
import _root_.com.mchange.v2.c3p0.ComboPooledDataSource
import com.tomliddle.database.{MongoSupport, DatabaseSupport, User, Tables}
import Tables._
import com.tomliddle.controllers.{ResourceController, SecureController, SessionsController}
import com.tomliddle.entity.Depot
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

	private val mongoSupport = new MongoSupport("freight_demon")

	override def init(context: ServletContext) {
		context.mount(new SecureController(db, mongoSupport), "/*")
		context.mount(new SessionsController(db), "/sessions/*")
		context.mount(new ResourceController, "/resource/*")
	}

	override def destroy(context: ServletContext) {
		super.destroy(context)
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
				depots += Depot("depot", BigDecimal(0), BigDecimal(51.48), "Greenwich", 1)
			}
		}
	}
}

