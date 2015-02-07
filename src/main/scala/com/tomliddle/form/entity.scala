package com.tomliddle.form

import com.tomliddle.solution.{Stop, Truck}
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat


case class TruckForm(name: String, startTime: String, endTime: String, maxWeight: String) {
	private val formatter = DateTimeFormat.forPattern("HH:mm")
	def getTruck(userId: Int) = Truck(name, LocalTime.parse(startTime, formatter), LocalTime.parse(endTime), BigDecimal(maxWeight), userId)
}

case class StopForm(name: String, startTime: String, endTime: String, maxWeight: String, postcode: String) {
	private val formatter = DateTimeFormat.forPattern("HH:mm")
	def getStop(userId: Int, locationId: Int) = {
		Stop(name, locationId, LocalTime.parse(startTime, formatter), LocalTime.parse(endTime), BigDecimal(maxWeight), List() , userId)
	}
}