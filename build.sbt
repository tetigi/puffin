name := "Puffin"

version := "0.1"

scalaVersion := "2.10.3"

unmanagedResourceDirectories in Compile += baseDirectory.value / "src/main/shaders"

unmanagedResourceDirectories in Compile += baseDirectory.value / "resources"

libraryDependencies += "org.lwjgl.lwjgl" % "lwjgl" % "2.9.1"

libraryDependencies += "org.lwjgl.lwjgl" % "lwjgl_util" % "2.9.0"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0" % "test"

libraryDependencies += "org.apache.avro" % "avro" % "1.7.6"

libraryDependencies += "com.gensler" %% "scalavro" % "0.6.2"
