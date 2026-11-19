package app.fogwalk.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import org.json.JSONArray
import org.json.JSONObject

actual typealias PlatformContext = Context

actual fun createSettingsDataStore(context: PlatformContext): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { context.filesDir.resolve("settings.preferences_pb").absolutePath.toPath() }
    )
}

actual fun getDatabasePath(context: PlatformContext, name: String): String {
    return context.getDatabasePath(name).absolutePath
}

actual object BackupCodec {
    actual fun encode(payload: BackupPayload): String {
        val root = JSONObject()
        root.put("version", payload.version)
        root.put("cells", JSONArray().apply {
            payload.cells.forEach { c ->
                put(JSONObject().put("id", c.id).put("at", c.at))
            }
        })
        root.put("days", JSONArray().apply {
            payload.days.forEach { d ->
                put(JSONObject()
                    .put("day", d.day)
                    .put("cells", d.cells)
                    .put("meters", d.meters)
                    .put("caches", d.caches)
                    .put("rare", d.rare)
                    .put("beacon", d.beacon)
                    .put("night", d.night)
                    .put("dawn", d.dawn)
                    .put("steps", d.steps))
            }
        })
        root.put("walks", JSONArray().apply {
            payload.walks.forEach { w ->
                put(JSONObject()
                    .put("start", w.start)
                    .put("end", w.end)
                    .put("meters", w.meters)
                    .put("cells", w.cells)
                    .put("caches", w.caches))
            }
        })
        root.put("relics", JSONArray().apply {
            payload.relics.forEach { r ->
                put(JSONObject()
                    .put("id", r.id)
                    .put("at", r.at)
                    .put("count", r.count))
            }
        })
        return root.toString()
    }

    actual fun decode(json: String): BackupPayload {
        val root = JSONObject(json)
        val version = root.optInt("version", 1)
        val cellsArr = root.optJSONArray("cells") ?: JSONArray()
        val cells = (0 until cellsArr.length()).map { i ->
            val o = cellsArr.getJSONObject(i)
            BackupCell(o.getLong("id"), o.getLong("at"))
        }
        val daysArr = root.optJSONArray("days") ?: JSONArray()
        val days = (0 until daysArr.length()).map { i ->
            val o = daysArr.getJSONObject(i)
            BackupDay(
                day = o.getLong("day"),
                cells = o.optInt("cells", 0),
                meters = o.optDouble("meters", 0.0),
                caches = o.optInt("caches", 0),
                rare = o.optInt("rare", 0),
                beacon = o.optBoolean("beacon", false),
                night = o.optInt("night", 0),
                dawn = o.optInt("dawn", 0),
                steps = o.optInt("steps", 0),
            )
        }
        val walksArr = root.optJSONArray("walks") ?: JSONArray()
        val walks = (0 until walksArr.length()).map { i ->
            val o = walksArr.getJSONObject(i)
            BackupWalk(
                start = o.getLong("start"),
                end = o.getLong("end"),
                meters = o.optDouble("meters", 0.0),
                cells = o.optInt("cells", 0),
                caches = o.optInt("caches", 0),
            )
        }
        val relicsArr = root.optJSONArray("relics") ?: JSONArray()
        val relics = (0 until relicsArr.length()).map { i ->
            val o = relicsArr.getJSONObject(i)
            BackupRelic(
                id = o.getString("id"),
                at = o.getLong("at"),
                count = o.optInt("count", 1),
            )
        }
        return BackupPayload(version, cells, days, walks, relics)
    }
}
