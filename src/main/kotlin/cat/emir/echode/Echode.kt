package cat.emir.echode

import cat.emir.echode.commands.MainCommand
import cat.emir.echode.listener.LuaEventListeners
import cat.emir.echode.luavm.LuaEngine
import cat.emir.echode.script.ScriptLoader
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin

class Echode : JavaPlugin() {
    companion object {
        var instance: Echode? = null
            private set
    }
    val engine: LuaEngine = LuaEngine(this)
    val loader: ScriptLoader = ScriptLoader(this).apply { load() }

    override fun onEnable() {
        instance = this

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(MainCommand().getCommand().build())
        }

        server.pluginManager.registerEvents(LuaEventListeners(loader), this)
    }

    override fun onDisable() {
        instance = null
    }
}
