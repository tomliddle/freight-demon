package com.tomliddle.solution
import org.joda.time.{Duration, LocalTime}



class RouteInvalidException(message: String) extends Exception

trait TruckLinks {
	this: Truck =>

	@throws[RouteInvalidException]
	def getLinks(): List[Link] = {

		var routeEarliestStartTime: LocalTime = startTime
		var routeLatestStartTime: LocalTime = endTime
		var routeJourneyTime = new Duration(0)

		//private bool addLinkTimeCostDistance(Stop previous, Stop current, ref TimeSpan tsRouteEarliestStart, ref TimeSpan tsRouteLatestStart, ref long lCurrRouteDistance, ref double dCurrRouteCost, ref TimeSpan tsRouteJourneyTime, bool bSetStopVars, int iOrder) {
		def getNextLink(stop1: Stop, stop2: Stop): Link = {

			logger.debug("GET LINK ---------------------")

			val linkJourneyDistTime = lm.distanceTimeBetween(stop1, stop2)
			routeJourneyTime = routeJourneyTime.plus(linkJourneyDistTime.time)
			val linkEarliestStartTime: LocalTime = stop2.startTime.minus(routeJourneyTime.toPeriod)
			val linkLatestStartTime = stop2.endTime.minus(routeJourneyTime.toPeriod)
			var linkWaitTime = new Duration(0)

			logger.debug("Link journey time: {}, route journey time: {}  link E Start: {}, link L Start {}", linkJourneyDistTime.time, routeJourneyTime, linkEarliestStartTime, linkLatestStartTime)

			// If you can't get to the stop before a certain time, the truck has to start later
			if (linkEarliestStartTime.isAfter(routeEarliestStartTime)) {
				logger.debug(s"Link E Start ${linkEarliestStartTime} After Route E start ${routeEarliestStartTime} setting route E start")
				routeEarliestStartTime = linkEarliestStartTime
			}

			// As the route gets longer, the latest start time gets earlier
			if (linkLatestStartTime.isBefore(routeLatestStartTime)) {
				logger.debug(s"Link L Start ${linkLatestStartTime} Before Route L start ${routeLatestStartTime} setting route L start")
				routeLatestStartTime = linkLatestStartTime
			}

			// If this happens, we have to wait. If the earliest link start is after latest route start - difference is wait time
			if (linkEarliestStartTime.isAfter(routeLatestStartTime)) {
				logger.debug(s"Link L Start ${linkLatestStartTime} Before Route L start ${routeLatestStartTime} setting route L start")
				// TODO this is wrong
				val waitTime = new Duration(math.abs(routeLatestStartTime.getMillisOfDay - linkEarliestStartTime.getMillisOfDay))
				linkWaitTime = linkWaitTime.plus(waitTime)
				routeJourneyTime = routeJourneyTime.plus(waitTime)
			}


			if (routeEarliestStartTime.isAfter(routeLatestStartTime)) {
				logger.debug(s"Route invalid Earliest start: ${routeEarliestStartTime} After routelateststart ${routeLatestStartTime}")
				throw new RouteInvalidException("Not possible in the time")
			}

			Link(linkWaitTime, linkJourneyDistTime)
		}

		val depotLinks: (Link, Link) =
			if (stops.size > 0) {
				routeJourneyTime = routeJourneyTime.plus(lm.distanceTimeBetween(depot, stops(0)).time)

				// TODO add wait time here

				(Link(new Duration(0), lm.distanceTimeBetween(depot, stops(0))),
				Link(new Duration(0), lm.distanceTimeBetween(depot, stops.last)))
			}
			else (Link(), Link())



		val stopLinks: List[Link] = if (stops.size > 1)
			stops.sliding(2).map {
				(currCities: List[Stop]) =>
					getNextLink(currCities(0), currCities(1))
			}.toList
		else List()

		List(depotLinks._1) ++ stopLinks ++ List(depotLinks._2)
	}
}
