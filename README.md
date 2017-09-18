# LocationExplorer
REST api created with ***Spring Boot***. It can be used for knowing the recommanded venues near alocation (expressed ***by name*** as **London** or **Sarzana**).

It is a *maven* project, you can use the following commands from the root directory of the project:
 * **Compile:** mvn compile
 * **Test:** mvn test
 * **Execute:** java -jar target/whitbread-test-0.0.1-SNAPSHOT.jar
 * **Compile and execute:** mvn install && java -jar target/whitbread-test-0.0.1-SNAPSHOT.jar
 
# Exposed API
The RESTful api you can invoke are the following:
 * **<localhost:8080>/?loc=\<location-by-name\>** for querying for any venue near \<location-by-name\>
 * **<localhost:8080>/search/\<location-by-name\>** the same, but passing data as path parameters 
 * **<localhost:8080>/search/\<location-by-name\>/\<limit\>** the same, but it limits the number of venues returned
 * **<localhost:8080>/search/\<location-by-name\>/\<query\>/\<limit\>** the same, but passing limits and a *query*. If this quesry is a word among the ones that specify a section ("food", "drinks", "coffee", "shops", "arts", "outdoors", "sights", "trending", "specials", "nextVenues", "topPicks"), the query limits the section, otherwise it is used in a generic way
 
The component exposes also two other APIs:
 * **<localhost:8080>/query/\<location-by-name\>** used for retrieving the path that the component client invokes (used mainly for test)
 * **<localhost:8080>/status/** for the Healthcheck (this is the only API that requires the GET verb)
 
# Example of invocation
I suggest using curl for verifying that the server is working fine. Examples of the commands you can use are:
 * *curl localhost:8080/?loc=sarzana*
 * *curl localhost:8080/search/london*
 * *curl localhost:8080/search/london/1*
 * *curl localhost:8080/search/london/food/3* (*food* will be used as section)
 * *curl localhost:8080/search/sarzana/pizza/3* (*pizza* will be used as a generic query)
 
and you can also call
 * *curl localhost:8080/query/london*
 * *curl localhost:8080/status/*
 
# Approach for developing this code
I have never setup a Spring Boot project. But I found it really easy to develop, even if having not worked with that it took me a bit of time.
I would have followed a TDD approach but this usually requires to have a skeleton of the application. And I found that having the skeleton of a functionality meant almost having the full functionality.
Having followed an incremental development process (I firstly setup the API, then I added the feature of having the backend invoke multiple time the external API, then I added the error handling and finally the logs), I ended up making unit tests after the development.

I didn't test everything. And the project is missing the BDD part (cucumber tests). But I enjoyed digging in the details of all the features of Spring Boot.
