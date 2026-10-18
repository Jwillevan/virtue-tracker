package com.devin.virtuetracker.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Stores the tracked items and the per-day completion records in
 * SharedPreferences as a small JSON document. Kept dependency-free on purpose
 * (uses Android's built-in org.json) so the app stays lightweight.
 */
class TrackerRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadItems(): List<TrackedItem> {
        val raw = prefs.getString(KEY_ITEMS, null) ?: return DefaultVirtues.list
        return try {
            val arr = JSONArray(raw)
            val items = ArrayList<TrackedItem>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                items.add(
                    TrackedItem(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        description = o.optString("description", ""),
                        subItems = o.optJSONArray("subItems")?.let { children ->
                            List(children.length()) { children.getString(it) }
                        } ?: emptyList(),
                        active = o.optBoolean("active", true)
                    )
                )
            }
            items
        } catch (e: Exception) {
            DefaultVirtues.list
        }
    }

    fun saveItems(items: List<TrackedItem>) {
        val arr = JSONArray()
        for (item in items) {
            val o = JSONObject()
            o.put("id", item.id)
            o.put("name", item.name)
            o.put("description", item.description)
            o.put("subItems", JSONArray(item.subItems))
            o.put("active", item.active)
            arr.put(o)
        }
        prefs.edit().putString(KEY_ITEMS, arr.toString()).apply()
    }

    /** Returns map of date -> set of completed item ids. */
    fun loadCompletions(): MutableMap<String, MutableSet<String>> {
        val raw = prefs.getString(KEY_COMPLETIONS, null) ?: return mutableMapOf()
        return try {
            val obj = JSONObject(raw)
            val result = HashMap<String, MutableSet<String>>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val date = keys.next()
                val arr = obj.getJSONArray(date)
                val set = HashSet<String>()
                for (i in 0 until arr.length()) set.add(arr.getString(i))
                result[date] = set
            }
            result
        } catch (e: Exception) {
            mutableMapOf()
        }
    }

    fun saveCompletions(completions: Map<String, Set<String>>) {
        val obj = JSONObject()
        for ((date, ids) in completions) {
            obj.put(date, JSONArray(ids.toList()))
        }
        prefs.edit().putString(KEY_COMPLETIONS, obj.toString()).apply()
    }

    fun markSeeded() {
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    fun isSeeded(): Boolean = prefs.getBoolean(KEY_SEEDED, false)

    companion object {
        private const val PREFS_NAME = "virtue_tracker_prefs"
        private const val KEY_ITEMS = "items"
        private const val KEY_COMPLETIONS = "completions"
        private const val KEY_SEEDED = "seeded"
    }
}
