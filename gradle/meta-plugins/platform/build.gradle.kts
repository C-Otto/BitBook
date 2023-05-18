plugins {
    `java-platform`
}

group = "de.c-otto.bitbook"

javaPlatform {
    allowDependencies()
}

dependencies {
    val springBootVersion = "2.6.15"

    api(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    api(platform("org.springframework.cloud:spring-cloud-dependencies:2021.0.5"))

    constraints {
        api("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
        api("io.vavr:vavr:0.10.4")
        api("org.apache.mina:mina-core:2.2.1")
        api("org.springframework.shell:spring-shell-starter:2.0.0.RELEASE")
    }
}
