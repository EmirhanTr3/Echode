package cat.emir.echode.script.event

import org.bukkit.event.Event

interface EchodeEvent<T : Event> {
    val pattern: Regex
    val event: Class<T>

    fun matches(header: String): MatchResult? {
        return pattern.matchEntire(header)
    }

    fun parseContext(event: Event): Map<String, Any> = emptyMap()

    fun isConditionMet(match: MatchResult, event: Event): Boolean = true
}