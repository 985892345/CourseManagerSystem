plugins {
  com.google.devtools.ksp
  io.github.`985892345`.KtProvider
  id("app.base.multiplatform")
}

dependencies {
  add("kspCommonMainMetadata", ktProvider.ksp)
  add("kspAndroid", ktProvider.ksp)
  add("kspDesktop", ktProvider.ksp)
  add("kspIosX64", ktProvider.ksp)
  add("kspIosArm64", ktProvider.ksp)
  add("kspIosSimulatorArm64", ktProvider.ksp)
//  add("kspWasmJs", ktProvider.ksp)
}
