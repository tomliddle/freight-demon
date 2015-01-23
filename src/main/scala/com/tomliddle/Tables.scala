package com.tomliddle

import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted.TableQuery


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

object Tables {
	val users: TableQuery[Users] = TableQuery[Users]
	val images: TableQuery[Images] = TableQuery[Images]

	def getUser(id: Int): Option[User] = {
		users.filter(_.id === id).firstOption
	}
	def getUser(email: String): Option[User] = {
		users.filter(_.email === email).firstOption
	}

}