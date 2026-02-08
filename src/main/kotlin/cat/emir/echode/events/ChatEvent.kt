package cat.emir.echode.events

import cat.emir.echode.script.event.EchodeEvent
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.Event

class ChatEvent : EchodeEvent<AsyncChatEvent> {
    override val pattern = Regex("""on chat""")
    override val event = AsyncChatEvent::class.java

    override fun parseContext(event: Event): Map<String, Any> {
        event as AsyncChatEvent
        return mapOf(
            "player" to event.player,
            "message" to event.message()
        )
    }
}