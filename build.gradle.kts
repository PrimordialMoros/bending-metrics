plugins {
    java
    id("com.gradleup.shadow").version("9.4.2")
}

group = "me.moros"
version = "1.1.0"

java.toolchain {
    languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
    mavenCentral() // for bending-api releases
    maven("https://central.sonatype.com/repository/maven-snapshots/") // for bending-api snapshots
}

dependencies {
    compileOnly("me.moros:bending-api:3.16.0")
    compileOnly("org.spongepowered:configurate-core:4.2.0")
    implementation("io.prometheus:prometheus-metrics-core:1.8.0")
    implementation("io.prometheus:prometheus-metrics-exporter-httpserver:1.8.0") {
        exclude(module = "prometheus-metrics-exposition-formats")
    }
}

tasks {
    shadowJar {
        archiveClassifier = ""
        val licenseName = "LICENSE_${rootProject.name.uppercase()}"
        from("$rootDir/LICENSE") {
            into("META-INF")
            rename { licenseName }
        }
        mergeServiceFiles()
    }
    assemble {
        dependsOn(shadowJar)
    }
}
