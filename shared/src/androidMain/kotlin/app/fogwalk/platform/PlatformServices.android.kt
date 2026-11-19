package app.fogwalk.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

actual class LocationTracker actual constructor() {
    actual val locations: Flow<GeoPoint> = emptyFlow()
    actual fun start() {}
    actual fun stop() {}
}

actual class StepCounter actual constructor() {
    actual fun isAvailable(): Boolean = false
    actual fun start(onSteps: (Int) -> Unit) {}
    actual fun stop() {}
}

actual object LocalNotifier {
    actual fun requestPermission() {}
    actual fun notify(title: String, body: String) {}
}

actual object SystemSharer {
    actual fun shareText(title: String, text: String) {}
}
