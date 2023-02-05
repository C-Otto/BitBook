plugins {
    id("bitbook.java-library-conventions")
}

dependencies {
    implementation(project(":backend"))
    implementation(project(":backend:models"))
    implementation(project(":backend:request:models"))
    implementation("org.apache.commons:commons-collections4")
    testImplementation("com.github.valfirst:slf4j-test")
    testImplementation(testFixtures(project(":backend:models")))
    integrationTestImplementation(project(":backend:models"))
    integrationTestImplementation(project(":backend:request:models"))
    configurations.named("testRuntimeOnly") {
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "org.slf4j", module = "slf4j-nop")
    }
}

tasks.jar {
    archiveBaseName.set("backend-request-models")
}
