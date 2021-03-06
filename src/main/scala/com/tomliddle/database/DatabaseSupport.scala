package com.tomliddle.database

import com.tomliddle.entity.{Stop, Depot}
import scala.slick.driver.H2Driver.simple._
import com.tomliddle.database.Tables.{depots, users, trucks, stops}
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession

	/**
	* DB support for Slick database
	* @param db the slick database
	*/
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
			users.filter { user => user.email === email && user.passwordHash === password }.firstOption
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

	def getTrucks(userId: Int): IndexedSeq[DBTruck] = {
		db.withDynSession {
			trucks.filter { truck => truck.userId === userId }.list.toIndexedSeq
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

	def getStops(userId: Int): IndexedSeq[Stop] = {
		db.withDynSession {
			stops.filter { stop => stop.userId === userId }.list.toIndexedSeq
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
	def getDepots(userId: Int): IndexedSeq[Depot] = {
		db.withDynSession {
			depots.filter { depot => depot.userId === userId }.list.toIndexedSeq
		}
	}
}
