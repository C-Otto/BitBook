plugins {
    id("bitbook.java-library-conventions")
}

dependencies {
    implementation(project(":backend:models"))
    api("com.fasterxml.jackson.core:jackson-databind")
    testImplementation(testFixtures(project(":backend:models")))
    testFixturesImplementation(testFixtures(project(":backend:models")))
    testFixturesImplementation("com.fasterxml.jackson.core:jackson-databind")
}

tasks.jar {
    archiveBaseName.set("backend-provider-base")
}
