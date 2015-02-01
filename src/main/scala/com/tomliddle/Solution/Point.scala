package com.tomliddle.Solution

class Point(val name: String, val x: Double, val y: Double, val postcode: String) {
	var distancesAndTimes = Map[Stop, (Double, Int)]()
	private def sortedDistances = distancesAndTimes.toList.sortBy(_._2._1)
	def findFurthest = sortedDistances.last._1
	def findNearest = sortedDistances.head._1
	override def toString = name + " " + x + "," + y
}

case class Depot(location: Point, id: Option[Int] = None)

case class Stop(location: Point, val startTime: Int, val endTime: Int, val maxWeight: Double, val specialCodes: List[String], id: Option[Int] = None)



