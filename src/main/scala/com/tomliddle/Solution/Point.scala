package com.tomliddle.Solution

class Point(val name: String, val x: Double, val y: Double, val postcode: String) {
	var distancesAndTimes = Map[Stop, (Double, Int)]()
	private lazy val sortedDistances = distancesAndTimes.toList.sortBy(_._2._1)
	lazy val findFurthest = sortedDistances.last._1
	lazy val findNearest = sortedDistances.head._1
	override lazy val toString = name + " " + x + "," + y
}

class Stop(override val name: String,
		   override val x: Double,
		   override val y: Double,
		   override val postcode: String,
		   val constraints: StopConstraints) extends Point(name, x, y, postcode) {
}

class StopConstraints(val startTime: Int, val endTime: Int, val maxWeight: Double, val specialCodes: List[String])

class TruckConstraints(val startTime: Int, val endTime: Int, val maxWeight: Double)
