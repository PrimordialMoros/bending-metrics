plugins {
    java
}

group = "me.moros"
version = "1.7.0"

java.toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral() // for bending-api releases
    maven("https://oss.sonatype.org/content/repositories/snapshots/") // for bending-api snapshots
}

dependencies {
    compileOnly("me.moros", "bending-api", "3.12.0-SNAPSHOT")
}
