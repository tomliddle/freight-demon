package com.tomliddle.util

import org.slf4j.LoggerFactory

/**
	* Created by tom on 16/12/15.
	*/
trait Logging {
	protected final val logger = LoggerFactory.getLogger(this.getClass)
}
