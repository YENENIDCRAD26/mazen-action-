// Top-level build file where you can add configuration options common to all sub-projects/modules.
val rootKeystore = file("debug.keystore")
val rootKeystoreBase64 = file("debug.keystore.base64")
if (!rootKeystore.exists() && rootKeystoreBase64.exists()) {
  rootKeystore.writeBytes(java.util.Base64.getDecoder().decode(rootKeystoreBase64.readText().trim()))
}

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
}
