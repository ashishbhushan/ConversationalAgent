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
val awsSdkVersion = "2.29.23"
val scalapbVersion = "0.11.17"
val grpcVersion = "1.68.2"  // Added specific gRPC version

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
      "io.github.ollama4j" % "ollama4j" % "1.0.89",

      // Scala Test and Scala Mock
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "org.scalatestplus" %% "scalacheck-1-15" % "3.2.11.0" % Test,
      "org.mockito" % "mockito-core" % "5.14.2" % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
      "org.scalatestplus" %% "mockito-4-11" % "3.2.18.0" % Test
    ),
      // Assembly settings
      assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) =>
        xs map {_.toLowerCase} match {
          case "services" :: xs => MergeStrategy.filterDistinctLines
          case _ => MergeStrategy.discard
        }
      case "reference.conf" => MergeStrategy.concat
      case "application.conf" => MergeStrategy.concat
      case PathList("proto", xs @ _*) => MergeStrategy.first
      case PathList("google", "protobuf", xs @ _*) => MergeStrategy.first
      case PathList("com", "google", xs @ _*) => MergeStrategy.first
      case PathList("scalapb", xs @ _*) => MergeStrategy.first
      case x if x.endsWith("io.netty.versions.properties") => MergeStrategy.first
      case "module-info.class" => MergeStrategy.discard
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    },

    assembly / assemblyJarName := "homework3-assembly.jar",

    // Exclude signature files from assembly
    assembly / assemblyExcludedJars := {
      val cp = (assembly / fullClasspath).value
      cp filter { f =>
        f.data.getName.toLowerCase match {
          case name if name.endsWith(".sf") => true
          case name if name.endsWith(".dsa") => true
          case name if name.endsWith(".rsa") => true
          case _ => false
        }
      }
    }
  )

// Enable using ScalaCheck with ScalaTest
testFrameworks += new TestFramework("org.scalatest.tools.Framework")
// Set parallel execution for tests to false using the slash syntax
Test / parallelExecution := false