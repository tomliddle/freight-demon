package com.tomliddle.auth

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import com.tomliddle.database.{DatabaseSupport, User}
import com.tomliddle.util.Logging
import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentryStrategy
import org.slf4j.LoggerFactory

/**
	* Provides the user password authentication strategy from the database.
	* @param app
	* @param db
	*/
class UserPasswordStrategy(protected val app: ScalatraBase, db: DatabaseSupport)(implicit request: HttpServletRequest, response: HttpServletResponse)
		extends ScentryStrategy[User] with Logging {

	private def login = app.params.getOrElse("login", "")
	private def password = app.params.getOrElse("password", "")
	override def name: String = "UserPassword"

	/**
	  * Determine whether the strategy should be run for the current request.
	  */
	override def isValid(implicit request: HttpServletRequest) = {
		logg.info("UserPasswordStrategy: determining isValid: " + (login != "" && password != "").toString())
		login != "" && password != ""
	}

	/**
	 * In real life, this is where we'd consult our data store, asking it whether the user credentials matched
	 * any existing user. Here, we'll just check for a known login/password combination and return a user if
	 * it's found.
	 */
	def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse): Option[User] = {
		logg.info("UserPasswordStrategy: attempting authentication")
		db.getUser(login, password)
	}

	/**
	 * What should happen if the user is currently not authenticated?
	 */
	override def unauthenticated()(implicit request: HttpServletRequest, response: HttpServletResponse) {
		app.redirect("/sessions/new")
	}

}

