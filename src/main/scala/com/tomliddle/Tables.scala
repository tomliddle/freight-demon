package com.tomliddle

import java.sql.{Time, Timestamp}

import solution._
import org.joda.time.{LocalTime, DateTime}
import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted.TableQuery
import Tables._

trait TypeConvert {
	implicit def pointConvert = MappedColumnType.base[Location, String] (
		dt => s"${dt.x},${dt.y},${dt.postcode}",
		ts => {
			val str = ts.split(",")
			new Location(BigDecimal(str(0)), BigDecimal(str(1)), str(2))
		}
	)

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

//************************** POSTCODE *****************************************
class Locations(tag: Tag) extends Table[Location](tag, "LOCATIONS") {
	def x: Column[BigDecimal] = column[BigDecimal]("x", O.NotNull)
	def y: Column[BigDecimal] = column[BigDecimal]("y", O.NotNull)
	def postcode: Column[String] = column[String]("postcode", O.NotNull)
	def id: Column[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

	// the * projection (e.g. select * ...) auto-transforms the tupled
	// column values to / from a User
	def * = (x, y, postcode, id.?) <>(Location.tupled, Location.unapply)
}


// ************************ TRUCK STUFF ***************************************

//case class DBTruck(name: String, startTime: DateTime, endTime: DateTime, maxWeight: BigDecimal, id: Option[Int] = None)

class Trucks(tag: Tag) extends Table[Truck](tag, "TRUCKS") with TypeConvert {
	def name: Column[String] = column[String]("name", O.NotNull)
	def startTime: Column[LocalTime] = column[LocalTime]("startTime")
	def endTime: Column[LocalTime] = column[LocalTime]("endTime")
	def maxWeight: Column[BigDecimal] = column[BigDecimal]("maxWeight")
	def id: Column[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

	def * = (name, startTime, endTime, maxWeight, id.?) <>(Truck.tupled, Truck.unapply)
}

class Depots(tag: Tag) extends Table[Depot](tag, "DEPOTS") with TypeConvert {
	def name: Column[String] = column[String]("name", O.NotNull)
	def location: Column[Location] = column[Location]("location", O.NotNull)
	def id: Column[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
	
	def * = (name, location, id.?) <>(Depot.tupled, Depot.unapply)
}

class Stops(tag: Tag) extends Table[Stop](tag, "STOPS") with TypeConvert {
	
	//(location: Point, startTime: DateTime, endTime: DateTime, maxWeight: Double, specialCodes: List[String], id: Option[Int] = None)	def location: Column[String] = column[String]("location", O.NotNull)
	def name: Column[String] = column[String]("name", O.NotNull)
	def location: Column[Location] = column[Location]("location")
	def startTime: Column[LocalTime] = column[LocalTime]("startTime")
	def endTime: Column[LocalTime] = column[LocalTime]("endTime")
	def maxWeight: Column[BigDecimal] = column[BigDecimal]("maxWeight")
	def specialCodes: Column[List[String]] = column[List[String]]("specialCodes")
	def id: Column[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

	// the * projection (e.g. select * ...) auto-transforms the tupled
	// column values to / from a User
	def * = (name, location, startTime, endTime, maxWeight, specialCodes, id.?) <>(Stop.tupled, Stop.unapply)
}


object Tables {
	val users: TableQuery[Users] = TableQuery[Users]
	val locations: TableQuery[Locations] = TableQuery[Locations]
	val trucks: TableQuery[Trucks] = TableQuery[Trucks]
	val depots: TableQuery[Depots] = TableQuery[Depots]
	val stops: TableQuery[Stops] = TableQuery[Stops]
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

	//************************ LOCATIONS ***********************************
	def getLocation(postcode: String): Option[Location] = {
		db.withDynSession {
			locations.filter(_.postcode === postcode).firstOption match {
				case Some(location) => Some(location)
				case None => {
					geocodeFromOnline(postcode) match {
						case Some(location: Location) => {
							locations += location
							Some(location)
						}
						case None => None
					}
				}
			}
		}
	}

	def addLocation(location: Location): Unit = {
		db.withDynSession {
			if (getLocation(location.postcode).isEmpty)
				locations += location
		}
	}

	//************************ Trucks ***********************************
	def getTruck(id: Int, userId: Int): Option[Truck] = {
		db.withDynSession {
			trucks.filter {truck => truck.id === id}.firstOption
		}
	}

	def getTrucks(userId: Int): List[Truck] = {
		db.withDynSession {
			trucks.list
		}
	}

	def addTruck(truck: Truck) = {
		db.withDynSession {
			trucks += truck
		}
	}

	def deleteTruck(id: Int, userId: Int) = {
		db.withDynSession {
			trucks.filter {truck => truck.id === id}.delete
		}
	}

	//************************ Stops ***********************************
	def getStop(id: Int, userId: Int): Option[Stop] = {
		db.withDynSession {
			stops.filter {stop => stop.id === id}.firstOption
		}
	}

	def getStops(userId: Int): List[Stop] = {
		db.withDynSession {
			stops.list
		}
	}

	def addStop(stop: Stop) = {
		db.withDynSession {
			stops += stop
		}
	}

	def deleteStop(id: Int, userId: Int) = {
		db.withDynSession {
			stops.filter {stop => stop.id === id}.delete
		}
	}


}