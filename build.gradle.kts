plugins {
    java
    id("com.gradleup.shadow").version("9.3.0")
}

group = "me.moros"
version = "1.0.0"

java.toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral() // for bending-api releases
    maven("https://central.sonatype.com/repository/maven-snapshots/") // for bending-api snapshots
}

dependencies {
    compileOnly("me.moros", "bending-api", "3.15.0")
    compileOnly("org.spongepowered", "configurate-core", "4.2.0")
    implementation("io.prometheus", "prometheus-metrics-core", "1.4.3")
    implementation("io.prometheus", "prometheus-metrics-exporter-httpserver", "1.4.3") {
        exclude(module = "prometheus-metrics-exposition-formats")
    }
}

tasks {
    shadowJar {
        archiveClassifier = ""
        archiveBaseName = project.name
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
