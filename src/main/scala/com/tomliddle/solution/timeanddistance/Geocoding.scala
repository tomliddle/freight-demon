package com.tomliddle.solution.timeanddistance

import java.net.URLEncoder

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.slf4j.LoggerFactory


trait Geocoding {

	private val logger = LoggerFactory.getLogger(getClass().getName());

	def geocodeFromOnline(address: String): Option[JValue] = {

		//val url: String = "http://www.freethepostcode.org/geocode?postcode=" + postcode

		val key = "AIzaSyASpRNBPXNWgvNighYUrkw6rswysrABjbU"
		val urlEncodedAddress = URLEncoder.encode(address, "UTF-8")
		val url = s"https://maps.googleapis.com/maps/api/geocode/json?address=$urlEncodedAddress&key=$key"

		try {
			val str = scala.io.Source.fromURL(url)
			val data = parse(str.mkString)
			Some(data)

		/*	implicit lazy val formats = org.json4s.DefaultFormats
			val lat = (json \ "results" \ "geometry" \ "location" \ "lat").extractOpt[BigDecimal]
			val lng = (json \ "results" \ "geometry" \ "location" \ "lng").extractOpt[BigDecimal]

			Some((lng.get, lat.get))*/
		}
		catch {
			case e: Exception =>
				logger.error("Cannot geocode" + e.getMessage, e)
				None
		}
	}

}

