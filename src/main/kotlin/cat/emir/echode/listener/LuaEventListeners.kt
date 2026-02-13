package cat.emir.echode.listener

import cat.emir.echode.script.ScriptLoader
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class LuaEventListeners(val loader: ScriptLoader) : Listener {

    @EventHandler fun on(e: PlayerJoinEvent) = trigger("join", e)
    @EventHandler fun on(e: PlayerQuitEvent) = trigger("quit", e)
    @EventHandler fun on(e: AsyncChatEvent) = trigger("chat", e)

    fun <T : Event> trigger(name: String, event: T) = loader.triggerEvent(name, event)
}