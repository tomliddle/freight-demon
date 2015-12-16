package com.tomliddle.solution


import com.tomliddle.entity.{LocationMatrix, Stop, Depot, Link}
import com.tomliddle.util.Logging
import org.joda.time.{Duration, LocalTime}
import scala.math.BigDecimal.RoundingMode

case class Truck(
										name: String,
										startTime: LocalTime,
										endTime: LocalTime,
										maxWeight: BigDecimal,
										depot: Depot,
										stops: List[Stop],
										lm: LocationMatrix,
										userId: Int,
										id: Option[Int] = None)
	extends TruckAlgorithm with TruckLinks with Logging  {



	lazy val totalWeight: BigDecimal = {
		stops.foldLeft(BigDecimal(0)) { (totalWeight, link) => totalWeight + link.maxWeight }
				.setScale(2, RoundingMode.HALF_UP)
	}

	lazy val cost: Option[BigDecimal] = {
		distance match {
			case Some(distance) => Some((distance * 1.2).setScale(2, RoundingMode.HALF_UP))
			case None => None
		}
	}

	lazy val getMaxSwapSize = stops.size / 2

	lazy val links: Option[List[Link]] = getLinks(lm)

	lazy val distance: Option[BigDecimal] = links match {
			case Some(links: List[Link]) => Some(links.foldLeft(BigDecimal(0)) { (a: BigDecimal, b: Link) => a + b.travelDT.distance })
			case None => None
	}

	lazy val time: Option[Duration] = links match {
		case Some(links: List[Link]) => Some(links.foldLeft(new Duration(0)) { (a: Duration, b: Link) => a.plus(b.travelDT.time).plus(b.waitTime) })
		case None => None
	}


	lazy val isValid: Boolean = weightValid && timeValid

	lazy val weightValid: Boolean = totalWeight <= maxWeight

	lazy val timeValid: Boolean = time.isDefined //&& time.get.isShorterThan(new Duration(5 * 1000 * 60 * 60))
}


