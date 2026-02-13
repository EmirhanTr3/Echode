package cat.emir.echode.script

import cat.emir.echode.luavm.LuaEngine
import java.nio.ByteBuffer
import java.nio.file.Path
import kotlin.io.path.readLines
import kotlin.io.path.relativeTo

class EchodeScript(val path: Path, val engine: LuaEngine) {
    val relativePath = path.relativeTo(engine.plugin.scriptsPath)
    var lines = path.readLines()

    fun load(resetLines: Boolean = false) {
        if (resetLines) lines = path.readLines()

        engine.lua.set("current_loading_file", relativePath.toString())

        engine.lua.run("""
            for eventName, handlers in pairs(internal_handlers) do
                for i = #handlers, 1, -1 do
                    if handlers[i].source == "$relativePath" then
                        table.remove(handlers, i)
                    end
                end
            end
        """.trimIndent())

        val header = """
            local script = {
                name = "${path.fileName}",
                path = "$relativePath"
            }
        """.trimIndent()

        try {
            val content = header + "\n" + lines.joinToString("\n")
            val bytes = content.toByteArray()
            val buffer = ByteBuffer.allocateDirect(bytes.size).put(bytes).flip()
            engine.lua.load(buffer, "@[$relativePath]")

            engine.lua.pCall(0, 0)
        } catch (e: Exception) {
            engine.plugin.logger.severe("Error: ${e.message}")

            engine.plugin.server.onlinePlayers
                .filter { it.hasPermission("echode.notify.error") }
                .forEach { it.sendRichMessage("<red>[Echode] ${e.message}") }
        }

        val top = engine.lua.top
        engine.lua.pushNil()
        engine.lua.setGlobal("current_loading_file")
        engine.lua.top = top
    }
}