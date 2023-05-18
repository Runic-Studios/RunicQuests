plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.runicrealms.plugin"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(commonLibs.taskchain)
    compileOnly(commonLibs.holographicdisplays)
    compileOnly(commonLibs.spigot)
    compileOnly(commonLibs.mythicmobs)
    compileOnly(commonLibs.paper)
    compileOnly(commonLibs.placeholderapi)
    compileOnly(project(":Projects:Core"))
    compileOnly(project(":Projects:Chat"))
    compileOnly(projects(":Projects:Npcs"))
    compileOnly(projects(":Projects:Professions"))
    compileOnly(projects(":Projects:Restart"))
    compileOnly(projects(":Projects:Items"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.runicrealms.plugin"
            artifactId = "quests"
            version = "1.0-SNAPSHOT"
            from(components["java"])
        }
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
//    build {
//        dependsOn(shadowJar)
//    }
}

tasks.register("wrapper")
tasks.register("prepareKotlinBuildScriptModel")