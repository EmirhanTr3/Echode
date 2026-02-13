package cat.emir.echode

import cat.emir.echode.commands.MainCommand
import cat.emir.echode.listener.LuaEventManager
import cat.emir.echode.luavm.LuaEngine
import cat.emir.echode.script.ScriptLoader
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Path

class Echode : JavaPlugin() {
    companion object {
        var instance: Echode? = null
            private set
    }

    val scriptsPath = Path.of(dataPath.toAbsolutePath().toString(), "scripts")
    val engine = LuaEngine(this)
    val loader = ScriptLoader(this)
    val eventManager = LuaEventManager(this)

    override fun onEnable() {
        instance = this

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(MainCommand().getCommand().build())
        }

        loader.load()
        eventManager.load()
    }

    override fun onDisable() {
        instance = null
    }

    fun createListener(className: String, luaName: String) = eventManager.registerCustom(className, luaName)
}
