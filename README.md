# jh-weather
jack henry

# configuration
- src/main/resources/application.conf : contains some configurable values.
- I used interface-port = 8081, as my machine was already using 8080.
- Configure these as needed.
 
* weather-app {
*   system {
*      interface-host = "localhost"
*      interface-port = 8081*    }
*  routes {
*    ask-timeout = 5s
*    api-key = "19910acd41211f853d61c136540c92eb"
*  }
*}

# clone project from github
git clone git@github.com:norwood-smith/jh-weather.git

# running the application
cd ~/path/to/project/jh-weather  //move into directory
sbt clean compile  // should compile the project
sbt run            // should run it,  or use an IDE

# use a browser to do the GET request 
http://127.0.0.1:8081/weather/lat/37.336630/lon/-121.940120 

you should see something similar to:
{"clouds":"broken clouds","heatAlert":false,"humidity":44,"hurricaneAlert":false,"iceAlert":false,"lat":37.33663,"lon":-121.94012,"temp":293.91,"tempMax":297.9,"tempMin":288.54,"wind":2.57,"windAlert":false}

