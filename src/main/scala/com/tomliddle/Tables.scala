package com.tomliddle

import java.sql.Time

import Tables._
import solution.Geocoding
import solution._

import org.joda.time.LocalTime

import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted.TableQuery

trait TypeConvert {

	implicit def localTime = MappedColumnType.base[LocalTime, Time](dt => new Time(dt.getMillisOfDay), ts => new LocalTime(ts.getTime))

	implicit def listToString = MappedColumnType.base[List[String], String](
		dt => dt.foldLeft(""){(a, b) => s"$a,$b"},
		ts => List("", "")
	)
}

case class User(email: String, name: String, passwordHash: String, id: Option[Int] = None) {
	def forgetMe = {
		//logger.info("User: this is where you'd invalidate the saved token in you User model")
	}
}

class Users(tag: Tag) extends Table[User](tag, "USERS") {
	def email: Column[String] = column[String]("email", O.NotNull)
	def name: Column[String] = column[String]("name", O.NotNull)
	def passwordHash: Column[String] = column[String]("password_hash", O.NotNull)
	def id: Column[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

	// the * projection (e.g. select * ...) auto-transforms the tupled
	// column values to / from a User
	def * = (email, name, passwordHash, id.?) <>(User.tupled, User.unapply)
}

// ************************ TRUCK STUFF ***************************************

case class DBTruck(name: String, startTime: LocalTime, endTime: LocalTime, maxWeight: BigDecimal, userId: Int, id: Option[Int] = None) {
	def toTruck(stops: List[Stop], depot: Depot, lm: LocationMatrix): Truck = {
		Truck(name, startTime, endTime, maxWeight, depot, stops, lm, userId, id)
	}
}

class Trucks(tag: Tag) extends Table[DBTruck](tag, "TRUCKS") with TypeConvert {
	def name: Column[String] = column[String]("name", O.NotNull)
	def startTime: Column[LocalTime] = column[LocalTime]("startTime")
	def endTime: Column[LocalTime] = column[LocalTime]("endTime")
	def maxWeight: Column[BigDecimal] = column[BigDecimal]("maxWeight")
	def userId: Column[Int] = column[Int]("userId")
	def id: Column[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

	def * = (name, startTime, endTime, maxWeight, userId, id.?) <>(DBTruck.tupled, DBTruck.unapply)
}

class Depots(tag: Tag) extends Table[Depot](tag, "DEPOTS") with TypeConvert {
	def name: Column[String] = column[String]("name", O.NotNull)
	def x: Column[BigDecimal] = column[BigDecimal]("x")
	def y: Column[BigDecimal] = column[BigDecimal]("y")
	def address: Column[String] = column[String]("address")
	def userId: Column[Int] = column[Int]("userId")
	def id: Column[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

	def * = (name, x, y, address, userId, id.?) <>(Depot.tupled, Depot.unapply)
}


class Stops(tag: Tag) extends Table[Stop](tag, "STOPS") with TypeConvert {

	def name: Column[String] = column[String]("name", O.NotNull)
	def x: Column[BigDecimal] = column[BigDecimal]("x")
	def y: Column[BigDecimal] = column[BigDecimal]("y")
	def address: Column[String] = column[String]("address")
	def startTime: Column[LocalTime] = column[LocalTime]("startTime")
	def endTime: Column[LocalTime] = column[LocalTime]("endTime")
	def maxWeight: Column[BigDecimal] = column[BigDecimal]("maxWeight")
	def specialCodes: Column[List[String]] = column[List[String]]("specialCodes")
	def userId: Column[Int] = column[Int]("userId")
	def id: Column[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

	def * = (name, x, y, address, startTime, endTime, maxWeight, specialCodes, userId, id.?) <>(Stop.tupled, Stop.unapply)
}

case class DBSolution(name: String, userId: Int, id: Option[Int] = None) {
	def toSolution(depot: Depot, stopsToLoad: List[Stop], trucks: List[Truck]) = {
		Solution(name, depot, stopsToLoad, trucks, userId, id)
	}
}

class Solutions(tag: Tag) extends Table[DBSolution](tag, "SOLUTIONS") with TypeConvert {

	def name: Column[String] = column[String]("name", O.NotNull)
	def userId: Column[Int] = column[Int]("userId")
	def id: Column[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

	def * = (name, userId, id.?) <>(DBSolution.tupled, DBSolution.unapply)
}


object Tables {
	val users: TableQuery[Users] = TableQuery[Users]
	val trucks: TableQuery[Trucks] = TableQuery[Trucks]
	val depots: TableQuery[Depots] = TableQuery[Depots]
	val stops: TableQuery[Stops] = TableQuery[Stops]
	val solutions: TableQuery[Solutions] = TableQuery[Solutions]
}

class DatabaseSupport(db: Database) extends Geocoding {

	import Database.dynamicSession

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
			users.filter { user => (user.email === email && user.passwordHash === password)}.firstOption
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
			trucks.filter {truck => truck.id === id && truck.userId === userId}.firstOption
		}
	}

	def getTrucks(userId: Int): List[DBTruck] = {
		db.withDynSession {
			trucks.filter {truck => truck.userId === userId}.list
		}
	}

	def addTruck(truck: DBTruck) = {
		db.withDynSession {
			trucks += truck
		}
	}

	def deleteTruck(id: Int, userId: Int) = {
		db.withDynSession {
			trucks.filter {truck => truck.id === id && truck.userId === userId}.delete
		}
	}

	//************************ Stops ***********************************
	def getStop(id: Int, userId: Int): Option[Stop] = {
		db.withDynSession {
			stops.filter {stop => stop.id === id && stop.userId === userId}.firstOption
		}
	}

	def getStops(userId: Int): List[Stop] = {
		db.withDynSession {
			stops.filter {stop => stop.userId === userId}.list
		}
	}

	def addStop(stop: Stop) = {
		db.withDynSession {
			stops += stop
		}
	}

	def deleteStop(id: Int, userId: Int) = {
		db.withDynSession {
			stops.filter {stop => stop.id === id && stop.userId === userId}.delete
		}
	}

	//************************ Depots ***********************************
	def getDepots(userId: Int): List[Depot] = {
		db.withDynSession {
			depots.filter {depot => depot.userId === userId}.list
		}
	}

	//************************ Solution ***********************************
	def getSolution(id: Int, user: Int): Option[Solution] = {

		val dbTrucks = getTrucks(user)
		val stops = getStops(user)
		val depots = getDepots(user)

		val lm: LocationMatrix = new LocationMatrix(stops, depots) with LatLongTimeAndDistCalc

		val trucks = dbTrucks.map {
			dbTruck =>
				dbTruck.toTruck(stops, depots.head, lm)
		}

		db.withDynSession {
			val solution = solutions.filter {solution => solution.id === id}.firstOption
			solution.map {
				solution =>
					solution.toSolution(depots.head, stops, trucks)
			}
		}
	}


	def getSolutions(userId: Int): List[DBSolution] = {
		db.withDynSession {
			solutions.filter {solution => solution.userId === userId}.list
		}
	}

	def addSolution(solution: DBSolution) = {
		db.withDynSession {
			solutions += solution
		}
	}

	def deleteSolution(id: Int, userId: Int) = {
		db.withDynSession {
			solutions.filter {solution => solution.id === id && solution.userId === userId}.delete
		}
	}


}