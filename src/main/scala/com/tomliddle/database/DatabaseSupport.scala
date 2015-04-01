package com.tomliddle.database

import com.tomliddle.solution.{LocationMatrix, Solution, Depot, Stop}
import scala.slick.driver.H2Driver.simple._
import com.tomliddle.database.Tables.{depots, users, trucks, stops, solutions}
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession


class DatabaseSupport(db: Database) {

	//************************ USERS ***********************************
	def getUser(id: Int): Option[User] = {
		db.withDynSession {
			users.filter(_.id === id).firstOption
		}
	}

	def getUser(email: String): Option[User] = {
		db.withDynSession {
			users.filter(_.email === email).firstOption
		}
	}

	//n.b. login == email
	def getUser(email: String, password: String): Option[User] = {
		db.withDynSession {
			users.filter { user => (user.email === email && user.passwordHash === password) }.firstOption
		}
	}

	def addUser(user: User): Unit = {
		db.withDynSession {
			users += user
		}
	}


	//************************ Trucks ***********************************
	def getTruck(id: Int, userId: Int): Option[DBTruck] = {
		db.withDynSession {
			trucks.filter { truck => truck.id === id && truck.userId === userId }.firstOption
		}
	}

	def getTrucks(userId: Int): List[DBTruck] = {
		db.withDynSession {
			trucks.filter { truck => truck.userId === userId }.list
		}
	}

	def addTruck(truck: DBTruck) = {
		db.withDynSession {
			trucks += truck
		}
	}

	def deleteTruck(id: Int, userId: Int) = {
		db.withDynSession {
			trucks.filter { truck => truck.id === id && truck.userId === userId }.delete
		}
	}

	//************************ Stops ***********************************
	def getStop(id: Int, userId: Int): Option[Stop] = {
		db.withDynSession {
			stops.filter { stop => stop.id === id && stop.userId === userId }.firstOption
		}
	}

	def getStops(userId: Int): List[Stop] = {
		db.withDynSession {
			stops.filter { stop => stop.userId === userId }.list
		}
	}

	def addStop(stop: Stop) = {
		db.withDynSession {
			stops += stop
		}
	}

	def deleteStop(id: Int, userId: Int) = {
		db.withDynSession {
			stops.filter { stop => stop.id === id && stop.userId === userId }.delete
		}
	}

	//************************ Depots ***********************************
	def getDepots(userId: Int): List[Depot] = {
		db.withDynSession {
			depots.filter { depot => depot.userId === userId }.list
		}
	}

	//************************ Solution ***********************************
	def getSolution(id: Int, user: Int): Option[Solution] = {

		val dbTrucks = getTrucks(user)
		val stops = getStops(user)
		val depots = getDepots(user)

		val lm: LocationMatrix = new LocationMatrix(stops, depots)

		val trucks = dbTrucks.map {
			dbTruck =>
				dbTruck.toTruck(stops, depots.head, lm)
		}

		db.withDynSession {
			val solution = solutions.filter { solution => solution.id === id }.firstOption
			solution.map {
				solution =>
					solution.toSolution(depots.head, stops, trucks)
			}
		}
	}


	def getSolutions(userId: Int): List[DBSolution] = {
		db.withDynSession {
			solutions.filter { solution => solution.userId === userId }.list
		}
	}

	def addSolution(solution: DBSolution) = {
		db.withDynSession {
			solutions += solution
		}
	}

	def deleteSolution(id: Int, userId: Int) = {
		db.withDynSession {
			solutions.filter { solution => solution.id === id && solution.userId === userId }.delete
		}
	}

}
