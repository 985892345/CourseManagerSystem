import utils.kspMultiplatform

plugins {
  id("com.google.devtools.ksp")
  id("io.github.985892345.KtProvider")
  id("app.base.multiplatform")
}

dependencies {
  kspMultiplatform(ktProvider.ksp)
}
