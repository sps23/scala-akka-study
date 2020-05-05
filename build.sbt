name := "scala-akka-study"

version := "0.1"

scalaVersion := "2.12.11"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.6.5",
  "com.typesafe.akka" %% "akka-persistence" % "2.6.5",
  "org.iq80.leveldb"            % "leveldb"           % "0.12",
  "org.fusesource.leveldbjni"   % "leveldbjni-all"    % "1.8"
)