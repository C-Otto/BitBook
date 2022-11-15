plugins {
    id("bitbook.java-library-conventions")
}

dependencies {
    implementation(project(":backend"))
    implementation(project(":backend:address-transactions"))
    implementation(project(":backend:blockheight"))
    implementation(project(":backend:models"))
    implementation(project(":backend:price"))
    implementation(project(":backend:request"))
    implementation(project(":backend:request:models"))
    testImplementation(testFixtures(project(":backend:address-transactions")))
    testImplementation(testFixtures(project(":backend:models")))
    integrationTestImplementation(project(":backend:request"))
    integrationTestImplementation(project(":backend:request:models"))
    integrationTestImplementation(testFixtures(project(":backend:address-transactions")))
    integrationTestImplementation(testFixtures(project(":backend:models")))
    testFixturesImplementation(testFixtures(project(":backend:address-transactions")))
    testFixturesImplementation(testFixtures(project(":backend:models")))
    testFixturesImplementation(project(":backend:request:models"))
    testFixturesImplementation("javax.persistence:javax.persistence-api")
}
