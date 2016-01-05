package com.tomliddle.form

import com.tomliddle.database.DBTruck
import com.tomliddle.entity.Stop
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat

/**
	* TODO use the proper serialisers.
	* These classes are supposed to be a temporary measure to avoid issues with scalatra serialisation
	*/
case class TruckForm(name: String, startTime: String, endTime: String, maxWeight: String) {
	private val formatter = DateTimeFormat.forPattern("HH:mm")
	def getTruck(userId: Int) = DBTruck(name, LocalTime.parse(startTime, formatter), LocalTime.parse(endTime, formatter), BigDecimal(maxWeight), userId)
}

case class StopForm(name: String, startTime: String, endTime: String, maxWeight: String, x: String, y: String, address: String) {
	private val formatter = DateTimeFormat.forPattern("HH:mm")
	def getStop(userId: Int): Stop = {
		Stop(name, BigDecimal(x), BigDecimal(y), address, LocalTime.parse(startTime, formatter), LocalTime.parse(endTime, formatter), BigDecimal(maxWeight), userId)
	}
}


case class SolutionForm(name: String)