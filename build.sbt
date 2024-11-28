ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.12"
ThisBuild / organization := "hw3"

val akkaVersion = "2.8.8"
val akkaHttpVersion = "10.5.3"
val scalaTestVersion = "3.2.19"
val typeSafeConfigVersion = "1.4.3"
val logbackVersion = "1.5.12"
val slf4jVersion = "2.0.16"
val awsLambdaVersion = "1.2.3"
val awsSdkVersion = "2.29.20"
val scalapbVersion = "0.11.17"
val grpcVersion = "1.68.1"  // Added specific gRPC version

// Add this at the top level
Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
)

lazy val root = (project in file("."))
  .settings(
    name := "homework3",
    libraryDependencies ++= Seq(
      // Akka HTTP
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,

      // AWS
      "software.amazon.awssdk" % "lambda" % awsSdkVersion,
      "software.amazon.awssdk" % "apigateway" % awsSdkVersion,
      "com.amazonaws" % "aws-lambda-java-core" % awsLambdaVersion,
      "software.amazon.awssdk" % "bedrockruntime" % awsSdkVersion,

      // gRPC & ScalaPB - Updated with specific versions
      "io.grpc" % "grpc-netty" % grpcVersion,
      "io.grpc" % "grpc-protobuf" % grpcVersion,
      "io.grpc" % "grpc-stub" % grpcVersion,
      "io.grpc" % "grpc-netty-shaded" % grpcVersion,  // Added shaded netty
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapbVersion % "protobuf",
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapbVersion,

      // Configuration & Logging
      "com.typesafe" % "config" % typeSafeConfigVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "org.slf4j" % "slf4j-api" % slf4jVersion,

      // Ollama Client
      "io.github.ollama4j" % "ollama4j" % "1.0.89"
    )
  )