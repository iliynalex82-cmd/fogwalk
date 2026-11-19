package app.fogwalk.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding

actual class PlatformContext

@OptIn(ExperimentalForeignApi::class)
private fun documentsDir(): String {
    val url = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    return requireNotNull(url?.path)
}

actual fun createSettingsDataStore(context: PlatformContext): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { "${documentsDir()}/settings.preferences_pb".toPath() }
    )
}

actual fun getDatabasePath(context: PlatformContext, name: String): String {
    return "${documentsDir()}/$name"
}

@OptIn(ExperimentalForeignApi::class)
actual object BackupCodec {
    actual fun encode(payload: BackupPayload): String {
        val dict = mapOf<Any?, Any?>(
            "version" to payload.version,
            "cells" to payload.cells.map { mapOf("id" to it.id, "at" to it.at) },
            "days" to payload.days.map {
                mapOf(
                    "day" to it.day,
                    "cells" to it.cells,
                    "meters" to it.meters,
                    "caches" to it.caches,
                    "rare" to it.rare,
                    "beacon" to it.beacon,
                    "night" to it.night,
                    "dawn" to it.dawn,
                    "steps" to it.steps,
                )
            },
            "walks" to payload.walks.map {
                mapOf(
                    "start" to it.start,
                    "end" to it.end,
                    "meters" to it.meters,
                    "cells" to it.cells,
                    "caches" to it.caches,
                )
            },
            "relics" to payload.relics.map {
                mapOf("id" to it.id, "at" to it.at, "count" to it.count)
            },
        )
        val data = NSJSONSerialization.dataWithJSONObject(dict, options = 0u, error = null)
            ?: return "{}"
        return NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString() ?: "{}"
    }

    @Suppress("UNCHECKED_CAST")
    actual fun decode(json: String): BackupPayload {
        val nsStr = json as NSString
        val data = nsStr.dataUsingEncoding(NSUTF8StringEncoding) ?: return BackupPayload()
        val obj = NSJSONSerialization.JSONObjectWithData(data, options = 0u, error = null) as? Map<String, Any?>
            ?: return BackupPayload()
        val version = (obj["version"] as? Number)?.toInt() ?: 1
        val cells = (obj["cells"] as? List<Map<String, Any?>>).orEmpty().map { m ->
            BackupCell(
                id = (m["id"] as Number).toLong(),
                at = (m["at"] as Number).toLong(),
            )
        }
        val days = (obj["days"] as? List<Map<String, Any?>>).orEmpty().map { m ->
            BackupDay(
                day = (m["day"] as Number).toLong(),
                cells = (m["cells"] as? Number)?.toInt() ?: 0,
                meters = (m["meters"] as? Number)?.toDouble() ?: 0.0,
                caches = (m["caches"] as? Number)?.toInt() ?: 0,
                rare = (m["rare"] as? Number)?.toInt() ?: 0,
                beacon = (m["beacon"] as? Boolean) ?: false,
                night = (m["night"] as? Number)?.toInt() ?: 0,
                dawn = (m["dawn"] as? Number)?.toInt() ?: 0,
                steps = (m["steps"] as? Number)?.toInt() ?: 0,
            )
        }
        val walks = (obj["walks"] as? List<Map<String, Any?>>).orEmpty().map { m ->
            BackupWalk(
                start = (m["start"] as Number).toLong(),
                end = (m["end"] as Number).toLong(),
                meters = (m["meters"] as? Number)?.toDouble() ?: 0.0,
                cells = (m["cells"] as? Number)?.toInt() ?: 0,
                caches = (m["caches"] as? Number)?.toInt() ?: 0,
            )
        }
        val relics = (obj["relics"] as? List<Map<String, Any?>>).orEmpty().map { m ->
            BackupRelic(
                id = m["id"] as String,
                at = (m["at"] as Number).toLong(),
                count = (m["count"] as? Number)?.toInt() ?: 1,
            )
        }
        return BackupPayload(version, cells, days, walks, relics)
    }
}
