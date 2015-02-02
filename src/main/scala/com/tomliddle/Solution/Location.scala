package com.tomliddle.solution

import org.joda.time.{Duration, DateTime}

class DistanceTime(val distance: BigDecimal, val time: Duration)

case class Location(val x: BigDecimal, val y: BigDecimal, val postcode: String)

case class Depot(name: String, location: Location, id: Option[Int] = None)

case class Stop(name: String, location: Location, startTime: DateTime, endTime: DateTime, maxWeight: BigDecimal, specialCodes: List[String], id: Option[Int] = None)

class LocationMatrix(stops: List[Stop], depots: List[Depot]) extends TimeAndDistCalc {

	private val stopDistancesAndTimes: Map[Stop, Map[Stop, DistanceTime]] = {
		stops.map {
			stop1: Stop => {
				// Map of [Stop, DistanceTime]
				stop1 -> stops.map {
					stop2: Stop =>
						stop2 -> getDistanceTime(stop1.location, stop2.location)
				}.toMap[Stop, DistanceTime]
			}
		}.toMap
	}

	private val depotDistancesAndTimes: Map[Depot, Map[Stop, DistanceTime]] = {
		depots.map {
			depot: Depot => {
				// Map of [Stop, DistanceTime]
				depot -> stops.map {
					stop: Stop =>
						stop -> getDistanceTime(depot.location, stop.location)
				}.toMap[Stop, DistanceTime]
			}
		}.toMap
	}


	def findFurthest(depot: Depot): Stop  = depotDistancesAndTimes(depot).toList.sortBy(_._2.distance).last._1

	def findNearest(stop: Stop): Stop = stopDistancesAndTimes(stop).toList.sortBy(_._2.distance).head._1

	def distanceBetween(depot: Depot, stop: Stop): BigDecimal = depotDistancesAndTimes(depot)(stop).distance

	def distanceBetween(stop1: Stop, stop2: Stop): BigDecimal = stopDistancesAndTimes(stop1)(stop2).distance

	def timeBetween(depot: Depot, stop: Stop): Duration = depotDistancesAndTimes(depot)(stop).time

	def timeBetween(stop1: Stop, stop2: Stop): Duration = stopDistancesAndTimes(stop1)(stop2).time
}



trait TimeAndDistCalc {

	def getDistance(stop1: Location, stop2: Location): BigDecimal = {
		// Math.sqrt(Math.pow(city.y - city2.y, 2) + Math.pow(city.x - city2.x, 2))
		val R = 6371 // km
		var lat1 = stop1.y.toDouble
		var lat2 = stop2.y.toDouble
		val lon1 = stop1.x.toDouble
		val lon2 = stop2.x.toDouble
		val dLat = scala.math.toRadians(lat2 - lat1)
		val dLon = scala.math.toRadians(lon2 - lon1)
		lat1 = scala.math.toRadians(lat1)
		lat2 = scala.math.toRadians(lat2)

		var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
			Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2)
		var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
		R * c
	}

	def getDuration(stop1: Location, stop2: Location): Duration = {
		new Duration(0)
	}

	def getDistanceTime(stop1: Location, stop2: Location): DistanceTime = {
		new DistanceTime(getDistance(stop1, stop2), getDuration(stop1, stop2))
	}

	//def getDistance(s) = Math.sqrt(Math.pow(depot.location.y - depot2.location.y, 2) + Math.pow(depot.location.x - depot2.location.x, 2))
}