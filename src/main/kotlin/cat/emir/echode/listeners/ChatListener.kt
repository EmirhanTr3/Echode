package cat.emir.echode.listeners

import cat.emir.echode.Echode
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ChatListener(val plugin: Echode) : Listener {

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        plugin.loader.runEvent(event)
    }
}