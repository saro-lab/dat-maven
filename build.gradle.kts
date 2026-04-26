import java.net.HttpURLConnection
import java.net.URI
import java.util.*

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.21"
	id("org.ec4j.editorconfig") version "0.1.0"
	id("idea")
	signing
	`maven-publish`
}

val datGroupId = "me.saro"
val datArtifactId = "dat"
val datVersion = "1.0.1"

group = datGroupId
version = datVersion

repositories {
	mavenCentral()
}

dependencies {
    implementation("org.bouncycastle:bcprov-jdk18on:1.84")

	// test
    val junitVer = "6.0.3"
	testImplementation("org.junit.jupiter:junit-jupiter:$junitVer")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitVer")
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<Test> {
	useJUnitPlatform()
	testLogging {
		events("passed", "failed", "skipped")
		showStandardStreams = true
	}
}

tasks.withType<Javadoc>().configureEach {
	options {
		this as StandardJavadocDocletOptions
		addBooleanOption("Xdoclint:none", true)
	}
}

configure<JavaPluginExtension> {
	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
}

publishing {
    publications {
        create<MavenPublication>("maven") {

            groupId = datGroupId
            artifactId = datArtifactId
            version = datVersion

            from(components["java"])

            repositories {
                maven {
                    credentials {
                        try {
                            username = project.property("sonatype.username").toString()
                            password = project.property("sonatype.password").toString()
                        } catch (e: Exception) {
                            println("warn: " + e.message)
                        }
                    }
                    name = "ossrh-staging-api"
                    url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
                }
            }

            pom {
                name.set("SARO DAT")
                description.set("SARO DAT")
                url.set("https://dat.saro.me")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        name.set("PARK Yong Seo")
                        email.set("j@saro.me")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/saro-lab/dat-maven.git")
                    developerConnection.set("scm:git:git@github.com:saro-lab/dat-maven.git")
                    url.set("https://github.com/saro-lab/dat-maven")
                }
            }
        }
    }
}

tasks.named("publish").configure {
    doLast {
        println("Ready, upload to Central Portal")
        val username = project.property("sonatype.username").toString()
        val password = project.property("sonatype.password").toString()
        val connection = URI.create("https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/$datGroupId").toURL().openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString("$username:$password".toByteArray()))
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        connection.outputStream.write("""{"publishing_type": "automatic"}""".toByteArray())
        val responseCode = connection.responseCode
        if (responseCode in 200..299) {
            println("Successfully uploaded to Central Portal")
        } else {
            throw GradleException("Failed to upload to Central Portal: $responseCode - ${connection.inputStream?.bufferedReader()?.readText()}")
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}
