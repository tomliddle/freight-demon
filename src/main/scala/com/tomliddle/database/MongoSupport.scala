package com.tomliddle.database

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import com.tomliddle.solution.Solution
import org.slf4j.LoggerFactory

class SolutionDAO extends SalatDAO[Solution, ObjectId](collection = MongoClient()("test_db")("test_coll"))

class MongoSupport(databaseName: String){

	private final val logger = LoggerFactory.getLogger(this.getClass)

	private val mongoClient = MongoConnection()//MongoClient("127.0.0.1", 27017)
	private val mongoDb = mongoClient(databaseName)
	private val mongoSolutions = mongoDb("solutions")

	implicit def solutionToDBObject(params: Solution): DBObject = grater[Solution].asDBObject(params)
	implicit def solutionFromDBObject(c: DBObject): Solution = grater[Solution].asObject(c)

	// TODO massive hack
	private val jodaLocalTimeSerializer = new LocalTimeConverter
	private val bigDecimalSerializer = new BigDecimalConverter
	private val solutionDAO = new SolutionDAO

	def getSolutions(userId: Int): List[Solution] = {
		solutionDAO.find(MongoDBObject("userId" -> userId)).toList
	}

	def getSolution(userId: Int, _id: ObjectId): Option[Solution] = {
		solutionDAO.findOne(MongoDBObject("userId" -> userId, "_id" -> _id))
	}

	def addSolution(solution: Solution): Option[ObjectId] = {
		solutionDAO.insert(solution)
	}

	def updateSolution(solution: Solution) {
		solutionDAO.update(MongoDBObject("userId" -> solution.userId, "_id" -> solution._id), solution)
	}

	def removeSolution(solution: Solution) {
		solutionDAO.remove(solution)
	}

	def removeSolutions(userId: Int) {
		solutionDAO.remove(MongoDBObject("userId" -> userId))
	}
}