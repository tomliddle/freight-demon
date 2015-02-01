package com.tomliddle.Solution

import org.joda.time.DateTime


class Data(stopsFile: String, depotsFile: String, trucksFile: String) {

	private lazy val stopsLocations: List[Stop] = stopLocations(stopsFile)
	private lazy val depotsLocations: List[Depot] = depotLocations(depotsFile)

	private def stopLocations(fileName: String): List[Stop] = {
		scala.io.Source.fromFile(fileName).getLines.drop(1).map(string => {
			val split = string.split("\t")
			var specialCodes = List[String]()
			if (split.size > 7) specialCodes = split(7).split(",").toList

			val geocodes: (String, String, String) = Geocoding.getCoordinates(split(3))
			if (geocodes == null) {}

			val point = new Point(split(0), geocodes._2.toDouble, geocodes._3.toDouble, split(3))
			new Stop(point, new DateTime(split(4).toInt), new DateTime(split(5).toInt), split(6).toDouble, specialCodes)
		})
	}.toList

	private def depotLocations(fileName: String): List[Depot] = {
		scala.io.Source.fromFile(fileName).getLines.drop(1).map(string => {
			val split = string.split("\t")
			new Depot(new Point(split(0), split(1).toDouble, split(2).toDouble, split(3)), Some(1))
		})
	}.toList


	def trucks: List[Truck] = {
		var trucks = List[Truck]()
		scala.io.Source.fromFile(trucksFile).getLines.drop(1).foreach(string => {
			val split = string.split("\t")

			depots.find(depot => depot.location.name == split(1)) match {
				case Some(depot) => trucks ::= new Truck(split(0), depot, Nil, new DateTime(split(3).toInt), new DateTime(split(4).toInt), split(2).toDouble)
				case None => println("Cannot find depot " + split(1) + "for truck")
			}
		})
		trucks
	}

	lazy val stops = stopsLocations.map(stop => addStopDistances(stop)).toList
	lazy val depots: List[Depot] = depotsLocations.map(depot => addDepotDistances(depot)).toList

	private def addStopDistances(stop: Stop): Stop = {
		stop.location.distancesAndTimes = stopsLocations.map {
			city2 => {
				val distance = getDistance(stop, city2)
				//val time = timesAndDistances(city.postcode)(city2.postcode)
				(city2, (distance, distance.toInt / 20))
			}
		}.toMap
		stop
	}

	private def getDistance(stop1: Stop, stop2: Stop): Double = {
		// Math.sqrt(Math.pow(city.y - city2.y, 2) + Math.pow(city.x - city2.x, 2))
		var R = 6371 // km
		var lat1 = stop1.location.y
		var lat2 = stop2.location.y
		var lon1 = stop1.location.x
		var lon2 = stop2.location.x
		var dLat = scala.math.toRadians(lat2 - lat1)
		var dLon = scala.math.toRadians(lon2 - lon1)
		lat1 = scala.math.toRadians(lat1)
		lat2 = scala.math.toRadians(lat2)

		var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
			Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2)
		var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
		R * c
	}

	private def addDepotDistances(depot: Depot): Depot = {
		depot.location.distancesAndTimes = stopsLocations.map {
			depot2 => {
				val distance = Math.sqrt(Math.pow(depot.location.y - depot2.location.y, 2) + Math.pow(depot.location.x - depot2.location.x, 2))
				//val time = timesAndDistances[city.postcode][city2.postcode]
				(depot2, (distance, distance.toInt / 20))
			}
		}.toMap
		depot
	}
}
