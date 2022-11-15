plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("de.aaschmid:gradle-cpd-plugin:3.3")
    implementation("net.ltgt.gradle:gradle-errorprone-plugin:3.0.1")
    implementation("net.ltgt.gradle:gradle-nullaway-plugin:1.5.0")
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:5.0.13")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:2.6.13")
    implementation("com.adarshr:gradle-test-logger-plugin:3.2.0")
    implementation("info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.9.0")
}
