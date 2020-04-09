name := "realworld-tapir-zio"

version := "0.1"

scalaVersion := "2.13.1"

resolvers += Resolver.sonatypeRepo("snapshots")

val versions = new {
  val slick = "3.3.2"
}

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % "1.0.0-RC18-2",

  "org.mindrot" % "jbcrypt" % "0.4",

  "com.softwaremill.quicklens" %% "quicklens" % "1.4.12",
  "com.github.bbottema" % "emailaddress-rfc2822" % "2.1.4",
  "io.scalaland" %% "chimney" % "0.4.0",

  "com.typesafe.slick" %% "slick" % versions.slick,
  "com.typesafe.slick" %% "slick-hikaricp" % versions.slick,
  "com.h2database" % "h2" % "1.4.200"
)
