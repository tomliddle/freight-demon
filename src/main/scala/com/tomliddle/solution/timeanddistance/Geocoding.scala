package com.tomliddle.solution.timeanddistance

import java.net.URLEncoder

import com.tomliddle.util.Logging
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.util.{Failure, Try}


trait Geocoding extends Logging {

	private val key = "AIzaSyASpRNBPXNWgvNighYUrkw6rswysrABjbU"

	def geocodeFromOnline(address: String): Try[JValue] = {
		val urlEncodedAddress = URLEncoder.encode(address, "UTF-8")
		val url = s"https://maps.googleapis.com/maps/api/geocode/json?address=$urlEncodedAddress&key=$key"

		Try{parse(scala.io.Source.fromURL(url).mkString)}.recoverWith {
			case e: Exception =>
				logg.error(s"Cannot geocode $e.getMessage")
				Failure(e)
		}
	}

}

