package com.tomliddle

import java.sql.Timestamp

import solution._
import org.joda.time.DateTime
import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted.TableQuery
import Tables._

trait TypeConvert {
	implicit def pointConvert = MappedColumnType.base[Point, String] (
		dt => s"${dt.name},${dt.x},${dt.y},${dt.postcode}",
		ts => {
			val str = ts.split(",")
			new Point(str(0), BigDecimal(str(1)), BigDecimal(str(2)), str(3))
		}
	)

	implicit def dateTime = MappedColumnType.base[DateTime, Timestamp](dt => new Timestamp(dt.getMillis), ts => new DateTime(ts.getTime))

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

//case class DBTruck(name: String, startTime: DateTime, endTime: DateTime, maxWeight: BigDecimal, id: Option[Int] = None)

class Trucks(tag: Tag) extends Table[Truck](tag, "TRUCKS") with TypeConvert {
	def name: Column[String] = column[String]("name", O.NotNull)
	def startTime: Column[DateTime] = column[DateTime]("startTime")
	def endTime: Column[DateTime] = column[DateTime]("endTime")
	def maxWeight: Column[BigDecimal] = column[BigDecimal]("maxWeight")
	def id: Column[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

	def * = (name, startTime, endTime, maxWeight, id.?) <>(Truck.tupled, Truck.unapply)
}

class Depots(tag: Tag) extends Table[Depot](tag, "DEPOTS") with TypeConvert {
	def name: Column[String] = column[String]("name", O.NotNull)
	def location: Column[Point] = column[Point]("location", O.NotNull)
	def id: Column[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
	
	def * = (name, location, id.?) <>(Depot.tupled, Depot.unapply)
}

class Stops(tag: Tag) extends Table[Stop](tag, "STOPS") with TypeConvert {
	
	//(location: Point, startTime: DateTime, endTime: DateTime, maxWeight: Double, specialCodes: List[String], id: Option[Int] = None)	def location: Column[String] = column[String]("location", O.NotNull)
	def name: Column[String] = column[String]("name", O.NotNull)
	def location: Column[Point] = column[Point]("location")
	def startTime: Column[DateTime] = column[DateTime]("startTime")
	def endTime: Column[DateTime] = column[DateTime]("endTime")
	def maxWeight: Column[BigDecimal] = column[BigDecimal]("maxWeight")
	def specialCodes: Column[List[String]] = column[List[String]]("specialCodes")
	def id: Column[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

	// the * projection (e.g. select * ...) auto-transforms the tupled
	// column values to / from a User
	def * = (name, location, startTime, endTime, maxWeight, specialCodes, id.?) <>(Stop.tupled, Stop.unapply)
}


object Tables {
	val users: TableQuery[Users] = TableQuery[Users]
	val trucks: TableQuery[Trucks] = TableQuery[Trucks]
	val depots: TableQuery[Depots] = TableQuery[Depots]
	val stops: TableQuery[Stops] = TableQuery[Stops]
}

class DatabaseSupport(db: Database) {

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


}