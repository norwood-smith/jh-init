package com.ngs

import spray.json.DefaultJsonProtocol

object JsonFormats  {
  import DefaultJsonProtocol._

  implicit val weatherJsonFormat = jsonFormat12(Weather)
}
