val artifactName = "quests"
val rrGroup: String by rootProject.extra
val rrVersion: String by rootProject.extra

plugins {
    `java-library`
    `maven-publish`
}

group = rrGroup
version = rrVersion

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
    compileOnly(project(":Projects:Common"))
    compileOnly(project(":Projects:Database"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rrGroup
            artifactId = artifactName
            version = rrVersion
            from(components["java"])
        }
    }
}