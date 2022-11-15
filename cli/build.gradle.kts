plugins {
    id("bitbook.java-conventions")
}

dependencies {
    implementation(project(":backend"))
    implementation(project(":backend:models"))
    implementation(project(":backend:price"))
    implementation(project(":backend:transaction"))
    implementation(project(":cli:base"))
    runtimeOnly(project(":backend:provider:all"))
    runtimeOnly(project(":cli:lnd"))
    runtimeOnly(project(":cli:ownership"))
    runtimeOnly("org.flywaydb:flyway-core")
    integrationTestImplementation(project(":cli:ownership"))
    testImplementation(testFixtures(project(":backend:models")))
    integrationTestImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
}

tasks.bootJar {
    archiveClassifier.set("boot")
}
tasks.bootRun {
    standardInput = System.`in`
}
