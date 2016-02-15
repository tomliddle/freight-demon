package com.tomliddle.solution.timeanddistance

import com.tomliddle.entity.Point
import org.joda.time.Duration

/**
	* Calculates hypotenuse lengths between points.
	*/
trait SimpleTimeAndDistCalc extends TimeAndDistCalc {

	override def getMetresDistance(location1: Point, location2: Point): BigDecimal = {
		val xdiff = location1.x.doubleValue() - location2.x.doubleValue()
		val ydiff = location1.y.doubleValue() - location2.y.doubleValue()
		math.hypot(xdiff, ydiff)
	}

	override def getDuration(location1: Point, location2: Point): Duration = {
		// 10=m/s = 36kmph
		new Duration ((getMetresDistance(location1, location2) * 100).toLong)
	}
}
