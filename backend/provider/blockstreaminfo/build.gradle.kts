plugins {
    id("bitbook.java-library-conventions")
}

dependencies {
    implementation(project(":backend"))
    implementation(project(":backend:models"))
    implementation(project(":backend:provider:base"))
    implementation(project(":backend:address-transactions"))
    testImplementation(testFixtures(project(":backend:models")))
    testImplementation(testFixtures(project(":backend:provider:base")))
    testFixturesImplementation(testFixtures(project(":backend:models")))
    testFixturesImplementation(testFixtures(project(":backend:provider:base")))
}
