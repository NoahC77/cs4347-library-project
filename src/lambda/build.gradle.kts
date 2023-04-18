import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.beust.klaxon.Parser
import org.gradle.configurationcache.extensions.capitalized

typealias BAOS = java.io.ByteArrayOutputStream

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
createSourceSetWithName("account")
createSourceSetWithName("sale")
createSourceSetWithName("purchaseOrder")
createSourceSetWithName("suppliedItem")
createSourceSetWithName("optimizer")

dependencies {
    commonCompile("org.projectlombok:lombok:1.18.26")
    commonCompile("com.amazonaws:aws-lambda-java-core:1.2.2")
    commonCompile("com.amazonaws:aws-lambda-java-events:3.11.1")
    commonCompile("com.amazonaws:aws-java-sdk-rds:1.12.439")
    commonCompile("software.amazon.awssdk:rds:2.20.36")
    commonCompile("software.aws.rds:aws-mysql-jdbc:1.1.5")
    commonCompile("com.amazonaws.serverless:aws-serverless-java-container-spark:1.9.2")
    commonCompile("com.sparkjava:spark-core:2.9.4")
    commonCompile("mysql:mysql-connector-java:8.0.32")
    commonRuntime("com.amazonaws:aws-lambda-java-log4j2:1.5.1")
    commonCompile("com.google.code.gson:gson:2.10.1")
    commonCompile("org.slf4j:jcl-over-slf4j:2.0.5")
    commonCompile("org.slf4j:slf4j-log4j12:2.0.5")
    commonCompile("com.amazonaws:aws-lambda-java-log4j:1.0.1")
    annotationProcessor("org.projectlombok:lombok:1.18.26")
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

        val annotationProcessorConfig = configurations.getByName(annotationProcessorConfigurationName)
        dependencies {
            annotationProcessorConfig("org.projectlombok:lombok:1.18.26")
        }
    }
    val buildTaskName = "build${name.capitalized()}"
    tasks.register<Zip>(buildTaskName) {
        dependsOn(tasks.getByName("mainJar"))
        from(sourceSets[name].output)
        archiveFileName.set("$name.zip")
        into("lib") {
            from(tasks["mainJar"].outputs.files)
            from(sourceSets[name].runtimeClasspath)
            from(sourceSets[name].compileClasspath)
        }
    }

    val getCategoryARNTaskName = "getCategoryARN${name}"
    val categoryARN = "${name}ARN"
    tasks.register<Exec>(getCategoryARNTaskName) {
        commandLine(
            "aws",
            "cloudformation",
            "describe-stacks",
            "--stack-name",
            "LambdaStack",
            "--query",
            "Stacks[0].Outputs[?OutputKey=='${name}ARN'].{category:OutputKey,arn:OutputValue}[0]"
        )
        standardOutput = BAOS()

        doLast {
            val arn = (standardOutput as BAOS).toByteArray().toString(Charsets.UTF_8)
            ext[categoryARN] = arn
        }
    }
    tasks.register<Exec>("update${name.capitalized()}") {
        dependsOn(tasks.getByName(buildTaskName))
        dependsOn(tasks.getByName(getCategoryARNTaskName))

        val categoryZipFilePath = tasks.getByName(buildTaskName).outputs.files.singleFile.absolutePath

        commandLine("aws")
        argumentProviders.add(CommandLineArgumentProvider {
            val arn = (Parser.default().parse(StringBuilder(ext[categoryARN] as String)) as JsonObject)["arn"] as String

            listOf(
                "lambda",
                "update-function-code",
                "--function-name",
                arn,
                "--zip-file",
                "fileb://${categoryZipFilePath}"
            )
        })
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

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("com.beust:klaxon:5.5")
    }
}