plugins {
  `kotlin-dsl`
}

dependencies {
  implementation(projects.base)
  implementation(libs.android.gradlePlugin)
  implementation(libs.kotlin.gradlePlugin)
  implementation(libs.compose.gradlePlugin)
  implementation(libs.ksp.gradlePlugin)
  implementation(libs.ktProvider.gradlePlugin)
  implementation(libs.kotlinx.serialization.gradlePlugin)
  implementation(libs.ktor.fit.gradlePlugin)
  implementation(libs.ktorfit.gradlePlugin)
}