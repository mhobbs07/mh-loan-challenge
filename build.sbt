name := "aria-loan-service-challenge"

scalaVersion := "2.13.6"
val akkaVersion = "2.6.5"
val akkaHttpVersion = "10.2.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  //Adding spray json support
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,

)


