plugins {
    kotlin("jvm") version "1.6.10"
    id("application")
    id("java")
    id("idea")

    id("org.graalvm.buildtools.native") version "0.9.14"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}

// If we are building a JAR file for a managed runtime we need to use Java 11. Otherwise we can use Java 17.
val javaLanguageVersion = if (System.getenv("JAR").isNullOrEmpty()) 17 else 11

tasks.distZip { enabled = false }
tasks.distTar { enabled = false }
tasks.shadowDistZip { enabled = false }
tasks.shadowDistTar { enabled = false }

val myMainClass = "com.timmattison.App"
val myImageName = myMainClass.substringAfterLast(".").toLowerCase()

application {
    mainClass.set(myMainClass)
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("app")
            mainClass.set(myMainClass)
            fallback.set(false)
            sharedLibrary.set(false)
            useFatJar.set(true)
            javaLauncher.set(javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(javaLanguageVersion))
                vendor.set(JvmVendorSpec.matching("GraalVM Community"))
            })
            buildArgs.add("-H:ClassInitialization=org.slf4j:build_time")
            buildArgs.add("-H:EnableURLProtocols=https,http")
            buildArgs.add("--initialize-at-build-time=ch.qos.logback")
            // This is "quick build" - https://docs.oracle.com/en/graalvm/enterprise/22/docs/release-notes/
            if (!System.getenv("QUICK").isNullOrEmpty()) {
                buildArgs.add("-Ob")
            }
        }
    }
}

idea.module.isDownloadSources = true
idea.module.isDownloadJavadoc = true

java.toolchain.languageVersion.set(JavaLanguageVersion.of(javaLanguageVersion))

val gradleDependencyVersion = "7.4.1"

tasks.wrapper {
    gradleVersion = gradleDependencyVersion
    distributionType = Wrapper.DistributionType.ALL
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://repo.gradle.org/gradle/libs-releases-local/")
}

tasks.distZip { enabled = false }
tasks.distTar { enabled = false }

val gsonVersion = "2.10"
val slf4jVersion = "2.0.3"
val vavrVersion = "0.10.4"
val vavrGsonVersion = "0.10.2"
val immutablesValueVersion = "2.9.2"

dependencies {
    // Prevents "java.lang.ClassNotFoundException: org.apache.commons.logging.impl.LogFactoryImpl"
    implementation("org.slf4j:jcl-over-slf4j:$slf4jVersion")

    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("io.vavr:vavr:$vavrVersion")
    implementation("io.vavr:vavr-gson:$vavrGsonVersion")

    annotationProcessor("org.immutables:value:$immutablesValueVersion")
    api("org.immutables:value:$immutablesValueVersion")
    api("org.immutables:gson:$immutablesValueVersion")

    // For RuntimeReflectionRegistrationFeature
    // Guidance from:
    // - https://github.com/otto-de/jlineup/blob/master/cli/build.gradle
    // - https://github.com/otto-de/jlineup/blob/master/cli/src/main/java/de/otto/jlineup/cli/graalvm/RuntimeReflectionRegistrationFeature.java
    api("org.graalvm.nativeimage:svm:22.2.0")

    testImplementation("junit:junit:4.13.2")
}

tasks.nativeCompileClasspathJar {
    // Signatures from BouncyCastle will break native image builds
    exclude("META-INF/*.SF", "META-INF/*.DSA")
}
