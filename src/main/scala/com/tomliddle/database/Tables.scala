package com.tomliddle.database

import com.tomliddle.solution._
import java.sql.Time
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

object Tables {
	val users: TableQuery[Users] = TableQuery[Users]
	val trucks: TableQuery[Trucks] = TableQuery[Trucks]
	val depots: TableQuery[Depots] = TableQuery[Depots]
	val stops: TableQuery[Stops] = TableQuery[Stops]
}

