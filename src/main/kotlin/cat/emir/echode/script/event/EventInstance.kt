package cat.emir.echode.script.event

import org.bukkit.event.Event

data class EventInstance(
    val eventType: EchodeEvent<out Event>,
    val matchResult: MatchResult,
    val lines: List<String>
)