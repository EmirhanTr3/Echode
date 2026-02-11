package cat.emir.echode.script

import cat.emir.echode.Echode
import org.bukkit.command.CommandSender
import org.bukkit.event.Event
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.name

class ScriptLoader(val plugin: Echode) {
    val lua = plugin.engine.lua
    val scripts = mutableMapOf<Path, EchodeScript>()

    fun load() {
        val files = Files.walk(Path(plugin.dataPath.toString(), "scripts"), 3)
            .filter { it.extension == "lua" }
            .toList()

        for (file in files) {
            plugin.logger.info("Loading script ${file.fileName}")
            scripts[file.toAbsolutePath()] = EchodeScript(file, plugin.engine)
        }

        for (script in scripts.values) {
            script.load()
        }
    }

    fun reloadScript(sender: CommandSender?, script: EchodeScript?) {
        val start = System.currentTimeMillis()

        sendReloadMessage(sender, "<aqua>[Echode] Reloading ${script?.path?.name ?: "all scripts"}...")

        if (script != null) {
            script.load(true)
        } else {
            scripts.clear()
            load()
        }

        sendReloadMessage(sender, "<aqua>[Echode] Reloaded ${script ?: "all scripts"} in ${System.currentTimeMillis() - start}ms")
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