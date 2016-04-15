organization  := "br.com.zupme.simpleEndpoint"

version       := "0.1"

scalaVersion  := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

javaOptions in run ++= Seq("-Xms3g", "-Xmx3g", "-XX:+CMSClassUnloadingEnabled", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=3000")

libraryDependencies ++= {
  val akkaVersion = "2.3.14"
  val sprayVersion = "1.3.2"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayVersion,
    "io.spray"            %%  "spray-routing" % sprayVersion,
    "io.spray"            %%  "spray-json"    % sprayVersion,
    "io.spray"            %%  "spray-testkit" % sprayVersion  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaVersion,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaVersion   % "test",
    "org.scalatest"       %%  "scalatest"      % "2.2.4"       % "test"
  )
}

// Disable tests in assembly
test in assembly := {}

Revolver.settings
