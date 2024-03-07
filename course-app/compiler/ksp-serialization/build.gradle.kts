plugins {
  alias(libs.plugins.kotlinJvm)
}

dependencies {
  compileOnly(libs.ksp.api)
  // https://square.github.io/kotlinpoet/
  implementation(libs.kotlinpoet)
  implementation("io.github.985892345:provider-api:${libs.versions.ktProvider.get()}")
  implementation(libs.kotlinx.serialization)
}
