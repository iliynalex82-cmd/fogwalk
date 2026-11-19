package app.fogwalk.platform

import kotlinx.coroutines.flow.Flow

data class GeoPoint(val latitude: Double, val longitude: Double, val accuracyMeters: Float = 0f)

expect class LocationTracker() {
    val locations: Flow<GeoPoint>
    fun start()
    fun stop()
}

expect class StepCounter() {
    fun isAvailable(): Boolean
    fun start(onSteps: (Int) -> Unit)
    fun stop()
}

expect object LocalNotifier {
    fun requestPermission()
    fun notify(title: String, body: String)
}

expect object SystemSharer {
    fun shareText(title: String, text: String)
}
