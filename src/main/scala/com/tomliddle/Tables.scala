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
	def userId: Column[Int] = column[Int]("userId")
	def id: Column[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

	def * = (name, startTime, endTime, maxWeight, userId, id.?) <>(Truck.tupled, Truck.unapply)
}

class Depots(tag: Tag) extends Table[Depot](tag, "DEPOTS") with TypeConvert {
	def name: Column[String] = column[String]("name", O.NotNull)
	def locationId: Column[Int] = column[Int]("locationId", O.NotNull)
	def userId: Column[Int] = column[Int]("userId")
	def id: Column[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

	def * = (name, locationId, userId, id.?) <>(Depot.tupled, Depot.unapply)
}

class Stops(tag: Tag) extends Table[Stop](tag, "STOPS") with TypeConvert {

	//(location: Point, startTime: DateTime, endTime: DateTime, maxWeight: Double, specialCodes: List[String], id: Option[Int] = None)	def location: Column[String] = column[String]("location", O.NotNull)
	def name: Column[String] = column[String]("name", O.NotNull)
	def location: Column[Location] = column[Location]("location")
	def startTime: Column[LocalTime] = column[LocalTime]("startTime")
	def endTime: Column[LocalTime] = column[LocalTime]("endTime")
	def maxWeight: Column[BigDecimal] = column[BigDecimal]("maxWeight")
	def specialCodes: Column[List[String]] = column[List[String]]("specialCodes")
	def userId: Column[Int] = column[Int]("userId")
	def id: Column[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

	def * = (name, locationId, startTime, endTime, maxWeight, specialCodes, userId, id.?) <>(Stop.tupled, Stop.unapply)
}


/*class Solutions(tag: Tag) extends Table[Solution](tag, "SOLUTIONS") with TypeConvert {

	def name: Column[String] = column[String]("name", O.NotNull)
	def userId: Column[Int] = column[Int]("userId")
	def id: Column[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

	def * = (name, userId, id.?) <>(Solution.tupled, Solution.unapply)
}*/


object Tables {
	val users: TableQuery[Users] = TableQuery[Users]
	val locations: TableQuery[Locations] = TableQuery[Locations]
	val trucks: TableQuery[Trucks] = TableQuery[Trucks]
	val depots: TableQuery[Depots] = TableQuery[Depots]
	val stops: TableQuery[Stops] = TableQuery[Stops]
	//val solutions: TableQuery[Solutions] = TableQuery[Solutions]
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
			trucks.filter {truck => truck.id === id && truck.userId === userId}.firstOption
		}
	}

	def getTrucks(userId: Int): List[Truck] = {
		db.withDynSession {
			trucks.filter {truck => truck.userId === userId}.list
		}
	}

	def addTruck(truck: Truck) = {
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
	/*private def joinLocation(sl: Option[(Stop, Location)]): Option[Stop] = {
		sl match {
			case Some(sl: (Stop, Location)) =>
				sl._1.location = sl._2
				Some(sl._1)
			case None => None
		}
	}*/

	def getStop(id: Int, userId: Int): Option[Stop] = {
		db.withDynSession {
			val explicitCrossJoin = for {
				(s, l) <- stops innerJoin locations on (_.locationId === _.id)
			} yield (s, l)

			val opt: Option[(Stop, Location)] = explicitCrossJoin.list.filter { sl: (Stop, Location)  => sl._1.id == id && sl._1.userId == userId }.headOption //sl => sl._1.id === id && sl._1.userId === userId

			opt match {
				case Some(slFound: (Stop, Location)) => {
					slFound._1.location = slFound._2
					Some(slFound._1)
				}
				case None => None
			}
		}
	}

	def getStops(userId: Int): List[Stop] = {
		db.withDynSession {
			val explicitCrossJoin = for {
				(s, l) <- stops innerJoin locations on (_.locationId === _.id)
			} yield (s, l)

			explicitCrossJoin.list.filter{sl: (Stop, Location) => sl._1.userId == userId}.map {
				sl: (Stop, Location) => {
					sl._1.location = sl._2
					sl._1
				}
			}
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
			val explicitCrossJoin = for {
				(s, l) <- depots innerJoin locations on (_.locationId === _.id)
			} yield (s, l)

			explicitCrossJoin.list.map {
				sl: (Depot, Location) => {
					sl._1.location = sl._2
					sl._1
				}
			}
		}
	}

	//************************ Solution ***********************************
	/*def getSolution(id: Int, userId: Int): Option[Solution] = {
		db.withDynSession {
			solutions.filter {solution => solution.id === id}.firstOption
		}
	}

	def getSolutions(userId: Int): List[Solution] = {
		db.withDynSession {
			solutions.list
		}
	}

	def addSolution(solution: Solution) = {
		db.withDynSession {
			solutions += solution
		}
	}

	def deleteSolution(id: Int, userId: Int) = {
		db.withDynSession {
			solutions.filter {solution => solution.id === id}.delete
		}
	}*/


}