plugins {
    java
    id("com.gradleup.shadow").version("8.3.6")
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
    compileOnly("me.moros", "bending-api", "3.12.0")
    compileOnly("org.spongepowered", "configurate-core", "4.2.0")
    implementation("io.prometheus", "prometheus-metrics-core", "1.3.6")
    implementation("io.prometheus", "prometheus-metrics-exporter-httpserver", "1.3.6") {
        exclude(module = "prometheus-metrics-exposition-formats")
    }
}

tasks {
    shadowJar {
        archiveClassifier = ""
        archiveBaseName = project.name
        from(rootDir.resolve("LICENSE")) {
            rename { "META-INF/${it}_${rootProject.name.uppercase()}" }
        }
        mergeServiceFiles()
    }
    assemble {
        dependsOn(shadowJar)
    }
}
