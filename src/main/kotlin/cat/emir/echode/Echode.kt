package cat.emir.echode

import cat.emir.echode.effects.BroadcastEffect
import cat.emir.echode.effects.GiveEffect
import cat.emir.echode.effects.PrintEffect
import cat.emir.echode.events.ChatEvent
import cat.emir.echode.events.LoadEvent
import cat.emir.echode.listeners.ChatListener
import cat.emir.echode.script.effect.EffectRegistry
import cat.emir.echode.script.Parser
import cat.emir.echode.script.ScriptLoader
import cat.emir.echode.script.event.EventRegistry
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Files
import kotlin.io.path.Path

class Echode : JavaPlugin() {
    companion object {
        var instance: Echode? = null
            private set
    }

    lateinit var parser: Parser
    lateinit var loader: ScriptLoader
    lateinit var effectRegistry: EffectRegistry
    lateinit var eventRegistry: EventRegistry

    override fun onEnable() {
        instance = this
        parser = Parser(this)
        loader = ScriptLoader(this)
        effectRegistry = EffectRegistry(this)
        eventRegistry = EventRegistry(this)

        if (!Files.exists(Path(dataFolder.toString(), "scripts"))) {
            Files.createDirectories(Path(dataFolder.toString(), "scripts"))
        }

        effectRegistry.register(
            PrintEffect(),
            GiveEffect(),
            BroadcastEffect()
        )

        eventRegistry.register(
            LoadEvent(),
            ChatEvent()
        )

        Bukkit.getPluginManager().registerEvents(ChatListener(this), this)

        loader.load()
    }

    override fun onDisable() {
        instance = null
    }
}
