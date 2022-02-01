name := """makka"""
version := "1.0"
scalaVersion := "2.13.8"

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value / "scalapb"
)

val Version = new {
  val scalatest           = "3.2.10"
  val scalatestScalacheck = scalatest + ".0"
  val akka                = "2.6.18"
  val akkaHttp            = "10.2.6"
  val akkaKafka           = "2.0.3"
  val json4s              = "3.6.8"
  val scalapb             = "0.10.7"
}

val scalatest = Seq(
  "org.scalatest"     %% "scalatest"       % Version.scalatest           % Test,
  "org.scalatestplus" %% "scalacheck-1-15" % Version.scalatestScalacheck % Test
)

val circe = Seq(
  "io.circe" %% "circe-core"    % "0.14.1",
  "io.circe" %% "circe-generic" % "0.14.1",
  "io.circe" %% "circe-parser"  % "0.14.1"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka"          %% "akka-actor"                  % Version.akka,
  "com.typesafe.akka"          %% "akka-actor-testkit-typed"    % Version.akka    % Test,
  "com.typesafe.akka"          %% "akka-actor-typed"            % Version.akka,
  "com.typesafe.akka"          %% "akka-cluster"                % Version.akka,
  "com.typesafe.akka"          %% "akka-cluster-metrics"        % Version.akka,
  "com.typesafe.akka"          %% "akka-cluster-sharding"       % Version.akka,
  "com.typesafe.akka"          %% "akka-cluster-sharding-typed" % Version.akka,
  "com.typesafe.akka"          %% "akka-cluster-tools"          % Version.akka,
  "com.typesafe.akka"          %% "akka-cluster-tools"          % Version.akka,
  "com.typesafe.akka"          %% "akka-cluster-typed"          % Version.akka,
  "com.typesafe.akka"          %% "akka-distributed-data"       % Version.akka,
  "com.typesafe.akka"          %% "akka-http"                   % Version.akkaHttp,
  "com.typesafe.akka"          %% "akka-http-core"              % Version.akkaHttp,
  "com.typesafe.akka"          %% "akka-http-jackson"           % Version.akkaHttp,
  "com.typesafe.akka"          %% "akka-http-testkit"           % Version.akkaHttp,
  "com.typesafe.akka"          %% "akka-multi-node-testkit"     % Version.akka    % Test,
  "com.typesafe.akka"          %% "akka-osgi"                   % Version.akka,
  "com.typesafe.akka"          %% "akka-persistence"            % Version.akka,
  "com.typesafe.akka"          %% "akka-persistence-query"      % Version.akka,
  "com.typesafe.akka"          %% "akka-persistence-tck"        % Version.akka,
  "com.typesafe.akka"          %% "akka-persistence-typed"      % Version.akka,
  "com.typesafe.akka"          %% "akka-remote"                 % Version.akka,
  "com.typesafe.akka"          %% "akka-serialization-jackson"  % Version.akka,
  "com.typesafe.akka"          %% "akka-slf4j"                  % Version.akka,
  "com.typesafe.akka"          %% "akka-stream"                 % Version.akka,
  "com.typesafe.akka"          %% "akka-stream-kafka"           % Version.akkaKafka,
  "com.typesafe.akka"          %% "akka-stream-testkit"         % Version.akka    % Test,
  "com.typesafe.akka"          %% "akka-stream-typed"           % Version.akka,
  "com.typesafe.akka"          %% "akka-testkit"                % Version.akka    % Test,
  "org.json4s"                 %% "json4s-jackson"              % Version.json4s,
  "com.thesamet.scalapb"       %% "scalapb-runtime"             % Version.scalapb % "protobuf",
  "com.typesafe.scala-logging" %% "scala-logging"               % "3.9.4",
  "ch.qos.logback"              % "logback-classic"             % "1.2.10",
  "org.typelevel"              %% "cats-core"                   % "2.7.0",
  "org.iq80.leveldb"            % "leveldb"                     % "0.12",
  "org.fusesource.leveldbjni"   % "leveldbjni-all"              % "1.8"
) ++ scalatest ++ circe

//https://tpolecat.github.io/2017/04/25/scalac-flags.html

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8",                         // Specify character encoding used by source files.
  "-explaintypes",                 // Explain type errors in more detail.
  "-feature",                      // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials",        // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros", // Allow macro definition (besides implementation and application)
  "-language:higherKinds",         // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-language:reflectiveCalls",
  "-language:postfixOps",
  "-unchecked",  // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  //"-Xfatal-warnings",              // Fail the compilation if there are any warnings.
  "-Xlint:adapted-args",           // Warn if an argument list is modified to match the receiver.
  "-Xlint:constant",               // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select",     // Selecting member of DelayedInit.
  "-Xlint:doc-detached",           // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",           // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",              // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",   // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-unit",           // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",        // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",         // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",            // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",  // A local type parameter shadows a type already in scope.
  "-Yrangepos",
  "-Ymacro-annotations",
  "-Ywarn-dead-code",        // Warn when dead code is identified.
  "-Ywarn-extra-implicit",   // Warn when more than one implicit parameter section is defined.
  "-Ywarn-numeric-widen",    // Warn when numerics are widened.
  "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports",   // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals",    // Warn if a local definition is unused.
  "-Ywarn-unused:params",    // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars",   // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates",  // Warn if a private member is unused.
  "-Ywarn-value-discard"     // Warn when non-Unit expression results are unused.
)

scalacOptions in (Compile, console) --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings")
