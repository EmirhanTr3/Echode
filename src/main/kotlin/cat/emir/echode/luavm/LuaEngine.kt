package cat.emir.echode.luavm

import cat.emir.echode.Echode
import cat.emir.echode.script.ScriptLogger
import party.iroiro.luajava.ClassPathLoader
import party.iroiro.luajava.Lua
import party.iroiro.luajava.lua53.Lua53
import java.nio.ByteBuffer

class LuaEngine(val plugin: Echode) {
    val lua: Lua = Lua53().apply {
        openLibraries()
        setExternalLoader(ClassPathLoader(LuaEngine::class.java.classLoader))
        set("plugin", plugin)
        set("logger", ScriptLogger(plugin))
        set("data", LuaDataAccessor(plugin.variableManager))
    }
    val initLines: List<String>

    init {
        val bytes = plugin.getResource("init.lua")!!.readAllBytes()
        initLines = String(bytes).split("\n")

        val buffer = ByteBuffer.allocateDirect(bytes.size).put(bytes).flip()
        lua.load(buffer, "@init")

        lua.pCall(0, 0)
    }
}