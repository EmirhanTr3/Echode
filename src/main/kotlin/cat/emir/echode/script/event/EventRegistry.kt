package cat.emir.echode.script.event

import cat.emir.echode.Echode
import org.bukkit.event.Event

class EventRegistry(val plugin: Echode) {
    private val events = mutableListOf<EchodeEvent<out Event>>()

    fun register(vararg events: EchodeEvent<out Event>) {
        for (event in events) {
            this.events.add(event)
        }
    }

    fun findMatch(header: String): Pair<EchodeEvent<out Event>, MatchResult>? {
        val cleanedHeader = header.replace("{", "").trim()
        for (event in events) {
            val match = event.matches(cleanedHeader)
            if (match != null) return Pair(event, match)
        }
        return null
    }
}