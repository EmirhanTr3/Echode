package cat.emir.echode.luavm

import cat.emir.echode.Echode
import cat.emir.echode.script.ScriptLogger
import party.iroiro.luajava.ClassPathLoader
import party.iroiro.luajava.Lua
import party.iroiro.luajava.lua53.Lua53

class LuaEngine(val plugin: Echode) {
    val lua: Lua = Lua53().apply {
        openLibraries()
        setExternalLoader(ClassPathLoader(LuaEngine::class.java.classLoader))
        set("plugin", plugin)
        set("logger", ScriptLogger(plugin))
    }

    init {
        lua.run(String(plugin.getResource("init.lua")!!.readAllBytes()))
    }
}