package cat.emir.echode.script

import cat.emir.echode.luavm.LuaEngine
import java.nio.file.Path
import kotlin.io.path.readLines

class EchodeScript(val path: Path, val engine: LuaEngine) {
    var lines = path.readLines()

    fun load(resetLines: Boolean = false) {
        if (resetLines) lines = path.readLines()

        engine.lua.set("current_loading_file", path.fileName.toAbsolutePath().toString())

        engine.lua.run("""
            for eventName, handlers in pairs(internal_handlers) do
                for i = #handlers, 1, -1 do
                    if handlers[i].source == "${path.fileName.toAbsolutePath()}" then
                        table.remove(handlers, i)
                    end
                end
            end
        """.trimIndent())

        val header = """
            local script = {
                name = "${path.fileName}"
            }
        """.trimIndent()

        engine.lua.run(header + "\n" + lines.joinToString("\n"))

        val top = engine.lua.top
        engine.lua.pushNil()
        engine.lua.setGlobal("current_loading_file")
        engine.lua.top = top
    }
}