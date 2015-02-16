package com.tomliddle.controllers

import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import akka.actor.{ActorSystem, ActorRef}
import akka.util.Timeout
import com.tomliddle.{DBStop, DBTruck, DatabaseSupport}
import com.tomliddle.auth.AuthenticationSupport
import com.tomliddle.form.{StopForm, TruckForm}
import com.tomliddle.solution.{Solution, LocationMatrix}
import org.json4s.JsonAST.{JString, JObject}
import org.json4s.JsonDSL.WithBigDecimal._
import org.joda.time.{LocalDate, LocalTime}
import org.joda.time.format.DateTimeFormat
import org.json4s._
import org.json4s.ext.JodaTimeSerializers
import org.scalatra.{RequestEntityTooLarge, FutureSupport}
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.servlet.{SizeConstraintExceededException, FileUploadSupport, MultipartConfig}
import scala.concurrent.ExecutionContext

case class Status(status: String)

class SecureController(protected val db: DatabaseSupport, system: ActorSystem, myActor: ActorRef)
		extends ScalateServlet with FutureSupport with FileUploadSupport with AuthenticationSupport with JacksonJsonSupport {

	configureMultipartHandling(MultipartConfig(maxFileSize = Some(3 * 1024 * 1024)))

	private final val formatter = DateTimeFormat.forPattern("HH:mm")

	case object LocalTimeSerialiser extends CustomSerializer[LocalTime](format => ({
			case JString(s) => LocalTime.parse(s, formatter)
			case JNull => null
		},{
			case x: LocalTime => JString(x.toString(formatter))
		})
	)

	protected implicit val timeout = Timeout(5, TimeUnit.SECONDS)
	protected implicit val jsonFormats: Formats = DefaultFormats +  LocalTimeSerialiser
	protected implicit def executor: ExecutionContext = system.dispatcher


	before() {
		requireLogin()
	}

	//************** Truck HANDLING ******************************
	// Add truck
	post("/truck") {
		var truckForm = parsedBody.extract[TruckForm]
		db.addTruck(truckForm.getTruck(scentry.user.id.get))
	}

	// Get truck
	get("/truck/:id") {
		contentType = formats("json")
		db.getTruck(params("id").toInt, scentry.user.id.get)
	}

	// Get truck for that user
	get("/truck") {
		contentType = formats("json")
		db.getTrucks(scentry.user.id.get)
	}

	// Delete truck
	delete("/truck/:id") {
		db.deleteTruck(params("id").toInt, scentry.user.id.get)
	}

	//************** Stop HANDLING ******************************
	// Add stop
	post("/stop") {
		var stopForm = parsedBody.extract[StopForm]

		db.getLocation(stopForm.postcode).foreach {
			location =>
				val stop = stopForm.getStop(scentry.user.id.get, location.id.get)
				db.addStop(stop)
		}
	}

	// Get stop
	get("/stop/:id") {
		contentType = formats("json")
		db.getStop(params("id").toInt, scentry.user.id.get)
	}

	// Get stop for that user
	get("/stop") {
		contentType = formats("json")
		db.getStops(scentry.user.id.get)
	}

	// Delete stop
	delete("/stop/:id") {
		db.deleteStop(params("id").toInt, scentry.user.id.get)
	}


	// ****************************** SOLUTION *********************

	post("/solution") {
		val name = params("name")
	}

	get("/solution") {
		contentType = formats("json")
		val user = scentry.user.id.get
		val dbSolutions = db.getSolutions(user)
		val dbTrucks = db.getTrucks(user)
		val stops = db.getStops(user)
		val depots = db.getDepots(user)

		val lm = new LocationMatrix(stops, depots)

		val trucks = dbTrucks.map {
			dbTruck =>
				dbTruck.toTruck(stops, depots.head, lm: LocationMatrix)
		}

		dbSolutions.map {
			dbSolution =>
				dbSolution.toSolution(depots.head, stops, trucks)
		}



	}

	get("/solution/:id") {
		contentType = formats("json")
		val user = scentry.user.id.get
		getSolution(params("id").toInt)

	}

	////////////////////

	post("/solution/truck/:id") {

	}

	post("/solution/stop/:id") {

	}

	get("/solution/run/:id") {
		contentType = formats("json")

		getSolution(params("id").toInt).map {
			solution =>
				solution.shuffle
		}
	}



	//****************************** OTHER *************************


	get("/") {
		contentType = "text/html"
		ssp("/home")
	}

	error {
		case e: SizeConstraintExceededException => RequestEntityTooLarge("file is too big")
	}

	private def getSolution(id: Int): Option[Solution] = {
		val user = scentry.user.id.get
		//val dbSolutions = db.getSolutions(user)
		val dbTrucks = db.getTrucks(user)
		val stops = db.getStops(user)
		val depots = db.getDepots(user)

		val lm = new LocationMatrix(stops, depots)

		val trucks = dbTrucks.map {
			dbTruck =>
				dbTruck.toTruck(stops, depots.head, lm: LocationMatrix)
		}

		val solution = db.getSolution(id, user)

		solution.map {
			solution =>
				solution.toSolution(depots.head, stops, trucks)
		}
	}

}
