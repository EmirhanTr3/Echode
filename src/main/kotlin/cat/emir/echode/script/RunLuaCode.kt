package cat.emir.echode.script

import cat.emir.echode.luavm.LuaEngine
import org.bukkit.entity.Player
import party.iroiro.luajava.Lua
import party.iroiro.luajava.value.RefLuaValue
import java.nio.ByteBuffer
import kotlin.collections.set
import kotlin.io.path.Path

class RunLuaCode(val code: String, val engine: LuaEngine) {
    val lua = engine.lua

    fun run(sender: Player): Boolean {
        val top = lua.top

        try {
            lua.set("TEMP_RUN_SENDER", sender)

            val header = """
                local player = TEMP_RUN_SENDER
                TEMP_RUN_SENDER = nil
            """.trimIndent()

            val content = "$header\n$code"
            val bytes = content.toByteArray()
            val buffer = ByteBuffer.allocateDirect(bytes.size).put(bytes).flip()
            lua.load(buffer, "runCommand")

            lua.pCall(0, -1)

            val resultCount = lua.top - top

            for (i in 1..resultCount) {
                val index = top + i
                sender.sendRichMessage(
                    when {
                        lua.isNil(index) -> "nil"
                        lua.isBoolean(index) -> lua.toBoolean(index).toString()
                        lua.isInteger(index) -> lua.toInteger(index).toString()
                        lua.isNumber(index) -> lua.toNumber(index).toString()
                        lua.isString(index) -> lua.toString(index)!!
                        lua.isJavaObject(index) -> getJavaObjectString(lua.toJavaObject(index))
                        lua.isTable(index) -> getTableString(lua.toObject(index) as MutableMap<Any, Any>).toString()
                        lua.isFunction(index) -> getFunctionString(index)
                        else -> {
                            val value = lua.get()
                            "<red>[Unknown return type: <hover:show_text:${value::class.java.name}>${value::class.java.simpleName}</hover>]"
                        }
                    }
                )
            }

            return true
        } catch (e: Exception) {
            sender.sendRichMessage("<red>[Echode] ${e.message}")

            return false
        } finally {
            lua.pushNil()
            lua.setGlobal("TEMP_RUN_SENDER")
            lua.top = top
        }
    }

    fun getJavaObjectString(javaObject: Any?): String {
        if (javaObject is Array<*>) {
            return javaObject.toList().toString()
        }
        return javaObject.toString()
    }

    fun getTableString(map: MutableMap<Any, Any>): MutableMap<Any, Any> {
        for ((key, value) in map) {
            if (value is RefLuaValue && value.type() == Lua.LuaType.FUNCTION) {
                lua.push(value)
                map[key] = getFunctionString(lua.top)
                lua.pop(1)
            } else if (value is MutableMap<*, *>) {
                value as MutableMap<Any, Any>
                map[key] = getTableString(value)
            }
        }

        return map
    }

    fun getFunctionString(index: Int): String {
        lua.getGlobal("debug")
        lua.getField(-1, "getinfo")

        lua.pushValue(index)
        lua.push("S")

        lua.pCall(2, 1)

        lua.getField(-1, "short_src")
        val file = lua.toString(-1) ?: "unknown"
        val fileName = when {
            file.endsWith(".lua]") && !file.contains("/") -> file.removePrefix("[").removeSuffix("]")
            else -> file
        }
        lua.pop(1)

        lua.getField(-1, "linedefined")
        var lineStart = lua.toNumber(-1).toInt()
        lua.pop(1)

        lua.getField(-1, "what")
        val what = lua.toString(-1)
        lua.pop(1)

        lua.pop(2)

        if (what == "C") return "[native function]"

        val script = engine.plugin.loader.scripts[Path(file.removePrefix("[").removeSuffix("]"))]
        var name = "unknown"

        fun String.containsAll(vararg others: String): Boolean {
            for (other in others) {
                if (!this.contains(other))
                    return false
            }
            return true
        }

        fun parseName(line: String) {
            if (line.containsAll("function", "(", ")")) {
                name = line.replace(Regex(""".*function (.*)?\(.*"""), "$1")
                if (name.isEmpty())
                    name = "func"
            } else {
                name = "unknown"
            }
        }

        if (script != null) {
            lineStart -= script.header.split("\n").size
            parseName(script.lines[lineStart - 1])
        } else if (fileName == "init") {
            parseName(engine.initLines[lineStart - 1])
        }

        return "[function $name@$fileName:$lineStart]"
    }
}