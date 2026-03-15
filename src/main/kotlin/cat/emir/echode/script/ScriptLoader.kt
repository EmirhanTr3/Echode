package cat.emir.echode.script

import cat.emir.echode.Echode
import org.bukkit.command.CommandSender
import org.bukkit.event.Event
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.pathString

class ScriptLoader(val plugin: Echode) {
    val lua = plugin.engine.lua
    val scripts = mutableMapOf<Path, EchodeScript>()

    fun load() {
        val files = Files.walk(plugin.scriptsPath, 3)
            .filter { it.extension == "lua" }
            .toList()

        for (file in files) {
            if (file.pathString.split("/", "\\").any { it.startsWith("-") }) {
                plugin.logger.info("Skipped disabled script ${file.fileName}.")
                continue
            }

            plugin.logger.info("Loading script ${file.fileName}")
            val script = EchodeScript(file, plugin.engine)
            scripts[script.relativePath] = script
        }

        for (script in scripts.values) {
            script.load()
        }
    }

    fun loadScript(path: Path): EchodeScript? {
        if (scripts.containsKey(path)) return null

        plugin.logger.info("Loading script ${path.fileName}")
        val script = EchodeScript(path, plugin.engine)
        scripts[script.relativePath] = script

        script.load()

        return script
    }

    fun reloadScript(sender: CommandSender?, script: EchodeScript?) {
        val start = System.currentTimeMillis()

        sendReloadMessage(sender, "<aqua>[Echode] Reloading ${script?.relativePath ?: "all scripts"}...")

        if (script != null) {
            script.load(true)
        } else {
            scripts.clear()

            lua.run("internal_handlers = {}")

            load()
        }

        sendReloadMessage(sender, "<aqua>[Echode] Reloaded ${script?.relativePath ?: "all scripts"} in ${System.currentTimeMillis() - start}ms")
    }

    private fun sendReloadMessage(sender: CommandSender?, message: String) {
        if (sender != null) {
            sender.sendRichMessage(message)
        } else {
            plugin.server.onlinePlayers
                .filter { it.hasPermission("echode.notify.reload") }
                .forEach { it.sendRichMessage(message) }
        }
    }

    fun <T : Event> triggerEvent(eventName: String, event: T) {
        val topAtStart = lua.top

        try {
            lua.getGlobal("events") // Index: topAtStart + 1
            if (!lua.isTable(-1)) return

            lua.getField(-1, eventName) // Index: topAtStart + 2
            if (!lua.isTable(-1)) return

            val listIndex = lua.top
            var i = 1
            while (true) {
                lua.setTop(listIndex)

                lua.rawGetI(listIndex, i)
                if (lua.isNil(-1)) break

                if (lua.isTable(-1)) {
                    val entryIndex = lua.top
                    lua.getField(entryIndex, "fn")

                    if (lua.isFunction(-1)) {
                        lua.pushJavaObject(event)

                        kotlin.runCatching {
                            lua.pCall(1, 0)
                        }.onFailure { ex ->
                            val luaMsg = if (lua.isString(-1)) lua.toString(-1) else ex.message
                            plugin.logger.severe("[$eventName] Error: $luaMsg")

                            plugin.server.onlinePlayers
                                .filter { it.hasPermission("echode.notify.error") }
                                .forEach { it.sendRichMessage("<red>[Echode] [$eventName] $luaMsg") }
                        }
                    }
                }
                i++
            }
        } catch (e: Exception) {
            plugin.logger.severe("Echode Engine fatal JNI state: ${e.message}")
        } finally {
            lua.setTop(topAtStart)
        }
    }
}