package app.fogwalk.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.CoreMotion.CMPedometer
import platform.Foundation.NSDate
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
actual class LocationTracker actual constructor() : NSObject(), CLLocationManagerDelegateProtocol {
    private val manager = CLLocationManager()
    private val _locations = MutableSharedFlow<GeoPoint>(
        replay = 1,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    actual val locations: Flow<GeoPoint> = _locations

    init {
        manager.delegate = this
        manager.desiredAccuracy = kCLLocationAccuracyBest
        manager.distanceFilter = 5.0
        manager.allowsBackgroundLocationUpdates = true
        manager.pausesLocationUpdatesAutomatically = false
    }

    actual fun start() {
        manager.requestAlwaysAuthorization()
        manager.startUpdatingLocation()
    }

    actual fun stop() {
        manager.stopUpdatingLocation()
    }

    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        val loc = didUpdateLocations.lastOrNull() as? CLLocation ?: return
        val point = loc.coordinate.useContents {
            GeoPoint(
                latitude = latitude,
                longitude = longitude,
                accuracyMeters = loc.horizontalAccuracy.toFloat(),
            )
        }
        _locations.tryEmit(point)
    }
}

actual class StepCounter actual constructor() {
    private val pedometer = CMPedometer()

    actual fun isAvailable(): Boolean = CMPedometer.isStepCountingAvailable()

    actual fun start(onSteps: (Int) -> Unit) {
        if (!isAvailable()) return
        pedometer.startPedometerUpdatesFromDate(NSDate()) { data, _ ->
            val steps = data?.numberOfSteps?.intValue ?: return@startPedometerUpdatesFromDate
            onSteps(steps)
        }
    }

    actual fun stop() {
        pedometer.stopPedometerUpdates()
    }
}

actual object LocalNotifier {
    actual fun requestPermission() {
        UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound,
            completionHandler = { _, _ -> },
        )
    }

    actual fun notify(title: String, body: String) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
        }
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "fogwalk_${NSDate().timeIntervalSinceReferenceDate}",
            content = content,
            trigger = null,
        )
        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request, withCompletionHandler = null)
    }
}

actual object SystemSharer {
    actual fun shareText(title: String, text: String) {
        val rootVc = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
        val vc = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
        rootVc.presentViewController(vc, animated = true, completion = null)
    }
}
