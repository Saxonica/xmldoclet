plugins {
  id("application")
}

repositories {
  mavenLocal()
  mavenCentral()
}

val xmlDoclet = configurations.create("xmlDoclet") {
  extendsFrom(configurations["implementation"])
}

dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  implementation("nu.validator.htmlparser:htmlparser:1.4")
  implementation("net.sf.saxon:Saxon-HE:12.10")
  implementation("com.github.javaparser:javaparser-core:3.27.1")
  implementation(files("../xmldoclet/build/libs/xmldoclet.jar"))
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





