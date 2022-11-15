plugins {
    id("bitbook.java-library-conventions")
}

dependencies {
    implementation(project(":backend:models"))
    implementation(project(":backend:provider:base"))
    implementation(project(":backend:blockheight"))
    testFixturesApi(testFixtures(project(":backend:provider:base")))
    testImplementation(testFixtures(project(":backend:models")))
    testFixturesImplementation(testFixtures(project(":backend:provider:base")))
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rules.forEach {rule ->
            rule.limits.forEach {limit ->
                if (limit.counter == "INSTRUCTION") {
                    limit.minimum = 0.66.toBigDecimal()
                }
                if (limit.counter == "METHOD") {
                    limit.minimum = 0.83.toBigDecimal()
                }
            }
        }
    }
}
