package cat.emir.echode

import cat.emir.echode.commands.MainCommand
import cat.emir.echode.listener.LuaEventManager
import cat.emir.echode.luavm.LuaEngine
import cat.emir.echode.script.ScriptLoader
import cat.emir.echode.variables.VariableManager
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin
import kotlin.io.path.div

class Echode : JavaPlugin() {
    companion object {
        lateinit var instance: Echode
            private set
    }

    val scriptsPath = (dataPath.toAbsolutePath() / "scripts")
    val variableManager = VariableManager(this)
    val engine = LuaEngine(this)
    val loader = ScriptLoader(this)
    val eventManager = LuaEventManager(this)

    override fun onEnable() {
        instance = this

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(MainCommand().getCommand().build())
        }

        variableManager.load()
        variableManager.startSaveTask()
        loader.load()
        eventManager.load()
    }

    override fun onDisable() {
        variableManager.save()
    }

    fun createListener(className: String, luaName: String) = eventManager.registerCustom(className, luaName)
}
