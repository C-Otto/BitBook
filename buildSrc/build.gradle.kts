plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(platform("de.c-otto.bitbook:platform"))
    implementation("de.c-otto:java-conventions:2023.03.22")
    implementation("org.springframework.boot:spring-boot-gradle-plugin")
}
