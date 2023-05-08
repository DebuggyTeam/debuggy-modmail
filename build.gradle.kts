plugins {
	java
	application
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
	implementation("net.dv8tion:JDA:5.0.0-beta.3") {
		exclude(module = "opus-java")
	}
	implementation("com.google.guava:guava:31.1-jre")

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
