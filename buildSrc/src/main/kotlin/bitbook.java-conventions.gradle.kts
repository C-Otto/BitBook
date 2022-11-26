plugins {
    id("java")
    id("bitbook.cpd")
    id("bitbook.errorprone")
    id("bitbook.checkstyle")
    id("bitbook.tests")
    id("bitbook.mutationtests")
    id("bitbook.integration-tests")
    id("bitbook.pmd")
    id("bitbook.jacoco")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("java-test-fixtures")
    id("bitbook.spotbugs")
    id("bitbook.versions")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    consistentResolution {
        useCompileClasspathVersions()
    }
}

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Werror")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("com.google.guava:guava:31.1-jre")
}