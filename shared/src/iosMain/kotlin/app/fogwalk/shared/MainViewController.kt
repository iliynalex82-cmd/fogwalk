package app.fogwalk.shared

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIDevice
import platform.UIKit.UIViewController

actual fun platformName(): String =
    UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

fun MainViewController(): UIViewController = ComposeUIViewController {
    FogwalkIosApp()
}
