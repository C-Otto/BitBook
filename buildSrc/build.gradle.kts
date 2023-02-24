plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(platform("de.c-otto.bitbook:platform"))
    implementation("de.c-otto:java-conventions:2023.02.25")
    implementation("org.springframework.boot:spring-boot-gradle-plugin")
}
