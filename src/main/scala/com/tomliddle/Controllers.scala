package com.tomliddle

import java.util.concurrent.TimeUnit

import _root_.akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import Tables._
import auth.AuthenticationSupport
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


	// Edit profile

	//************** IMAGE HANDLING ******************************

	// Add image
	post("/image/add") {
		val name = ""//params("name")
		def file = fileParams("image-file")
		// Return image id
		db.addImage(Image(name, file.get, scentry.user.id.get))
	}

	// Get image
	get("/image/get/:id") {
		contentType = "image/jpeg"
		db.getImage(params("id").toInt, scentry.user.id.get).get.image
	}
	// Delete image
	get("/image/delete/:id") {
		db.deleteImage(params("id").toInt, scentry.user.id.get)
	}

	// Get images for that user
	get("/image/all") {
		contentType = formats("json")
		db.getImages(scentry.user.id.get).map {
			img => img.id.get
		}
	}

	get("/image/list") {
		db.getImageList(scentry.user.id.get)
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