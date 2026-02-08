package cat.emir.echode.script

import cat.emir.echode.Echode
import cat.emir.echode.script.event.EchodeEvent
import cat.emir.echode.script.event.EventInstance
import org.bukkit.event.Event
import java.io.File

class Parser(val plugin: Echode) {
    fun parse(file: File): EchodeScript {
        val script = EchodeScript(file.name)
        val lines = file.readLines()

        var currentHeader: Pair<EchodeEvent<out Event>, MatchResult>? = null
        val currentBlockLines = mutableListOf<String>()

        for (line in lines) {
            val trimmed = line.trim()

            if (trimmed.endsWith("{")) {
                currentHeader = plugin.eventRegistry.findMatch(trimmed)
                currentBlockLines.clear()
                continue
            }

            if (trimmed == "}") {
                if (currentHeader != null) {
                    script.instances.add(EventInstance(
                        currentHeader.first,
                        currentHeader.second,
                        currentBlockLines.toList()
                    ))
                }
                currentHeader = null
                continue
            }

            if (currentHeader != null) {
                currentBlockLines.add(trimmed)
            }
        }
        return script
    }
}