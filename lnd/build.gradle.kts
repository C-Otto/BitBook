plugins {
    id("bitbook.java-library-conventions")
}

dependencies {
    implementation(project(":backend"))
    implementation(project(":backend:models"))
    implementation(project(":backend:transaction"))
    implementation(project(":ownership"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    testImplementation(testFixtures(project(":backend:models")))
    testFixturesImplementation(testFixtures(project(":backend:models")))
}
