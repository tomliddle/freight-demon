package com.tomliddle.Solution

import java.io.{FileWriter, InputStream}

import scala.collection.mutable
import scala.io.Source

object Geocoding {

	private val fileName = "GB.txt"
	private val geoMap: mutable.Map[String, (String, String)] = new mutable.HashMap[String, (String, String)]()

	for (line <- Source.fromFile(fileName).getLines) {
		val tokens: Array[String] = line.split("\t")
		geoMap += tokens(0) ->(tokens(1), tokens(2))
	}


	def getCoordinates(postcode: String): (String, String, String) = {
		var obj: (String, String, String) = geocodeFromCache(postcode)
		if (obj == null) {
			obj = geocodeFromFile(postcode)
			if (obj == null) {
				obj = geocodeFromOnline(postcode)
			}
			if (obj != null) {
				writeGeocodeToFile(obj)
			}
		}
		(obj._1, obj._2, obj._2)
	}

	private def geocodeFromFile(postcode: String): (String, String, String) = {
		var line: Option[(String, String)] = geoMap.get(postcode)
		if (line != null && line != None) {
			var tup: (String, String) = line.get
			return (postcode, tup._1, tup._2)
		}
		null
	}

	// Name, lat long
	private def writeGeocodeToFile(obj: (String, String, String)) {
		val out = new FileWriter("geocoding.obj", true)
		out.write(obj._1 + "\t" + obj._2 + "\t" + obj._3 + "\n")
		out.close()
	}

	private def geocodeFromCache(name: String): (String, String, String) = {
		val lines = io.Source.fromFile("geocoding.obj").getLines
		while (lines.hasNext) {
			val sp: Array[String] = lines.next.split("\t")
			if (name != null && sp != null && sp.length == 3) {
				if (name.compareToIgnoreCase(sp(0)) == 0) {
					return (sp(0), sp(1), sp(2))
				}
			}
		}
		null
	}

	private def geocodeFromOnline(postcode: String): (String, String, String) = {

		val url: String = "http://www.freethepostcode.org/geocode?postcode=" + postcode
		var line: String = null
		var reader: java.io.BufferedReader = null
		var inputStream: InputStream = null
		try {
			var netUrl = new java.net.URL(url);
			inputStream = netUrl.openStream();
			reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream))
			reader.readLine() // discard header
			line = reader.readLine()
		}
		catch {
			case e: Exception => {
				println("Exception geocoding online" + e.getMessage)
				return null
			}
		}
		finally {
			if (reader != null) {
				reader.close()
			}
			if (inputStream != null) {
				inputStream.close()
			}
		}

		val sp: Array[String] = line.split(" ")
		if (sp.length > 3) {
			return (postcode, sp(0), sp(1))
		}
		null
	}

}

