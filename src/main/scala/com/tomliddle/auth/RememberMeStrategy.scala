package com.tomliddle.auth

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import com.tomliddle.database.{DatabaseSupport, User}
import com.tomliddle.util.Logging
import org.scalatra.auth.ScentryStrategy
import org.scalatra.{CookieOptions, ScalatraBase}
import org.slf4j.LoggerFactory

class RememberMeStrategy(protected val app: ScalatraBase, db: DatabaseSupport)(implicit request: HttpServletRequest, response: HttpServletResponse)
		extends ScentryStrategy[User] with Logging {

	override def name: String = "RememberMe"

	private val COOKIE_KEY = "rememberMe"
	private val oneWeek = 7 * 24 * 3600

	/** *
	  * Grab the value of the rememberMe cookie token.
	  */
	private def tokenVal = {
		app.cookies.get(COOKIE_KEY) match {
			case Some(token) => token
			case None => ""
		}
	}

	/** *
	  * Determine whether the strategy should be run for the current request.
	  */
	override def isValid(implicit request: HttpServletRequest): Boolean = {
		logg.info("RememberMeStrategy: determining isValid: " + (tokenVal != "").toString)
		tokenVal != ""
	}

	/** *
	  * In a real application, we'd check the cookie's token value against a known hash, probably saved in a
	  * datastore, to see if we should accept the cookie's token. Here, we'll just see if it's the one we set
	  * earlier ("foobar") and accept it if so.
	  */
	def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse) = {
		logg.info("RememberMeStrategy: attempting authentication")
		if (tokenVal == "foobar") {
			//db.getUser("tom@gmail.com")
			None
		}
		else None
	}

	/**
	 * What should happen if the user is currently not authenticated?
	 */
	override def unauthenticated()(implicit request: HttpServletRequest, response: HttpServletResponse) {
		//app.redirect("/sessions/new")
		val i = 0
	}

	/** *
	  * After successfully authenticating with either the RememberMeStrategy, or the UserPasswordStrategy with the
	  * "remember me" tickbox checked, we set a rememberMe cookie for later use.
	  *
	  * NB make sure you set a cookie path, or you risk getting weird problems because you've accidentally set
	  * more than 1 cookie.
	  */
	override def afterAuthenticate(winningStrategy: String, user: User)(implicit request: HttpServletRequest, response: HttpServletResponse) = {
		logg.info("rememberMe: afterAuth fired")
		if (winningStrategy == "RememberMe" ||
				(winningStrategy == "UserPassword" && checkbox2boolean(app.params.get("rememberMe").getOrElse("")))) {

			val token = "foobar"
			app.cookies.set(COOKIE_KEY, token)(CookieOptions(maxAge = oneWeek, path = "/"))
		}
	}

	/**
	 * Run this code before logout, to clean up any leftover database state and delete the rememberMe token cookie.
	 */
	override def beforeLogout(user: User)(implicit request: HttpServletRequest, response: HttpServletResponse) = {
		logg.info("rememberMe: beforeLogout")
		if (user != null) {
			user.forgetMe
		}
		app.cookies.delete(COOKIE_KEY)(CookieOptions(path = "/"))
	}


	/**
	 * Used to easily match a checkbox value
	 */
	private def checkbox2boolean(s: String): Boolean = {
		s match {
			case "yes" => true
			case "y" => true
			case "1" => true
			case "true" => true
			case _ => false
		}
	}
}

