package com.tomliddle

import java.io.File
import java.util.concurrent.TimeUnit
import _root_.akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout
import com.tomliddle.examples.Suppliers
import org.scalatra._
import org.scalatra.servlet.{SizeConstraintExceededException, MultipartConfig, FileUploadSupport}
import org.slf4j.LoggerFactory
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import scala.concurrent.ExecutionContext
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.lifted.TableQuery
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import scala.slick.driver.H2Driver.simple._
import Tables._

class MyServlet(db: Database, system: ActorSystem, myActor: ActorRef) extends ScalatraServlet with FutureSupport with FileUploadSupport {

	configureMultipartHandling(MultipartConfig(maxFileSize = Some(3*1024*1024)))

	protected implicit val timeout = Timeout(5, TimeUnit.SECONDS)
	protected implicit val jsonFormats: Formats = DefaultFormats
	protected implicit def executor: ExecutionContext = system.dispatcher

	private val logger = LoggerFactory.getLogger(getClass)

	before() {

	}

	after() {

	}


	//*********************** LOGINS ETC ***************************
	get("/") {
		contentType="text/html"
		new File("src/main/webapp/index.html")
	}

	post("/login") {

	}

	post("/register") {
		db withDynSession {
			users += User("tom", "test")
		}
	}

	post("/create") {
		db withDynSession {
			(users.ddl ++ images.ddl).create
		}
	}

	// Edit profile

	//************** IMAGE HANDLING ******************************
	put("/image/:name") {
		val name = params("name")
		def file = fileParams("image-file")
		// Return image id
		db withDynSession {
			images += Image(name, file.get, 1)
		}
	}

	get("/image/:id") {
		val imageId = params("id")
		db withDynSession {
			users.filter(_.id == imageId)
		}
	}

	get("/image/delete/:id") {
		val imageId = params("id")
		db withDynSession {
			images.filter(_.id == imageId).delete
		}
	}

	get("/images") {
		db withDynSession {
			images.filter(_.userId == 1)
		}
	}


	error {
		case e: SizeConstraintExceededException => RequestEntityTooLarge("file is too big")
	}




	// *************************** Old ***************************
/*	get("/heating/set/:temp") {
		try {
			val temp = BigDecimal(params("temp")).setScale(2)
			myActor ! HeatingStatus(Status.SET_TO, Some(temp))
		} catch {
			case e: NumberFormatException => {
				logger.error("Number format exception", e)
			}
		}
		""
	}
	get("/add") {
		contentType = "application/json"
		implicit val timeoutT = Timeout(5, TimeUnit.SECONDS)
		new AsyncResult {
			val is = (myActor ? GetStatus).mapTo[HeatingStatusAll].map {
				statusAll =>
					compact(render(JObject(
						"status" -> statusAll.status.toString,
						"currentTemp" -> statusAll.currentTemp,
						"targetTemp" -> statusAll.targetTemp,
						"outsideTemp" -> statusAll.outsideTemp,
						"outlook" -> statusAll.outlook
					)))
			}
		}
	}*/
}

