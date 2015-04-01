package com.tomliddle.database

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoClient}
import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import com.tomliddle.solution.Solution
import org.slf4j.LoggerFactory

class MongoSupport(databaseName: String){

	private final val logger = LoggerFactory.getLogger(this.getClass)

	private val mongoClient = MongoConnection()//MongoClient("127.0.0.1", 27017)
	private val mongoDb = mongoClient(databaseName)
	private val mongoSolutions = mongoDb("solutions")

	// TODO massive hack
	private val jodaLocalTimeSerializer = new LocalTimeConverter
	private val bigDecimalSerializer = new BigDecimalConverter

	def getMSolutions(userId: Int): List[Solution] = {
		val q = MongoDBObject("userId" -> userId)

		val users = for (x <- mongoSolutions.find(q)) yield x
		logger.debug("size is sdfsdf" + users.size.toString)
		val x = users.map {
			obj => {
				logger.debug("size ssss " + obj.toString)
				val y = grater[Solution].asObject(obj)
				logger.debug("size ssss " + y.toString)
				y
			}
		}

		logger.debug("size is " + x.size.toString)

		x.toList
	}

	def getMSolution(userId: Int, id: Int): Option[Solution] = {
		val q = MongoDBObject("userId" -> userId, "id" -> id)
		val obj: Option[DBObject] = mongoSolutions.findOne(q)

		obj match {
			case Some(dbSol) => {
				val sol = grater[Solution].asObject(dbSol)
				if (sol.userId == userId) Some(sol)
				else None
			}
			case _ => None
		}
	}

	def addMSolution(solution: Solution) {
		val dbo = grater[Solution].asDBObject(solution)
		mongoSolutions += dbo
		logger.debug(s"size is ${mongoSolutions.size.toString} after adding")
	}

	def removeMSolutions(userId: Int) {
		val q = MongoDBObject("userId" -> userId)
		mongoSolutions -= q
		logger.debug(s"size is ${mongoSolutions.size.toString} after removal")
	}
}

