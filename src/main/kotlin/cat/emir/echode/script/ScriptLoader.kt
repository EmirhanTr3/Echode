package cat.emir.echode.script

import cat.emir.echode.Echode
import cat.emir.echode.events.EchodeLoadEvent
import cat.emir.echode.script.effect.ExecutionContext
import org.bukkit.event.Event
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.extension

class ScriptLoader(val plugin: Echode) {
    val scripts = mutableListOf<EchodeScript>()

    fun load() {
        val files = Files.walk(Path(plugin.dataPath.toString(), "scripts"), 3)
            .filter { it.extension == "ecd" }
            .toList()

        for (file in files) {
            plugin.logger.info("Loading script ${file.fileName}")
            scripts.add(plugin.parser.parse(file.toFile()))
        }

        val loadEvent = EchodeLoadEvent()

        runEvent(loadEvent)
    }

    fun runEvent(event: Event) {
        for (script in scripts) {
            script.runEvents(event)
        }
    }
}