package com.tomliddle.solution
import org.joda.time.{Duration, LocalTime}



class RouteInvalidException(message: String) extends Exception

trait TruckLinks {
	this: Truck =>


	def getLinks: Option[List[Link]] = {

		var routeEarliestStartTime: LocalTime = startTime
		var routeLatestStartTime: LocalTime = endTime
		var routeJourneyTime = new Duration(0)

		@throws[RouteInvalidException]
		def getNextLink(stop1: Stop, stop2: Stop): Link = {

			//logger.trace("GET LINK ---------------------")

			val linkJourneyDistTime = lm.distanceTimeBetween(stop1, stop2)
			val linkEarliestStartTime: LocalTime = stop2.startTime.minus(routeJourneyTime.toPeriod)
			val linkLatestStartTime = stop2.endTime.minus(routeJourneyTime.toPeriod)
			var linkWaitTime = new Duration(0)
			routeJourneyTime = routeJourneyTime.plus(linkJourneyDistTime.time)

			//logger.debug(s"RouteEarliestStartTime: {}, RouteLatestStartTime: {}", routeEarliestStartTime, routeLatestStartTime)
			//logger.debug(s"1Link journey time: ${linkJourneyDistTime.time.toStandardMinutes}, route journey time: ${routeJourneyTime.toStandardMinutes}")
			//logger.debug(s"Stop E ${stop2.startTime} Stop L ${stop2.endTime} link E Start: ${linkEarliestStartTime}, link L Start ${linkLatestStartTime}")

			// If this happens, we have to wait. If the earliest link start is after latest route start - difference is wait time
			//	  ||							truck e/l start
			//		| |__________________| |    link e/l starts
			//	   ||Wait Time					(new journeytime)
			//	   | routeVars (e & l) become the same
			if (linkEarliestStartTime.isAfter(routeLatestStartTime)) {
				val waitTime = new Duration(math.abs(routeLatestStartTime.getMillisOfDay - linkEarliestStartTime.getMillisOfDay))
				linkWaitTime = linkWaitTime.plus(waitTime)
				routeJourneyTime = routeJourneyTime.plus(waitTime)
				//logger.debug(s"4Link L Start ${linkEarliestStartTime} Is after Route L start ${routeLatestStartTime} adding waitTime ${linkWaitTime.toStandardMinutes}")
				routeEarliestStartTime = routeLatestStartTime
			}
			else {
				// As the route gets longer, the latest start time gets earlier
				//	  |  |							truck E/L start
				//		| |__________________| |    link e/l start
				//		||							make truck E start later
				if (linkLatestStartTime.isBefore(routeLatestStartTime)) {
					//logger.debug(s"3Link L Start ${linkLatestStartTime} Before Route L start ${routeLatestStartTime} setting route L start to ${linkLatestStartTime}")
					routeLatestStartTime = linkLatestStartTime
				}
				// If you can't get to the stop before a certain time, the truck has to start later
				//	  |  |							truck E/L start
				//		| |__________________| |    link e/l start
				//		||							make truck E start later
				if (linkEarliestStartTime.isAfter(routeEarliestStartTime)) {
					//logger.debug(s"2Link E Start ${linkEarliestStartTime} After Route E start ${routeEarliestStartTime} setting route E start to ${linkEarliestStartTime}")
					routeEarliestStartTime = linkEarliestStartTime
				}
			}

			// Even if you leave at the earliest route time, yuou can't make the latest link time - invalid
			//			||						truck e/l start
			//		| |__________________| |    link e/l starts
			if (routeEarliestStartTime.isAfter(routeLatestStartTime)) {
				//logger.debug(s"Route invalid Earliest start: ${routeEarliestStartTime} After routelateststart ${routeLatestStartTime}")
				throw new RouteInvalidException("Not possible in the time")
			}

			Link(linkWaitTime, linkJourneyDistTime)
		}

		val depotLinks: (Link, Link) =
			if (stops.size > 0) {
				routeJourneyTime = routeJourneyTime.plus(lm.distanceTimeBetween(depot, stops(0)).time)

				//logger.debug("Route journey time: {}", routeJourneyTime.toStandardMinutes)

				// TODO add wait time here

				(Link(new Duration(0), lm.distanceTimeBetween(depot, stops(0))),
				Link(new Duration(0), lm.distanceTimeBetween(depot, stops.last)))
			}
			else (Link(), Link())


		def getStopLinks: List[Link] = {
			if (stops.size > 1)
				stops.sliding(2).map {
					(currCities: List[Stop]) =>
						getNextLink(currCities(0), currCities(1))
				}.toList
			else List()
		}

		try {
			Some(List(depotLinks._1) ++ getStopLinks ++ List(depotLinks._2))
		}
		catch {
			case rie: RouteInvalidException =>
				None
		}


	}
}



/*

//Helper method, calculates the opportunity cost and actual cost for the current stop and adds this on to total cost
private bool addLinkTimeCostDistance(Stop previous, Stop current, ref TimeSpan tsRouteEarliestStart, ref TimeSpan tsRouteLatestStart, ref long lCurrRouteDistance, ref double dCurrRouteCost, ref TimeSpan tsRouteJourneyTime, bool bSetStopVars, int iOrder) {

//Time (added time, plus wait time)
TimeSpan tsAddedTime = new TimeSpan ();
tsAddedTime = previous.liTimes[current.ID];
tsRouteJourneyTime += tsAddedTime;
tsRouteJourneyTime += current.tsWaitTime; //-not sure about

//Cost (cost per dist, and time, and wait time)
double dAddedCost = getCostPerMetre (previous.liDistances[current.ID]);
dAddedCost += getCostPerTimeSpan(tsAddedTime);

//Current Earliest start = earliest start to make this stop, same with latest
TimeSpan currentEarliestStart = new TimeSpan (0,0,0);
TimeSpan currentLatestStart = new TimeSpan(0, 0, 0);
currentEarliestStart = current.tsEarlyTime - tsRouteJourneyTime;
currentLatestStart = current.tsLateTime - tsRouteJourneyTime;


//	  |  |							routeVars
//		| |__________________| |    current e/l starts
//		||							new routevars
//if the earliest time at the stop - journey time means the truck has to start later
//set the route earliest time to the later time.
if (currentEarliestStart > tsRouteEarliestStart) {
	tsRouteEarliestStart = currentEarliestStart;
}

//		|	|						routeVars
//		| |__________________| |    current e/l starts
//		| |							new routevars
if (currentLatestStart < tsRouteLatestStart) {
	tsRouteLatestStart = currentLatestStart;
}

//	  ||							routeVars
//		| |__________________| |    current e/l starts
//	   ||Wait Time					(new journeytime)
//	   | routeVars (e & l)
//If you need to wait before you get there...
if (currentEarliestStart > tsRouteLatestStart) {

	//Time (add on the extra wait)
	tsRouteJourneyTime += currentEarliestStart - tsRouteLatestStart;

	//Cost (add on the extra cost of waiting)
	dAddedCost += getCostPerTimeSpan(currentEarliestStart - tsRouteLatestStart);

	//Set the earliest start to a bit later
	tsRouteEarliestStart = tsRouteLatestStart;
}

printCurrentTimeWindows((Stop)current, currentEarliestStart, currentLatestStart);//only prints if on


//			||						routeVars
//		| |__________________| |    current e/l starts
//Even getting there at the latest time, cannot leave early enough
if (tsRouteEarliestStart > currentLatestStart) {
	bReturn = false;
}





}*/
