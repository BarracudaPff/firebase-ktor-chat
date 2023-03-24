plugins {
    kotlin("multiplatform") version "1.8.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

kotlin {
    jvm {
        jvmToolchain(11)
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }
    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-netty:2.0.2")
                implementation("io.ktor:ktor-server-html-builder-jvm:2.0.2")
                implementation("io.ktor:ktor-server-content-negotiation-jvm:2.0.2")
                implementation("io.ktor:ktor-server-core-jvm:2.0.2")
                implementation("io.ktor:ktor-serialization-gson:2.0.2")
                implementation("io.ktor:ktor-server-auth-jvm:2.0.2")
                implementation("io.ktor:ktor-server-netty-jvm:2.0.2")
                implementation("io.ktor:ktor-server-websockets:2.0.2")
                implementation("io.ktor:ktor-server-call-logging:2.0.2")

                implementation("io.ktor:ktor-client-logging:2.0.2")
                implementation("io.ktor:ktor-client-content-negotiation:2.0.2")
                implementation("io.ktor:ktor-client-core:2.0.2")
                implementation("io.ktor:ktor-client-cio:2.0.2")

                implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.0.2")
                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")

                implementation("com.google.firebase:firebase-admin:9.1.1")

                implementation("ch.qos.logback:logback-classic:1.4.6")
                implementation("org.apache.logging.log4j:log4j-core:2.20.0")
                implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.2.0-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.2.0-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.9.3-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:6.3.0-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-redux:4.1.2-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-redux:7.2.6-pre.346")
            }
        }
        val jsTest by getting
    }
}

application {
    mainClass.set("ru.otus.kotlin.ServerKt")
}

tasks.named<Copy>("jvmProcessResources") {
    val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
    from(jsBrowserDistribution)
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<Jar>("jvmJar"))
    classpath(tasks.named<Jar>("jvmJar"))
}
