package com.tomliddle

import org.scalatra.ScalatraBase
import org.scalatra.auth.{ScentryConfig, ScentrySupport}
import org.slf4j.LoggerFactory


trait AuthenticationSupport extends ScalatraBase with ScentrySupport[User] {
	self: ScalatraBase =>

	protected def fromSession = { case id: String => User(id.toString, "")  }
	protected def toSession   = { case usr: User => usr.id.toString }

	protected val scentryConfig = (new ScentryConfig {
		override val login = "/sessions/new"
	}).asInstanceOf[ScentryConfiguration]

	protected val logger = LoggerFactory.getLogger(getClass)

	protected def requireLogin() = {
		if(!isAuthenticated) {
			redirect(scentryConfig.login)
		}
	}

	/**
	 * If an unauthenticated user attempts to access a route which is protected by Scentry,
	 * run the unauthenticated() method on the UserPasswordStrategy.
	 */
	override protected def configureScentry = {
		scentry.unauthenticated {
			scentry.strategies("UserPassword").unauthenticated()
		}
	}

	/**
	 * Register auth strategies with Scentry. Any controller with this trait mixed in will attempt to
	 * progressively use all registered strategies to log the user in, falling back if necessary.
	 */
	override protected def registerAuthStrategies = {
		scentry.register("UserPassword", app => new UserPasswordStrategy(app))
		scentry.register("RememberMe", app => new RememberMeStrategy(app))
	}
}



/*
class MyBasicAuthStrategy(protected override val app: ScalatraBase, realm: String) extends BasicAuthStrategy[User](app, realm) {

	protected def validate(userName: String, password: String): Option[User] = {
		if(userName == "scalatra" && password == "scalatra") Some(User("scalatra", "test"))
		else None
	}

	protected def validate(userName: String, password: String)(http: HttpServletRequest, response: HttpServletResponse): Option[User] = {
		if(userName == "scalatra" && password == "scalatra") Some(User("scalatra", "test"))
		else None
	}

	protected def getUserId(user: User): String = user.name

	protected def getUserId(user: User)(http: HttpServletRequest, response: HttpServletResponse): String = user.name

}


trait AuthenticationSupport extends ScentrySupport[User] with BasicAuthSupport[User] {
	self: ScalatraBase =>

	val realm = "Scalatra Basic Auth Example"

	protected def fromSession = { case id: Int => User(id)  }
	protected def toSession   = { case usr: User => usr.id }

	protected val scentryConfig = (new ScentryConfig {}).asInstanceOf[ScentryConfiguration]


	override protected def configureScentry = {
		scentry.unauthenticated {
			scentry.strategies("Basic").unauthenticated()
		}
	}

	override protected def registerAuthStrategies = {
		scentry.register("Basic", app => new MyBasicAuthStrategy(app, realm))
	}
}*/
