dbbenchmark
===========

Benchmark for multi- database solution performance

===========

How-to

1. Run with source code directly (after git clone, under project root directory)

./gradlew clean build run 

(modify file "scenariosetting.json" to change scenario setting, and run above command again)

2. Run with compiled all-in-one jar file (after git clone, under project root directory)

./gradlew clean build fatJar
java -jar build/libs/dbbenchmark-0.1.0.jar

(modify file "build/libs/scenariosetting.json" to change scenario setting, and run above command again)

