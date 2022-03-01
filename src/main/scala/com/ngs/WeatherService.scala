package com.ngs

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.Http

import io.circe._
import io.circe.parser._

final case class Weather( lat: Double, lon: Double,
                          clouds: String,
                          temp: Double, tempMin: Double, tempMax: Double,
                          wind: Double, humidity: Int,
                          windAlert: Boolean, hurricaneAlert: Boolean, heatAlert: Boolean, iceAlert: Boolean )

object WeatherService {

  sealed trait Command
  final case class GetWeather(lat: Double, lon: Double, replyTo: ActorRef[GetWeatherResponse]) extends Command
  final case class GetWeatherResponse( maybeWeather: Option[Weather] )

  def apply(): Behavior[Command] = Behaviors.setup[Command] { ctx =>
    implicit val classic = ctx.system.classicSystem
    implicit val ec = ctx.executionContext

    Behaviors.receiveMessage {
      case GetWeather(lat, lon, replyTo) =>

        val apiId = ctx.system.settings.config.getString("weather-app.routes.api-key")
        val req = s"https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&appid=${apiId}"
        val futResponse: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = req))

        Try(Await.ready(futResponse, Duration.create(3, "second") ) ) match {
          case Success(f) => f.value.get match {
            case Success( response ) =>
              Unmarshal(response.entity).to[String].onComplete({
                case Success( s ) =>
                  parse( s ).getOrElse(Json.Null) match {
                    case Json.Null => replyTo ! GetWeatherResponse( None )
                    case doc =>
                      val cursor: HCursor = doc.hcursor
                      val clouds = cursor.downField("weather").downArray.downField("description").as[String].getOrElse("")
                      val m = cursor.downField("main")
                      val temp = m.downField("temp").as[Double].getOrElse(0.0)
                      val temp_min = m.downField("temp_min").as[Double].getOrElse(0.0)
                      val temp_max = m.downField("temp_max").as[Double].getOrElse(0.0)
                      val humidity = m.downField("humidity").as[Int].getOrElse(0)
                      val wind_speed = cursor.downField("wind").downField("speed").as[Double].getOrElse(0.0)
                      val wind_alert = if ( 50 < wind_speed ) true else false
                      val hurricane_alert = if ( wind_alert && 95 < humidity ) true else false
                      val heat_alert = if ( 316.5 < temp_max) true else false
                      val ice_alert = if ( 95 < humidity && temp_min < 273.2 ) true else false
                      val w = Weather( lat, lon, clouds, temp, temp_min, temp_max,
                        wind_speed, humidity, wind_alert, hurricane_alert, heat_alert, ice_alert )
                      replyTo ! GetWeatherResponse( Some( w ) )
                  }
                case Failure( _ ) => replyTo ! GetWeatherResponse( None )
              })
            case Failure( _ ) => replyTo ! GetWeatherResponse( None )
          }
          case Failure( _ ) => replyTo ! GetWeatherResponse( None )
        }
        Behaviors.same
    }
  }

}

