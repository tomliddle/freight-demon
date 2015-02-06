package com.tomliddle.controllers

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, ActorRef}
import akka.util.Timeout
import com.tomliddle.DatabaseSupport
import com.tomliddle.auth.AuthenticationSupport
import com.tomliddle.solution.{Stop, Truck}
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.json4s.{Formats, DefaultFormats}
import org.json4s.ext.JodaTimeSerializers
import org.scalatra.{RequestEntityTooLarge, FutureSupport}
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.servlet.{SizeConstraintExceededException, FileUploadSupport, MultipartConfig}
import scala.concurrent.ExecutionContext

class SecureController(protected val db: DatabaseSupport, system: ActorSystem, myActor: ActorRef)
		extends ScalateServlet with FutureSupport with FileUploadSupport with AuthenticationSupport with JacksonJsonSupport {

	configureMultipartHandling(MultipartConfig(maxFileSize = Some(3 * 1024 * 1024)))

	private final val formatter = DateTimeFormat.forPattern("HH:mm")

	protected implicit val timeout = Timeout(5, TimeUnit.SECONDS)
	protected implicit val jsonFormats: Formats = {
		DefaultFormats
		//+ CustomSerializer
		//+ JodaTimeSerializers
	}

	protected implicit def executor: ExecutionContext = system.dispatcher


	before() {
		requireLogin()
	}

	//************** Truck HANDLING ******************************
	// Add truck
	post("/truck") {
		val name = params("name")
		val startTime = params("startTime")
		val endTime = params("endTime")
		val maxWeight = params("maxWeight")

		val truck = Truck(name, LocalTime.parse(startTime, formatter), LocalTime.parse(endTime), BigDecimal(maxWeight), scentry.user.id.get)

		db.addTruck(truck)
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
		val name = params("name")
		val startTime = params("startTime")
		val endTime = params("endTime")
		val maxWeight = params("maxWeight")
		val postcode = params("postcode")

		db.getLocation(postcode).foreach {
			location =>
				val stop = Stop(name, location.id.get, LocalTime.parse(startTime, formatter), LocalTime.parse(endTime), BigDecimal(maxWeight), List(), scentry.user.id.get)
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
