package com.tomliddle.controllers

import java.util.concurrent.TimeUnit
import akka.actor.{ActorSystem, ActorRef}
import akka.util.Timeout
import com.tomliddle.DatabaseSupport
import com.tomliddle.auth.AuthenticationSupport
import com.tomliddle.form.{StopForm, TruckForm}
import org.json4s.{Formats, DefaultFormats}
import org.json4s.ext.JodaTimeSerializers
import org.scalatra.{RequestEntityTooLarge, FutureSupport}
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.servlet.{SizeConstraintExceededException, FileUploadSupport, MultipartConfig}
import scala.concurrent.ExecutionContext


class SecureController(protected val db: DatabaseSupport, system: ActorSystem, myActor: ActorRef)
		extends ScalateServlet with FutureSupport with FileUploadSupport with AuthenticationSupport with JacksonJsonSupport {

	configureMultipartHandling(MultipartConfig(maxFileSize = Some(3 * 1024 * 1024)))

	protected implicit val timeout = Timeout(5, TimeUnit.SECONDS)
	protected implicit val jsonFormats: Formats = DefaultFormats ++ JodaTimeSerializers.all

	protected implicit def executor: ExecutionContext = system.dispatcher

	//private implicit def str2localdate(str: String) = LocalTime.parse(str, formatter)



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

	post("/solution/truck/:id") {

	}

	post("/solution/stop/:id") {

	}

	get("/solution") {

	}

	get("/solution/run") {

	}



	//****************************** OTHER *************************
	get("/") {
		contentType = "text/html"
		ssp("/home")
	}

	error {
		case e: SizeConstraintExceededException => RequestEntityTooLarge("file is too big")
	}
}
