package com.tomliddle.solution.timeanddistance

import com.tomliddle.entity.{Point, DistanceTime}
import org.joda.time.Duration

/**
	* Adds time and distance calculation functionality.
	* In the future this will be replaced by real world road time and distance calculations
	*/
trait TimeAndDistCalc {

	def getMetresDistance(location1: Point, location2: Point): BigDecimal

	def getDuration(location1: Point, location2: Point): Duration

	def getDistanceTime(stop1: Point, stop2: Point): DistanceTime = {
		new DistanceTime(getMetresDistance(stop1, stop2), getDuration(stop1, stop2))
	}

}
