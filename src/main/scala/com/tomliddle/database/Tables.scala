package com.tomliddle.database

import com.tomliddle.entity.{LocationMatrix, Stop, Depot}
import com.tomliddle.solution._
import java.sql.Time
import org.joda.time.LocalTime
import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted.TableQuery

	/**
	* Adds implicit conversions to slick tables
	*/
trait TypeConvert {

	implicit def localTime = MappedColumnType.base[LocalTime, Time](dt => new Time(dt.getMillisOfDay), ts => new LocalTime(ts.getTime))

	implicit def listToString = MappedColumnType.base[Seq[String], String](
		dt => dt.foldLeft(""){(a, b) => s"$a,$b"},
		ts => Seq("", "")
	)
}

case class User(email: String, name: String, passwordHash: String, id: Option[Int] = None)

/**
* Slick user table
*/
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

/**
* DBTruck is saved to the database. This excludes additional information stored in the
* truck such as depot and stops.
* This is not ideal which is why the move to MongoDB should solve this.
*/
case class DBTruck(name: String, startTime: LocalTime, endTime: LocalTime, maxWeight: BigDecimal, userId: Int, id: Option[Int] = None) {
	def toTruck(stops: Seq[Stop], depot: Depot, lm: LocationMatrix): Truck = {
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
	def userId: Column[Int] = column[Int]("userId")
	def id: Column[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

	def * = (name, x, y, address, startTime, endTime, maxWeight, userId, id.?) <>(Stop.tupled, Stop.unapply)
}

object Tables {
	val users: TableQuery[Users] = TableQuery[Users]
	val trucks: TableQuery[Trucks] = TableQuery[Trucks]
	val depots: TableQuery[Depots] = TableQuery[Depots]
	val stops: TableQuery[Stops] = TableQuery[Stops]
}

