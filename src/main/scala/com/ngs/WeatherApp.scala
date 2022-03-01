package com.ngs

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import scala.util.{Success, Failure}

object WeatherApp extends App {

  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    import system.executionContext

    val cfg = system.settings.config
    val host = cfg.getString("weather-app.system.interface-host")
    val port = cfg.getInt("weather-app.system.interface-port")
    val futureBinding = Http().newServerAt( host, port ).bind( routes )
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(e) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", e)
        system.terminate()
    }
  }

  val rootBehavior = Behaviors.setup[Nothing] { context =>
    val weatherActor = context.spawn(WeatherService(), "WeatherService")
    context.watch(weatherActor)

    val routes = new WeatherRoutes(weatherActor)(context.system)
    startHttpServer(routes.weatherRoute)(context.system)

    Behaviors.empty
  }

  implicit val system = ActorSystem[Nothing](rootBehavior, "WeatherServiceApp")

}
