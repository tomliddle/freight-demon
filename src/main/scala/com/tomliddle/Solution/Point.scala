package com.tomliddle.Solution

import org.joda.time.{Duration, DateTime}

class Point(val name: String, val x: Double, val y: Double, val postcode: String) {
	// Maps each stop to a distance
	var distancesAndTimes = Map[Stop, (Double, Duration)]()
	private def sortedDistances = distancesAndTimes.toList.sortBy(_._2._1)
	def findFurthest = sortedDistances.last._1
	def findNearest = sortedDistances.head._1
	override def toString = name + " " + x + "," + y
}

case class Depot(location: Point, id: Option[Int] = None)

case class Stop(location: Point, val startTime: DateTime, val endTime: DateTime, val maxWeight: Double, val specialCodes: List[String], id: Option[Int] = None)



