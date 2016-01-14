package com.tomliddle.entity

import com.tomliddle.solution.timeanddistance.StraightLineTimeAndDistCalc
import org.joda.time.{Duration, LocalTime}

/**
	* Represents a distance and time between two points
	* @param distance
	* @param time
	*/
class DistanceTime(val distance: BigDecimal = BigDecimal(0), val time: Duration = new Duration(0)) {
	def +(operand: DistanceTime): DistanceTime = {
		new DistanceTime(this.distance + operand.distance, this.time.plus(operand.time))
	}
}

/**
	* Represents a link in a solution which includes distance/time and any wait time
	* @param waitTime
	* @param travelDT
	*/
case class Link(waitTime: Duration = new Duration(0), travelDT: DistanceTime = new DistanceTime()) {
	def +(operand: Link): Link = {
		new Link(this.waitTime.plus(operand.waitTime), this.travelDT + operand.travelDT)
	}

	def elapsedTime: Duration = travelDT.time.plus(waitTime)
}

/**
	* Represents a named point.
	* @param x
	* @param y
	* @param address
	*/
class Point(val x: BigDecimal, val y: BigDecimal, val address: String) {
	def +(operand: Point): Point = {
		new Point(x + operand.x, y + operand.y, "")
	}
	def /(operand: BigDecimal): Point = {
		new Point(x / operand, y / operand, address)
	}
}

/**
	* A truck start and end point.
	*/
case class Depot(name: String, override val x: BigDecimal,  override val y: BigDecimal, override val address: String, userId: Int, id: Option[Int] = None) extends Point(x, y, address)

/**
	* A delivery stop. There can be any number at the same address.
 	*/
case class Stop(name: String, override val x: BigDecimal, override val y: BigDecimal, override val address: String, startTime: LocalTime, endTime: LocalTime, maxWeight: BigDecimal, userId: Int, id: Option[Int] = None) extends Point(x, y, address)

/**
	* A location matrix within a solution from every stop to every depot.
	* The design of this is not currently ideal as each truck has a reference to this allowing it to calculate
	* its own routes. Logically it makes sense for the truck to be able to do this but it should do it with reference
	* to the solutions location matrix (in practice they are the same immutable object anyhow).
	* @param stops
	* @param depots
	*/
case class LocationMatrix(stops: List[Stop], depots: List[Depot]) extends StraightLineTimeAndDistCalc {

	private val distancesAndTimes: Map[Point, Map[Point, DistanceTime]] = {
		(stops ::: depots).map {
			stop1: Point => {
				stop1 -> stops.map {
					stop2: Point =>
						stop2 -> getDistanceTime(stop1, stop2)
				}.toMap[Point, DistanceTime]
			}
		}.toMap
	}

	def findFurthestStop(point: Point): Stop  = {
		distancesAndTimes(point).collect{case (s: Stop, dt: DistanceTime) => (s, dt)}.toList.sortBy(_._2.distance).last._1
	}

	def distanceTimeBetween(stop1: Point, stop2: Point): DistanceTime = distancesAndTimes(stop1)(stop2)

}

