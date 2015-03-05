package com.tomliddle.form

import com.tomliddle.DBTruck
import com.tomliddle.solution.Stop
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat


case class TruckForm(name: String, startTime: String, endTime: String, maxWeight: String) {
	private val formatter = DateTimeFormat.forPattern("HH:mm")
	def getTruck(userId: Int) = DBTruck(name, LocalTime.parse(startTime, formatter), LocalTime.parse(endTime, formatter), BigDecimal(maxWeight), userId)
}

case class StopForm(name: String, startTime: String, endTime: String, maxWeight: String, address: String) {
	private val formatter = DateTimeFormat.forPattern("HH:mm")
	def getStop(userId: Int, x: BigDecimal, y: BigDecimal) = {
		Stop(name, x, y, address, LocalTime.parse(startTime, formatter), LocalTime.parse(endTime, formatter), BigDecimal(maxWeight), List() , userId)
	}
}