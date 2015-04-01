package com.tomliddle

package com.tomliddle

import akka.actor.{Actor, ActorLogging}
import database.Users

import scala.concurrent.ExecutionContext


class Worker extends Actor with ActorLogging {

	implicit val ec = ExecutionContext.Implicits.global

	def receive = {
		case status: Users ⇒ {


		}
	}
}