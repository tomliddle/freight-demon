package com.tomliddle.controllers

import com.tomliddle.auth.AuthenticationSupport
import com.tomliddle.database.{DatabaseSupport, User}

	/**
	* Handles authentication check, and redirects according to authentication status.
	* @param db
	*/
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

/**
	* Simple resource controller which doesn't need authentication
	*/
class ResourceController extends ScalateServlet {
}