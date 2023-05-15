plugins {
	java
	application

	id("com.github.johnrengelman.shadow") version "8.1.1"
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

application {
	mainClass.set("gay.debuggy.modmail.Main")
}

repositories {
	mavenCentral()
}

dependencies {
	shadow("net.dv8tion:JDA:5.0.0-beta.9") {
		exclude(module = "opus-java")
	}
	shadow("com.google.guava:guava:31.1-jre")
	shadow("org.slf4j:slf4j-log4j12:2.0.7")

	testImplementation(platform("org.junit:junit-bom:5.9.3"))
	testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks {
	withType<JavaCompile> {
		options.encoding = "UTF-8"
	}
}

tasks.test {
	useJUnitPlatform()
	testLogging {
		events("passed", "skipped", "failed")
	}
}

tasks.jar {
	manifest {
		attributes["Main-Class"] = application.mainClass
	}
}
