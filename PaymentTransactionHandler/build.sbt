lazy val akkaHttpVersion = "10.2.4"
lazy val akkaVersion    = "2.6.13"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.demo",
      scalaVersion    := "2.13.4"
    )),
    name := "PaymentTransactionHandler",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"                % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json"     % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-actor-typed"         % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"              % akkaVersion,
      "com.typesafe.akka" %% "akka-stream-kafka" %      "2.0.6",
      "ch.qos.logback"    % "logback-classic"           % "1.2.3",
      "com.google.code.gson" % "gson" % "1.7.1",
      "com.typesafe.akka" %% "akka-http-testkit"        % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"                % "3.1.4"         % Test
    )
  )
