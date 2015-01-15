package com.tomliddle

import akka.actor.{Actor, ActorLogging, Cancellable}
import org.h2.engine.Database
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.math.BigDecimal.RoundingMode
import scala.slick.lifted.TableQuery
import scala.sys.process._
import org.json4s._
import org.json4s.jackson.JsonMethods._


class Worker extends Actor with ActorLogging {

	implicit val ec = ExecutionContext.Implicits.global

	def receive = {
		case status: Users ⇒ {


		}
	}
}
