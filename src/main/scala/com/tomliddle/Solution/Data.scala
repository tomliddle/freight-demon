package com.tomliddle.solution

import org.joda.time.DateTime

class Data(stopsFile: String, depotsFile: String, trucksFile: String) {

	val stops: List[Stop] = stopLocations(stopsFile)
	val depots: List[Depot] = depotLocations(depotsFile)

	private def stopLocations(fileName: String): List[Stop] = {
		scala.io.Source.fromFile(fileName).getLines.drop(1).map(string => {
			val split = string.split("\t")
			var specialCodes = List[String]()
			if (split.size > 7) specialCodes = split(7).split(",").toList

			val geocodes: (String, String, String) = Geocoding.getCoordinates(split(3))
			if (geocodes == null) {}

			val point = new Point(split(0), BigDecimal(geocodes._2), BigDecimal(geocodes._3), split(3))
			new Stop("Stop", point, new DateTime(split(4)), new DateTime(split(5)), BigDecimal(split(6)), specialCodes)
		})
	}.toList

	private def depotLocations(fileName: String): List[Depot] = {
		scala.io.Source.fromFile(fileName).getLines.drop(1).map(string => {
			val split = string.split("\t")
			new Depot("Depot", new Point(split(0), BigDecimal(split(1)), BigDecimal(split(2)), split(3)), Some(1))
		})
	}.toList


	def trucks: List[Truck] = {
		var trucks = List[Truck]()
		scala.io.Source.fromFile(trucksFile).getLines.drop(1).foreach(string => {
			val split = string.split("\t")

			depots.find(depot => depot.location.name == split(1)) match {
				case Some(depot) => trucks ::= new Truck(split(0), new DateTime(split(3).toInt), new DateTime(split(4).toInt), split(2).toDouble)
				case None => println("Cannot find depot " + split(1) + "for truck")
			}
		})
		trucks
	}
}
