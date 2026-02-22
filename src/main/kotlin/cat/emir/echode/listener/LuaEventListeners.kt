package cat.emir.echode.listener

import cat.emir.echode.script.ScriptLoader
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityInteractEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.server.ServerCommandEvent

class LuaEventListeners(val loader: ScriptLoader) : Listener {

    @EventHandler fun on(e: PlayerJoinEvent) = trigger("join", e)
    @EventHandler fun on(e: PlayerQuitEvent) = trigger("quit", e)
    @EventHandler fun on(e: AsyncChatEvent) = trigger("chat", e)
    @EventHandler fun on(e: BlockBreakEvent) = trigger("block_break", e)
    @EventHandler fun on(e: BlockPlaceEvent) = trigger("block_place", e)
    @EventHandler fun on(e: PlayerCommandPreprocessEvent) = trigger("player_command", e)
    @EventHandler fun on(e: ServerCommandEvent) = trigger("server_command", e)
    @EventHandler fun on(e: PlayerDropItemEvent) = trigger("drop", e)
    @EventHandler fun on(e: PlayerInteractEvent) {
        trigger("player_interact", e)
        if (e.action.isRightClick) trigger("right_click", e)
        if (e.action.isLeftClick) trigger("left_click", e)
    }
    @EventHandler fun on(e: EntityInteractEvent) = trigger("entity_interact", e)
    @EventHandler fun on(e: EntityDamageEvent) = {
        trigger("entity_damage", e)
        if (e.entity is Player) trigger("player_damage", e)
    }
    @EventHandler fun on(e: PlayerSwapHandItemsEvent) = trigger("swap_hand", e)

    fun <T : Event> trigger(name: String, event: T) = loader.triggerEvent(name, event)
}