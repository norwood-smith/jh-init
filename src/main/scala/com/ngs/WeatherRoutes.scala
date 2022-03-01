package com.ngs

import com.ngs.WeatherService._
import com.ngs.WeatherService.GetWeather

import scala.concurrent.Future

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout

class WeatherRoutes( weatherService: ActorRef[WeatherService.Command])(implicit val system: ActorSystem[_]) {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("weather-app.routes.ask-timeout") )

  def getWeather(lat: Double, lon: Double): Future[GetWeatherResponse] = weatherService.ask( GetWeather(lat, lon, _) )

  val weatherRoute: Route =
    pathPrefix("weather") {
      path("lat" / DoubleNumber / "lon" / DoubleNumber) { (lat, lon) =>
        get {
          rejectEmptyResponse {
            onSuccess( getWeather(lat, lon) ) { response =>
              complete(response.maybeWeather)
            }
          }
        }
      }
    }

}
