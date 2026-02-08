package cat.emir.echode.events

import cat.emir.echode.script.event.EchodeEvent
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class LoadEvent : EchodeEvent<EchodeLoadEvent> {
    override val pattern = Regex("""on load""")
    override val event = EchodeLoadEvent::class.java
}

class EchodeLoadEvent : Event() {
    override fun getHandlers(): HandlerList = HandlerList()
}