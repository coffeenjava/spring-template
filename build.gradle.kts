plugins {
    id("org.springframework.boot") version "3.1.3" apply false
    id("io.spring.dependency-management") version "1.1.0"
    id("java")
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "java")

    group = "com.my-api"
    java.sourceCompatibility = JavaVersion.VERSION_17

    tasks {
        jar {
            enabled = false
        }
    }

    configurations {
        all {
            // 모듈 변경 라이브러리 버전(SNAPSHOT) 캐쉬 없음
            resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
        }
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("org.apache.httpcomponents.client5:httpclient5")
        implementation("org.apache.commons:commons-lang3")
        implementation("commons-io:commons-io:2.11.0")

        testImplementation("org.springframework.boot:spring-boot-starter-test") {
            exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        }

        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        testCompileOnly("org.projectlombok:lombok")
        testAnnotationProcessor("org.projectlombok:lombok")

        // @ConfigurationProperties를 사용하기 위한 의존성
        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    }

    tasks.named("compileJava") {
        inputs.files(tasks.named("processResources"))
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.named("clean") {
        doLast {
            // clean-up directory when necessary
            file("$projectDir/src/main/generated/").deleteRecursively()
            file("$projectDir/src/test/generated_tests/").deleteRecursively()
            // For IntelliJ IDEA
            file("$projectDir/out").deleteRecursively()
        }
    }
}
