plugins {
    id("java")
}

group = "com.github.noahc77.cs4347-library-project.lambda"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    implementation("com.amazonaws:aws-lambda-java-core:1.2.1")
    implementation("com.amazonaws:aws-lambda-java-events:3.11.0")
    implementation("com.amazonaws:aws-java-sdk-rds:1.12.439")
    implementation("software.amazon.awssdk:rds:2.20.36")
    implementation("software.aws.rds:aws-mysql-jdbc:1.1.5")
    implementation("com.amazonaws.serverless:aws-serverless-java-container-spark:1.9.2")
    implementation("com.sparkjava:spark-core:2.9.4")
    implementation("mysql:mysql-connector-java:8.0.32")
    runtimeOnly("com.amazonaws:aws-lambda-java-log4j2:1.5.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.register<Zip>("buildZip") {
    from(tasks.compileJava)
    from(tasks.processResources)
    into("lib") {
        from(configurations.runtimeClasspath)
    }
}

java {
    toolchain {
//        languageVersion.set(JavaLanguageVersion.of(11))
    }
}
tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(arrayOf("--release", "11"))
}