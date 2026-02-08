package cat.emir.echode.script

import cat.emir.echode.Echode
import cat.emir.echode.script.effect.ExecutionContext
import cat.emir.echode.script.event.EventInstance
import org.bukkit.event.Event

class EchodeScript(val fileName: String) {
    val instances = mutableListOf<EventInstance>()

    fun runEvents(event: Event) {
        for (instance in instances) {
            if (!instance.eventType.event.isInstance(event)) continue
            if (!instance.eventType.isConditionMet(instance.matchResult, event)) continue

            instance.lines.forEach { line ->
                Echode.instance?.effectRegistry?.findAndExecute(line, instance.eventType.parseContext(event))
            }
        }
    }
}