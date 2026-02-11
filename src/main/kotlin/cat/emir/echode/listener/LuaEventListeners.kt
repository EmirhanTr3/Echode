package cat.emir.echode.listener

import cat.emir.echode.script.ScriptLoader
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class LuaEventListeners(val loader: ScriptLoader) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        loader.triggerEvent("join", event)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        loader.triggerEvent("quit", event)
    }

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        loader.triggerEvent("chat", event)
    }
}