package com.tomliddle

import java.sql.Timestamp

import Solution._
import org.joda.time.DateTime
import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted.TableQuery
import Tables._

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

class Trucks(tag: Tag) extends Table[Truck](tag, "TRUCKS") {

	implicit def dateTime = MappedColumnType.base[DateTime, Timestamp](dt => new Timestamp(dt.getMillis), ts => new DateTime(ts.getTime))

	def name: Column[String] = column[String]("name", O.NotNull)
	def startTime: Column[DateTime] = column[DateTime]("startTime")
	def endTime: Column[DateTime] = column[DateTime]("endTime")
	def maxWeight: Column[BigDecimal] = column[BigDecimal]("maxWeight")
	def id: Column[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

	// the * projection (e.g. select * ...) auto-transforms the tupled
	// column values to / from a User
	def * = (name, startTime, endTime, maxWeight, id.?) <>(Truck.tupled, Truck.unapply)
}

case class DBDepot(name: String, startTime: DateTime, endTime: DateTime, maxWeight: BigDecimal, id: Option[Int] = None)

class Depots(tag: Tag) extends Table[Depot](tag, "DEPOTS") {

	implicit def pointConvert = MappedColumnType.base[Point, String] (
		dt => s"${dt.name},${dt.x},${dt.y},${dt.postcode}",
		ts => {
			val str = ts.split(",")
			new Point(str(0), BigDecimal(str(1)), BigDecimal(str(2)), str(3))
		}
	)

	def location: Column[String] = column[String]("location", O.NotNull)
	def id: Column[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

	// the * projection (e.g. select * ...) auto-transforms the tupled
	// column values to / from a User
	def * = (location, id.?) <>(Depot.tupled, Depot.unapply)
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

	def getTruckList(userId: Int): List[Truck] = {
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