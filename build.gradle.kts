import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jooq.meta.jaxb.Property

plugins {
    id("org.springframework.boot") version "2.3.5.RELEASE"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    id("org.flywaydb.flyway") version "7.2.0"
    id("nu.studer.jooq") version "5.2"
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.spring") version "1.3.72"
}

group = "ru.stubby.project"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.flywaydb:flyway-core")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    runtimeOnly("org.postgresql:postgresql")
    jooqGenerator("org.postgresql:postgresql:42.2.14")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

flyway {
    url = "jdbc:postgresql://localhost:5432/postgres"
    user = "postgres"
    password = "postgres"
    locations = arrayOf("filesystem:src/main/resources/migration")
}

jooq {
    version.set("3.14.1")
    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)

    configurations {
        create("javaClassesFromDB") {
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5432/postgres"
                    user = "postgres"
                    password = "postgres"
                    properties.add(Property().withKey("PAGE_SIZE").withValue("2048"))
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        forcedTypes.addAll(
                            arrayOf(
                                org.jooq.meta.jaxb.ForcedType()
                                    .withName("varchar")
                                    .withIncludeExpression(".*")
                                    .withIncludeTypes("JSONB?"),
                                org.jooq.meta.jaxb.ForcedType()
                                    .withName("varchar")
                                    .withIncludeExpression(".*")
                                    .withIncludeTypes("INET")
                            ).toList()
                        )
                    }
                    generate.apply {
                        isRelations = true
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                        isJavaTimeTypes = true
                    }
                    target.apply {
                        packageName = "ru.stubby.project.jooq"
                        directory = "src/main/java"
                    }
                }
            }
        }

        create("xmlFromDB") {
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5432/postgres"
                    user = "postgres"
                    password = "postgres"
                    properties.add(Property().withKey("PAGE_SIZE").withValue("2048"))
                }
                generator.apply {
                    name = "org.jooq.codegen.XMLGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        includes = ".*"
                    }
                    generate.apply {
                        isRelations = true
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                        isJavaTimeTypes = true
                    }
                    target.apply {
                        packageName = "jooq"
                        directory = "src/main/resources"
                    }
                }
            }
        }

        create("javaClassesFromXml") {
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.ERROR
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5432/postgres"
                    user = "postgres"
                    password = "postgres"
                }
                generator.apply {
                    database.apply {
                        name = "org.jooq.meta.xml.XMLDatabase"
                        properties.add(Property().withKey("xmlFile").withValue("src/main/resources/jooq/information_schema.xml"))
                        properties.add(Property().withKey("dialect").withValue("POSTGRES"))
                    }
                    generate.apply {
                        isRelations = true
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                        isJavaTimeTypes = true
                    }
                    target.apply {
                        packageName = "ru.stubby.project.jooq"
                        directory = "src/main/java"
                    }
                }
            }
        }
    }
}
