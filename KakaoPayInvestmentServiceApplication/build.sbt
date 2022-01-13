name := "KakaoPayInvestmentServiceApplication"

version := "0.1"

scalaVersion := "2.13.6"

libraryDependencies ++= Seq(
  "org.springframework.boot" % "spring-boot-starter-web" % "1.5.4.RELEASE",
  "org.springframework.boot" % "spring-boot-configuration-processor" % "1.5.4.RELEASE",
  "org.springframework.boot" % "spring-boot-starter-data-jpa" % "1.5.4.RELEASE",
  "org.springframework.boot" % "spring-boot-starter-test" % "1.5.4.RELEASE",
  "org.projectlombok" % "lombok" % "1.16.22",
  "com.h2database" % "h2" % "1.4.200",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.9.8",
  "org.scalatest" %% "scalatest" % "3.2.9" % "test",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.12.3",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.3"
)


// enable the Java app packaging archetype
enablePlugins(JavaServerAppPackaging)