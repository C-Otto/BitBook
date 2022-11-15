plugins {
    id("bitbook.java-library-conventions")
}

dependencies {
    implementation(project(":backend"))
    implementation(project(":backend:models"))
    implementation(project(":backend:transaction"))
    testFixturesImplementation("javax.persistence:javax.persistence-api")
    testImplementation(testFixtures(project(":backend:models")))
    testImplementation(testFixtures(project(":backend:transaction")))
    integrationTestImplementation(project(":backend"))
    integrationTestImplementation(project(":backend:models"))
    integrationTestImplementation(project(":backend:transaction"))
}
