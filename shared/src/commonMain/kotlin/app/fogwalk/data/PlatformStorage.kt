package app.fogwalk.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect class PlatformContext

expect fun createSettingsDataStore(context: PlatformContext): DataStore<Preferences>

expect fun getDatabasePath(context: PlatformContext, name: String): String

data class BackupCell(val id: Long, val at: Long)
data class BackupDay(
    val day: Long,
    val cells: Int,
    val meters: Double,
    val caches: Int,
    val rare: Int,
    val beacon: Boolean,
    val night: Int,
    val dawn: Int,
    val steps: Int,
)
data class BackupWalk(
    val start: Long,
    val end: Long,
    val meters: Double,
    val cells: Int,
    val caches: Int,
)
data class BackupRelic(val id: String, val at: Long, val count: Int)
data class BackupPayload(
    val version: Int = 1,
    val cells: List<BackupCell> = emptyList(),
    val days: List<BackupDay> = emptyList(),
    val walks: List<BackupWalk> = emptyList(),
    val relics: List<BackupRelic> = emptyList(),
)

expect object BackupCodec {
    fun encode(payload: BackupPayload): String
    fun decode(json: String): BackupPayload
}
