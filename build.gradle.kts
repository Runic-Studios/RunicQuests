plugins {
    `java-library`
    `maven-publish`
}

group = "com.runicrealms.plugin"
version = "1.0-SNAPSHOT"

dependencies {
    compileOnly(commonLibs.taskchain)
    compileOnly(commonLibs.holographicdisplays)
    compileOnly(commonLibs.spigot)
    compileOnly(commonLibs.mythicmobs)
    compileOnly(commonLibs.paper)
    compileOnly(commonLibs.placeholderapi)
    compileOnly(commonLibs.acf)
    compileOnly(commonLibs.springdatamongodb)
    compileOnly(commonLibs.mongodbdrivercore)
    compileOnly(commonLibs.mongodbdriversync)
    compileOnly(commonLibs.jedis)
    compileOnly(project(":Projects:Core"))
    compileOnly(project(":Projects:Chat"))
    compileOnly(project(":Projects:Npcs"))
    compileOnly(project(":Projects:Professions"))
    compileOnly(project(":Projects:Restart"))
    compileOnly(project(":Projects:Items"))
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

tasks.register("wrapper")
tasks.register("prepareKotlinBuildScriptModel")