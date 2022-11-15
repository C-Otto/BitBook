plugins {
    id("bitbook.java-library-conventions")
}

dependencies {
    implementation(project(":backend:request:models"))
    implementation(project(":backend:models"))
    testImplementation(testFixtures(project(":backend:models")))
    testFixturesImplementation(testFixtures(project(":backend:models")))
    testFixturesImplementation(project(":backend:request:models"))
}
