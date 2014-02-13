dbbenchmark
===========

Benchmark for multi- database solution performance

===========

How-to

* Run with source code directly (after git clone, under project root directory)

./gradlew clean build run 

(modify file "scenariosetting.json" to change scenario setting, and run above SHELL command again)

OR

* Run with compiled all-in-one jar file (after git clone, under project root directory)

./gradlew clean build fatJar

Afterwars, an all-in-one jar file, together with its configuration JSON file, will be generated in build/libs.

Those two generated files can be copied together to any other locations, and executed via following java commands: 

java -jar build/libs/dbbenchmark-0.1.0.jar

(modify file "build/libs/scenariosetting.json" to change scenario setting, and run above JAVA command again)

