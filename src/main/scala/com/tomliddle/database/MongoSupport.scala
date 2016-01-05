package com.tomliddle.database

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import com.tomliddle.solution.Solution
import org.slf4j.LoggerFactory

/**
* Salat serialisation for case classes which plugs into Mongo's DBObject
*/
class SolutionDAO extends SalatDAO[Solution, ObjectId](collection = MongoClient()("d")("test_coll"))

/**
* MongoDB Support class
* @param databaseName
*/
class MongoSupport(databaseName: String){

	private final val logger = LoggerFactory.getLogger(this.getClass)

	private val mongoClient = MongoConnection()//MongoClient("127.0.0.1", 27017)
	private val mongoDb = mongoClient(databaseName)
	private val mongoSolutions = mongoDb("solutions")

	implicit def solutionToDBObject(params: Solution): DBObject = grater[Solution].asDBObject(params)
	implicit def solutionFromDBObject(c: DBObject): Solution = grater[Solution].asObject(c)

	private val jodaLocalTimeSerializer = new LocalTimeConverter
	private val bigDecimalSerializer = new BigDecimalConverter
	private val solutionDAO = new SolutionDAO

	def getSolutions(userId: Int): List[Solution] = {
		solutionDAO.find(MongoDBObject("userId" -> userId)).toList
	}

	def getSolution(userId: Int, name: String): Option[Solution] = {
		solutionDAO.findOne(MongoDBObject("userId" -> userId, "name" -> name))
	}

	def addSolution(solution: Solution): Option[ObjectId] = {
		solutionDAO.insert(solution)
	}

	def updateSolution(solution: Solution) {
		solutionDAO.update(MongoDBObject("userId" -> solution.userId, "_id" -> solution._id), solution)
	}

	def removeSolution(userId: Int, name: String) {
		getSolution(userId, name) match {
			case Some(sol) => solutionDAO.remove(MongoDBObject("_id" -> sol._id))
			case None => None
		}
	}

	def removeSolutions(userId: Int) {
		solutionDAO.remove(MongoDBObject("userId" -> userId))
	}
}