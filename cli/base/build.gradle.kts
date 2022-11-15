plugins {
    id("bitbook.java-library-conventions")
}

dependencies {
    api("org.springframework.shell:spring-shell-starter:2.0.0.RELEASE")
    implementation(project(":backend"))
    implementation(project(":backend:models"))
    implementation(project(":backend:transaction"))
    implementation(project(":backend:price"))
    implementation(project(":ownership"))
    testImplementation(testFixtures(project(":backend:transaction")))
    testImplementation(testFixtures(project(":backend:models")))
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rules.forEach {rule ->
            rule.limits.forEach {limit ->
                if (limit.counter == "BRANCH") {
                    limit.minimum = 0.91.toBigDecimal()
                }
            }
        }
    }
}

tasks.jar {
    archiveBaseName.set("cli-base")
}
