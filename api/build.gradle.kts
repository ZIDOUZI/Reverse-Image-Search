plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
}

android {
    namespace = "zdz.revimg.api"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// 配置发布任务
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.ZIDOUZI"
            artifactId = "revimg-api"
            version = "1.0.0"

            afterEvaluate {
                from(components["release"])
            }

            // 添加 POM 信息
            pom {
                name.set("RevImg API")
                description.set("Reverse Image Search API Library for Android")
                url.set("https://github.com/ZIDOUZI/Reverse-Image-Search")

                licenses {
                    license {
                        name.set("GNU Lesser General Public License v3.0")
                        url.set("http://www.gnu.org/licenses/lgpl-3.0.html")
                    }
                }

                developers {
                    developer {
                        id.set("ZIDOUZI")
                        name.set("ZIDOUZI")
                        email.set("53157536+ZIDOUZI@users.noreply.github.com")
                    }
                }
                
                // 为JitPack添加SCM信息
                scm {
                    connection.set("scm:git:github.com/ZIDOUZI/Reverse-Image-Search.git")
                    developerConnection.set("scm:git:ssh://github.com/ZIDOUZI/Reverse-Image-Search.git")
                    url.set("https://github.com/ZIDOUZI/Reverse-Image-Search/tree/main")
                }
            }
        }

        repositories {
            // 本地Maven仓库
            mavenLocal {
                name = "local"
            }
            
            // GitHub Packages
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/ZIDOUZI/Reverse-Image-Search")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}

// 发布任务
tasks.register("publishToLocalTest") {
    dependsOn("publishMavenPublicationToLocalRepository")
    group = "publishing"
    description = "发布到本地测试仓库 (build/local-repo)"
}