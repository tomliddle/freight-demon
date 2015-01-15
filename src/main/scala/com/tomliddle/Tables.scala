package com.tomliddle

import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted.{TableQuery}


case class User(email: String, name: String, id: Option[Int] = None)

class Users(tag: Tag) extends Table[User](tag, "USERS") {
	def email: Column[String] = column[String]("email")
	def name: Column[String] = column[String]("name")
	def id: Column[Int] = column[Int]("id", O.PrimaryKey,  O.AutoInc)

	// the * projection (e.g. select * ...) auto-transforms the tupled
	// column values to / from a User
	def * = (email, name, id.?) <> (User.tupled, User.unapply)
}

case class Image(name: String, image: Array[Byte], userId: Option[Int] = None)

class Images(tag: Tag) extends Table[Image](tag, "IMAGES") {
	def name: Column[String] = column[String]("NAME", O.PrimaryKey, O.NotNull)
	def image: Column[Array[Byte]] = column[Array[Byte]]("IMAGE")
	def userId: Column[Int] = column[Int]("USER_ID")

	def * = (name, image, userId.?) <> (Image.tupled, Image.unapply)

	// A reified foreign key relation that can be navigated to create a join
	//def supplier: ForeignKeyQuery[Suppliers, (Int, String, String, String, String, String)] =
	foreignKey("USER_FK", userId, TableQuery[Users])(_.id)
}

object Tables {
	val users: TableQuery[Users] = TableQuery[Users]
	val images: TableQuery[Images] = TableQuery[Images]


}