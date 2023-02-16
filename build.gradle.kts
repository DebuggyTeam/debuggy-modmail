plugins {
	java
	application
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

application {
	mainClass.set("io.github.debuggyteam.modmail.Main")
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("net.dv8tion:JDA:5.0.0-beta.3") {
		exclude(module = "opus-java")
	}
}
