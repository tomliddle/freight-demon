package com.tomliddle.solution.timeanddistance

import com.tomliddle.entity.Point
import org.joda.time.Duration


trait LatLongTimeAndDistCalc extends TimeAndDistCalc {

	override def getMetresDistance(location1: Point, location2: Point): BigDecimal = {
		val R = 6371000 // m
		var lat1 = location1.y.toDouble
		var lat2 = location2.y.toDouble
		val lon1 = location1.x.toDouble
		val lon2 = location2.x.toDouble
		val dLat = scala.math.toRadians(lat2 - lat1)
		val dLon = scala.math.toRadians(lon2 - lon1)
		lat1 = scala.math.toRadians(lat1)
		lat2 = scala.math.toRadians(lat2)

		var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
			Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2)
		var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
		BigDecimal(R * c)
	}

	override def getDuration(stop1: Point, stop2: Point): Duration = {
		// 50 is 20 m/s which is 72kmph
		new Duration((getMetresDistance(stop1, stop2) * 50).toLong)
	}

}
