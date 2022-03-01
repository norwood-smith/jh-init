package com.ngs

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class UserRoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  lazy val testKit = ActorTestKit()

  implicit def typedSystem = testKit.system

  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.classicSystem

  val weatherService = testKit.spawn(WeatherService())
  lazy val routes = new WeatherRoutes(weatherService).weatherRoute

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._

  "WeatherRoutes" should {
    "not fail with (GET /weather/lat/37.336630/lon/-121.940120" in {

      val request = HttpRequest(uri = "/weather/lat/37.336630/lon/-121.940120")
      request ~> routes ~> check {
        //status should ===(StatusCodes.OK)
        //contentType should ===(ContentTypes.`application/json`)
        //entityAs[String] should ===("""{"users":[]}""")
      }
    }
  }

}

