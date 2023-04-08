import org.gradle.configurationcache.extensions.capitalized

plugins {
    id("java")
}

group = "com.github.noahc77.cs4347-library-project.lambda"
version = "1.0.0"

repositories {
    mavenCentral()
}


tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(arrayOf("--release", "11"))
}

val commonCompile = configurations.create("commonCompile")
val commonRuntime = configurations.create("commonRuntime")

createSourceSetWithName("login")
createSourceSetWithName("item")
createSourceSetWithName("warehouse")
createSourceSetWithName("vendor")
createSourceSetWithName("salesHistory")
createSourceSetWithName("account")
createSourceSetWithName("sale")
createSourceSetWithName("purchaseOrder")

dependencies {
    commonCompile("com.amazonaws:aws-lambda-java-core:1.2.2")
    commonCompile("com.amazonaws:aws-lambda-java-events:3.11.1")
    commonCompile("com.amazonaws:aws-java-sdk-rds:1.12.439")
    commonCompile("software.amazon.awssdk:rds:2.20.36")
    commonCompile("software.aws.rds:aws-mysql-jdbc:1.1.5")
    commonCompile("com.amazonaws.serverless:aws-serverless-java-container-spark:1.9.2")
    commonCompile("com.sparkjava:spark-core:2.9.4")
    commonCompile("mysql:mysql-connector-java:8.0.32")
    commonRuntime("com.amazonaws:aws-lambda-java-log4j2:1.5.1")
}

sourceSets {
    main.get().compileClasspath += commonCompile
    main.get().runtimeClasspath += commonRuntime
}

tasks.register<Zip>("buildCommon") {
    from(sourceSets["main"].output)
    archiveFileName.set("common.zip")
    into("lib") {
        from(sourceSets["main"].runtimeClasspath)
        from(sourceSets["main"].compileClasspath)
    }
}

fun createSourceSetWithName(name: String) {
    java.sourceSets.create(name) {
        java.srcDir("src/$name/java/")
        resources.srcDir("src/$name/resources/")
        compileClasspath = commonCompile + sourceSets["main"].output
        runtimeClasspath = commonRuntime + sourceSets["main"].output
    }
    tasks.register<Zip>("build${name.capitalized()}") {
        dependsOn(tasks.getByName("mainJar"))
        from(sourceSets[name].output)
        archiveFileName.set("$name.zip")
        into("lib") {
            from(tasks["mainJar"].outputs.files)
            from(sourceSets[name].runtimeClasspath)
            from(sourceSets[name].compileClasspath)
        }
    }

}

tasks.register<Jar>("mainJar") {
    from(sourceSets["main"].output)
    archiveFileName.set("main.jar")
}

tasks.register("buildZips") {
    dependsOn(
        tasks.filter { it is Zip && it.name.startsWith("build") }
    )
}
