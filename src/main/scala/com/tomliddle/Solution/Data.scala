package com.tomliddle.Solution


class Data(stopsFile: String, depotsFile: String, trucksFile: String) {

	private lazy val stopsLocations: List[Stop] = stopLocations(stopsFile)
	private lazy val depotsLocations: List[Point] = depotLocations(depotsFile)

	private def stopLocations(fileName: String): List[Stop] = {
			scala.io.Source.fromFile(fileName).getLines.drop(1).map(string => {
			val split = string.split("\t")
			var specialCodes = List[String]()
			if (split.size > 7) specialCodes = split(7).split(",").toList

	val geocodes: (String, String, String) = Geocoding.getCoordinates(split(3))
	if (geocodes == null) {

	}
			new Stop(split(0), geocodes._2.toDouble, geocodes._3.toDouble, split(3),
				new StopConstraints(split(4).toInt, split(5).toInt, split(6).toDouble, specialCodes))
		})
	}.toList

	private def depotLocations(fileName: String): List[Point] = {
		scala.io.Source.fromFile(fileName).getLines.drop(1).map(string => {
			val split = string.split("\t")
			new Point(split(0), split(1).toDouble, split(2).toDouble, split(3))
		})
	}.toList


	def trucks: List[Truck] = {
		var trucks = List[Truck]()
		scala.io.Source.fromFile(trucksFile).getLines.drop(1).foreach(string => {
			val split = string.split("\t")

			depots.find(depot => depot.name == split(1)) match {
				case Some(depot) => trucks ::= new Truck(split(0), depot, Nil,
					new TruckConstraints(split(3).toInt, split(4).toInt, split(2).toDouble))
				case None => println("Cannot find depot " + split(1) + "for truck")
			}
		})
		trucks
	}

	lazy val cities  = stopsLocations.map(city => addDistances(city)).toList
	lazy val depots : List[Point] = depotsLocations.map(city => addDepotDistances(city)).toList

/*	private lazy val timesAndDistances: Map[String, Map[String, (Double, Int)]] = {
		val urlx = "http://maps.googleapis.com/maps/api/distancematrix/json?mode=driving&language=en-GB&sensor=false"
		val addresses = (stopsLocations ++ depotsLocations).foldLeft(""){(addresses: String, point: Point) => addresses + point.postcode + "|"}.dropRight(1)
		val origins="&origins=" + addresses
		val destinations="&destinations=" + addresses

		val json = urlx + origins + destinations
		print(getJson(json))


		def getJson(str: String): JValue = {
			val url = new URL(str)
			val content = scala.io.Source.fromInputStream(url.openStream).getLines.mkString("\n")
	  parse(content)
		}

		Map[String, Map[String, (Double, Int)]]()
	}*/

	private def addDistances(city: Stop): Stop = {
		city.distancesAndTimes = (stopsLocations).map {
			city2 => {
				val distance = getDistance(city, city2)
				//val time = timesAndDistances(city.postcode)(city2.postcode)
				(city2, (distance, distance.toInt / 20))
			}
		}.toMap
		city
	}


	private def getDistance(stop1: Stop, stop2: Stop): Double = {
	// Math.sqrt(Math.pow(city.y - city2.y, 2) + Math.pow(city.x - city2.x, 2))
	var R = 6371 // km
	var lat1 = stop1.y
	var lat2 = stop2.y
	var lon1 = stop1.x
	var lon2 = stop2.x
	var dLat = scala.math.toRadians(lat2 - lat1)
	var dLon = scala.math.toRadians(lon2-lon1)
	lat1 = scala.math.toRadians(lat1)
	lat2 = scala.math.toRadians(lat2)

	var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	  Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2)
	var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
	R * c
	}

	private def addDepotDistances(city: Point): Point = {
		city.distancesAndTimes = stopsLocations.map {
			city2 => {
				val distance = Math.sqrt(Math.pow(city.y - city2.y, 2) + Math.pow(city.x - city2.x, 2))
				//val time = timesAndDistances[city.postcode][city2.postcode]
				(city2, (distance, distance.toInt / 20))
			}
		}.toMap
		city
	}
	}
