import sbt.Keys.dependencyOverrides

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.11"

lazy val root = (project in file("."))
  .settings(
    name := "marketplace",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-core" % "1.6.0",
      "com.softwaremill.sttp.tapir" %% "tapir-enumeratum" % "1.6.0",
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.4.0",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.4.0",
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % "1.4.0",
      "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % "1.4.0",
      "com.softwaremill.sttp.apispec" %% "openapi-circe-yaml" % "0.3.2",
      "com.softwaremill.sttp.tapir" %% "tapir-derevo" % "1.6.0",
      "org.typelevel" %% "cats-effect" % "3.5.0",
      "com.github.jwt-scala" %% "jwt-circe" % "9.4.0",
      "org.mindrot" % "jbcrypt" % "0.4",
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC2",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC2", // HikariCP transactor.
      "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC2", // Postgres driver 42.3.1 + type mappings."tf.tofu" %% "tofu-logging" % "0.12.0.1",
      "tf.tofu" %% "tofu-logging-derivation" % "0.12.0.1",
      "tf.tofu" %% "tofu-logging-layout" % "0.12.0.1",
      "tf.tofu" %% "tofu-logging-logstash-logback" % "0.12.0.1",
      "tf.tofu" %% "tofu-logging-structured" % "0.12.0.1",
      "tf.tofu" %% "tofu-core-ce3" % "0.12.0.1",
      "tf.tofu" %% "tofu-doobie-logging-ce3" % "0.12.0.1",
      "tf.tofu" %% "derevo-tethys" % "0.13.0",
      "com.softwaremill.sttp.client3" %% "core" % "3.8.15",
      "tf.tofu" %% "derevo-circe" % "0.13.0",
      "io.estatico" %% "newtype" % "0.4.4",
      "com.github.pureconfig" %% "pureconfig" % "0.17.4",
      "org.http4s" %% "http4s-ember-server" % "0.23.19"
    ))

dependencyOverrides += "io.circe" %% "circe-core" % "0.14.3"
scalacOptions ++= Seq("-Ymacro-annotations")

