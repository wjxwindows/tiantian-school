plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.tiantian.school"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tiantian.school"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables { useSupportLibrary = true }

        // 当前开发服务器地址。局域网地址对模拟器和同一 Wi-Fi 下的真机都可用。
        buildConfigField("String", "DEFAULT_BASE_URL", "\"http://192.168.1.32:3000/\"")
        buildConfigField("String", "DEFAULT_SCHOOL_URL", "\"http://192.168.1.32:3000/school\"")
    }

    // ---------- 双端 ----------
    // 同一套代码，通过 product flavor 产出「孩子端」「家长端」两个 APK
    flavorDimensions += "role"

    productFlavors {
        create("child") {
            dimension = "role"
            applicationId = "com.tiantian.school.child"
            resValue("string", "app_name", "天天校园·孩子端")
            buildConfigField("String", "APP_ROLE", "\"child\"")
        }
        create("parent") {
            dimension = "role"
            applicationId = "com.tiantian.school.parent"
            resValue("string", "app_name", "天天校园·家长端")
            buildConfigField("String", "APP_ROLE", "\"parent\"")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            // 上线换成正式 HTTPS 域名
            buildConfigField("String", "DEFAULT_BASE_URL", "\"https://api.tiantian.school/\"")
            buildConfigField("String", "DEFAULT_SCHOOL_URL", "\"https://myschool.tiantian.school/\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.navigation:navigation-compose:2.8.5")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")

    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    implementation("io.coil-kt:coil-compose:2.7.0")

    // 家庭配对二维码生成
    implementation("com.google.zxing:core:3.5.3")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
