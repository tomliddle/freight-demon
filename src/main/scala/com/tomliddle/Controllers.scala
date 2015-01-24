package com.tomliddle

import java.util.concurrent.TimeUnit

import _root_.akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import Tables._
import org.json4s._
import org.scalatra._
import org.scalatra.servlet.{FileUploadSupport, MultipartConfig, SizeConstraintExceededException}

import scala.concurrent.ExecutionContext
import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession

class SecureController(db: DatabaseSupport, system: ActorSystem, myActor: ActorRef)
		extends ScalateServlet with FutureSupport with FileUploadSupport with AuthenticationSupport {

	configureMultipartHandling(MultipartConfig(maxFileSize = Some(3 * 1024 * 1024)))

	protected implicit val timeout = Timeout(5, TimeUnit.SECONDS)
	protected implicit val jsonFormats: Formats = DefaultFormats

	protected implicit def executor: ExecutionContext = system.dispatcher


	before() {
		requireLogin()
	}


	//*********************** LOGINS ETC ***************************


	post("/register") {
		db.addUser(User(params("email"), params("name"), params("password")))
	}


	// Edit profile

	//************** IMAGE HANDLING ******************************
	put("/image/:name") {
		val name = params("name")
		def file = fileParams("image-file")
		// Return image id
		db.addImage(Image(name, file.get, 1))

	}

/*	get("/image/:id") {
			val imageId = params("id")
			db withDynSession {
				images.filter(_.id === imageId.toInt).firstOption
			}
		}

	get("/image/delete/:id") {
		val imageId = params("id")
		db withDynSession {
			images.filter(_.id === imageId.toInt).delete
		}
	}

	get("/images") {
		db. withDynSession {
			images.filter(_.userId === 1)
		}
	}*/

	get("/") {
		contentType = "text/html"
		ssp("/home")
	}


	error {
		case e: SizeConstraintExceededException => RequestEntityTooLarge("file is too big")
	}
}

class SessionsController extends ScalateServlet with AuthenticationSupport {
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


/*	protected def basicAuth() = {
		val baReq = new BasicAuthStrategy.BasicAuthRequest(request)
		if(!baReq.providesAuth) {
			response.setHeader("WWW-Authenticate", "Basic realm=\"%s\"" format realm)
			halt(401, "Unauthenticated")
		}
		if(!baReq.isBasicAuth) {
			halt(400, "Bad Request")
		}
		scentry.authenticate("Basic")
	}*/

/*	override def unauthenticated() {
		response.setHeader("WWW-Authenticate", challenge)
		halt(401, "Unauthenticated")
	}*/