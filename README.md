# jh-weather
jack henry

# configuration
src/main/resources/application.conf : contains some configurable values. 
I used interface-port = 8081, as my machine was already using 8080.
Configure these as needed.
 
weather-app {
  system {
    interface-host = "localhost"
    interface-port = 8081
  }
  routes {
    ask-timeout = 5s
    api-key = "19910acd41211f853d61c136540c92eb"
  }
}

# clone project from github


# running the application
sbt clean compile  // should compile the project
sbt run            // should run it



# 
