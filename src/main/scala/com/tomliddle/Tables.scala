package com.tomliddle

import java.util.UUID

import com.tomliddle.Solution.Depot
import com.tomliddle.Solution.Stop

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

case class Image(name: String, image: Array[Byte], user_id: Int, id: Option[Int] = None)

class Images(tag: Tag) extends Table[Image](tag, "IMAGES") {
	def name: Column[String] = column[String]("NAME")
	def image: Column[Array[Byte]] = column[Array[Byte]]("IMAGE")
	def userId: Column[Int] = column[Int]("USER_ID")
	def id: Column[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
	def * = (name, image, userId, id.?) <>(Image.tupled, Image.unapply)

	// A reified foreign key relation that can be navigated to create a join
	//def supplier: ForeignKeyQuery[Suppliers, (Int, String, String, String, String, String)] =
	foreignKey("USER_FK", userId, TableQuery[Users])(_.id)
}

// ************************ TRUCK STUFF ***************************************

class Trucks(tag: Tag) extends Table[User](tag, "TRUCKS") {
	//val name: String, val depot: Depot, val stops: List[Stop], val startTime: Int, val endTime: Int, val maxWeight: Double, id: Option[Int] = None
	def name: Column[String] = column[String]("name", O.NotNull)
	def depot: Column[String] = column[String]("depot", O.NotNull)
	def stops: Column[String] = column[String]("stops", O.NotNull)
	def startTime: Column[String] = column[String]("startTime", O.NotNull)
	def endTime: Column[String] = column[String]("password_hash", O.NotNull)
	def maxWeight: Column[String] = column[String]("password_hash", O.NotNull)
	def id: Column[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

	// the * projection (e.g. select * ...) auto-transforms the tupled
	// column values to / from a User
	def * = (name, depot, stops, name, passwordHash, id.?) <>(User.tupled, User.unapply)
}



object Tables {
	val users: TableQuery[Users] = TableQuery[Users]
	val images: TableQuery[Images] = TableQuery[Images]
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

	//************************ IMAGES ***********************************
	def getImage(id: Int, userId: Int): Option[Image] = {
		db.withDynSession {
			images.filter {user => user.id === id && user.userId === userId}.firstOption
		}
	}

	def getImageList(userId: Int): List[Image] = {
		db.withDynSession {
			images.filter {user => user.userId === userId}.list
		}
	}

	def getImages(userId: Int): List[Image] = {
		db.withDynSession {
			images.filter(_.userId === userId).list
		}
	}

	def addImage(image: Image) = {
		db.withDynSession {
			images += image
		}
	}

	def deleteImage(id: Int, userId: Int) = {
		db.withDynSession {
			images.filter {user => user.id === id && user.userId === userId}.delete
		}
	}


}