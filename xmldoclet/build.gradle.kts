plugins {
  id("java-library")
  id("maven-publish")
  id("signing")
  id("com.github.gmazzo.buildconfig") version "5.5.0"
}

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

repositories {
  mavenLocal()
  mavenCentral()
}

val docletName = project.findProperty("docletName") as String
val docletTitle = project.findProperty("docletTitle") as String
val docletVersion = project.findProperty("docletVersion") as String
val schemaVersion = project.findProperty("schemaVersion") as String

val xmlDoclet = configurations.create("xmlDoclet") {
  extendsFrom(configurations["implementation"])
}

dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  api("nu.validator.htmlparser:htmlparser:1.4")
  api("net.sf.saxon:Saxon-HE:12.10")
  implementation("net.sf.saxon:Saxon-HE:12.10")
  implementation("com.github.javaparser:javaparser-core:3.27.1")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(17)
  }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
    }
}

// ============================================================

fun dateTime(): String {
  val tz = TimeZone.getTimeZone("UTC");
  val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
  df.setTimeZone(tz);
  return df.format(Date());
}

val gitHash = providers.exec {
  commandLine("git", "rev-parse", "--short", "HEAD")
}

buildConfig {
  packageName("com.saxonica.xmldoclet")
  buildConfigField("TITLE", "${docletTitle}")
  buildConfigField("VERSION", "${docletVersion}")
  buildConfigField("SCHEMA_VERSION", "${schemaVersion}")
  buildConfigField("PUB_DATE", dateTime())
  buildConfigField("PUB_HASH",
                   gitHash.standardOutput.asText.get().trim())
}

// ============================================================

tasks.register<Javadoc>("xmljavadoc") {
  group = "documentation"
  classpath = configurations["xmlDoclet"]
  source = sourceSets["main"].allJava
  destinationDir = layout.buildDirectory.file("xmldoc").get().asFile
  options.doclet = "com.saxonica.xmldoclet.XmlDoclet"

  val fileList = mutableListOf<File>()
  fileList.addAll(configurations["xmlDoclet"].files
                  + layout.buildDirectory.file("classes/java/main").get().asFile)
  options.docletpath = fileList

  val sep = System.getProperty("path.separator")

  val opts = options as StandardJavadocDocletOptions
  opts.addStringOption("sourcepath", sourceSets["main"].java.srcDirs.joinToString(sep))
}

// ============================================================

tasks.register("dist") {
  dependsOn("jar", "test", "copyStage", "copyStageDocs")
  finalizedBy("zipDist")
}

tasks.register<Copy>("copyStage") {
  dependsOn("jar")
  into(layout.buildDirectory.dir("stage"))
  from(layout.buildDirectory.dir("libs"))
}

tasks.register<Copy>("copyStageDocs") {
  dependsOn("jar")
  into(layout.buildDirectory.dir("stage/docs"))
  from(layout.projectDirectory.dir("..")) {
    include("README.md")
  }
}

tasks.register<Zip>("zipDist") {
  into("${docletName}-${docletVersion}")
  from(layout.buildDirectory.dir("stage"))
  archiveFileName = "${docletName}-${docletVersion}.zip"
}

// ============================================================

val javadocJar = tasks.register<Jar>("javadocJar") {
  dependsOn("javadoc")
  archiveClassifier = "javadoc"
  //from javadoc.destinationDir
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
  archiveClassifier = "sources"
  from(sourceSets["main"].allSource)
}

publishing {
  publications {
    create<MavenPublication>("mavenDoclet") {
      pom {
        name = docletTitle
        packaging = "jar"
        description = "An XML JavaDoc doclet"
        url = "https://github.com/saxonica/xmldoclet"

        scm {
          url = "scm:git@github.com:saxonica/xmldoclet.git"
          connection = "scm:git@github.com:saxonica/xmldoclet.git"
          developerConnection = "scm:git@github.com:saxonica/xmldoclet.git"
        }

        licenses {
          license {
            name = "Mozilla Public License Version 2.0"
            url = "http://www.mozilla.org/MPL/2.0/"
            distribution = "repo"
          }
        }

        developers {
          developer {
            id = "ndw"
            name = "Norman Walsh"
          }
        }
      }

      groupId = "com.saxonica"
      artifactId = docletName
      version = docletVersion
      from(components["java"])
      artifact(javadocJar.get())
      artifact(sourcesJar.get())
    }
  }

  repositories {
    maven {
      url = uri("build/maven")
    }
  }
}

signing {
  sign(publishing.publications["mavenDoclet"])
}

// ============================================================

tasks.register("helloWorld") {
  doLast {
    println("Hello ${dateTime()}")
  }
}

