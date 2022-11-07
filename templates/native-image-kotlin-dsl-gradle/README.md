This is a Gradle build script, written with the Kotlin DSL, that has dependencies and configuration items that are useful when working with [GraalVM's native image](https://www.graalvm.org/22.0/reference-manual/native-image/).

What does it provide?
- Basic setup to build a GraalVM native image with the `./gradlew nativeCompile` task
- Configuration for Bouncy Castle and other libraries so that they work in native image builds (removing signature files)
- Libraries that I use in almost every project
  - [Gson](https://github.com/google/gson)
  - [slf4j](https://www.slf4j.org/)
  - [vavr](https://www.vavr.io/)
  - [vavr-gson](https://github.com/vavr-io/vavr-gson)
  - [Immutables](https://immutables.github.io/)
