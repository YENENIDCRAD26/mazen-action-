// Ensure debug.keystore exists at configuration time so AGP signing config validation never fails
val rootKeystore = file("debug.keystore")
val rootKeystoreBase64 = file("debug.keystore.base64")
if (!rootKeystore.exists() && rootKeystoreBase64.exists()) {
  rootKeystore.writeBytes(java.util.Base64.getDecoder().decode(rootKeystoreBase64.readText().trim()))
}

abstract class GenerateDebugKeystoreTask : DefaultTask() {
  @get:OutputFile
  abstract val keystoreFile: RegularFileProperty

  @get:InputFile
  @get:Optional
  abstract val base64File: RegularFileProperty

  @TaskAction
  fun generate() {
    val target = keystoreFile.get().asFile
    if (!target.exists()) {
      val b64 = if (base64File.isPresent) base64File.get().asFile else null
      if (b64 != null && b64.exists()) {
        target.writeBytes(java.util.Base64.getDecoder().decode(b64.readText().trim()))
        logger.lifecycle("Restored debug.keystore from debug.keystore.base64")
      } else {
        try {
          val process = ProcessBuilder(
            "keytool", "-genkeypair",
            "-alias", "androiddebugkey",
            "-keypass", "android",
            "-keystore", target.absolutePath,
            "-storepass", "android",
            "-dname", "CN=Android Debug,O=Android,C=US",
            "-keyalg", "RSA",
            "-keysize", "2048",
            "-validity", "10000"
          ).start()
          process.waitFor()
          logger.lifecycle("Generated new debug.keystore via keytool")
        } catch (e: Exception) {
          logger.warn("Could not generate debug.keystore: ${e.message}")
        }
      }
    } else {
      logger.lifecycle("debug.keystore already exists.")
    }
  }
}

// Register explicit Gradle task to generate or restore debug.keystore
tasks.register<GenerateDebugKeystoreTask>("generateDebugKeystore") {
  description = "Generates a debug.keystore file if it is missing, ensuring signing configuration does not fail."
  group = "build setup"
  keystoreFile.set(layout.projectDirectory.file("debug.keystore"))
  base64File.set(layout.projectDirectory.file("debug.keystore.base64"))
}

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
}
