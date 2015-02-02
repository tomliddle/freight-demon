package com.tomliddle.controllers

import java.util.concurrent.TimeUnit

import _root_.akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.tomliddle.solution._
import com.tomliddle.{User, DatabaseSupport}
import com.tomliddle.auth.AuthenticationSupport
import org.joda.time.DateTime
import org.json4s._
import org.scalatra._
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.servlet.{FileUploadSupport, MultipartConfig, SizeConstraintExceededException}

import scala.concurrent.ExecutionContext

class SecureController(protected val db: DatabaseSupport, system: ActorSystem, myActor: ActorRef)
		extends ScalateServlet with FutureSupport with FileUploadSupport with AuthenticationSupport with JacksonJsonSupport {

	configureMultipartHandling(MultipartConfig(maxFileSize = Some(3 * 1024 * 1024)))

	protected implicit val timeout = Timeout(5, TimeUnit.SECONDS)
	protected implicit val jsonFormats: Formats = DefaultFormats

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

		val truck = Truck(name, new DateTime(startTime), new DateTime(endTime), BigDecimal(maxWeight))

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


		val stop = Stop(name, location, new DateTime(startTime), new DateTime(endTime), BigDecimal(maxWeight), List())

		db.addStop(stop)
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




	//****************************** OTHER *************************
	get("/") {
		contentType = "text/html"
		ssp("/home")
	}


	error {
		case e: SizeConstraintExceededException => RequestEntityTooLarge("file is too big")
	}
}

class SessionsController(protected val db: DatabaseSupport) extends ScalateServlet with AuthenticationSupport {
	before("/new") {
		logger.info("SessionsController: checking whether to run RememberMeStrategy: " + !isAuthenticated)

		if (!isAuthenticated) {
			scentry.authenticate("RememberMe")
		}
	}

	get("/new") {
		if (isAuthenticated) redirect("/")
		else {
			contentType = "text/html"
			ssp("/sessions/new")
		}
	}

	post("/") {
		checkAuthentication()
	}


	//*********************** LOGINS ETC ***************************
	post("/register") {
		db.addUser(User(params("login"), params("name"), params("password")))
		checkAuthentication()
	}

	private def checkAuthentication() = {
		scentry.authenticate()

		if (isAuthenticated) {
			redirect("/")
		} else {
			redirect("/sessions/new")
		}
	}

	// Never do this in a real app. State changes should never happen as a result of a GET request. However, this does
	// make it easier to illustrate the logout code.
	get("/logout") {
		scentry.logout()
		redirect("/")
	}

}


class ResourceController extends ScalateServlet {
}