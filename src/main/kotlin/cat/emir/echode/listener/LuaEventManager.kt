package cat.emir.echode.listener

import cat.emir.echode.Echode
import org.bukkit.event.Event
import org.bukkit.event.EventPriority

class LuaEventManager(val plugin: Echode) {
    private val registeredClasses = mutableSetOf<Class<out Event>>()

    fun load() {
        plugin.server.pluginManager.registerEvents(LuaEventListeners(plugin.loader), plugin)
    }

    fun registerCustom(className: String, luaName: String) {
        try {
            val clazz = Class.forName(className).asSubclass(Event::class.java)

            if (!registeredClasses.contains(clazz)) {
                val pluginManager = plugin.server.pluginManager

                pluginManager.registerEvent(
                    clazz,
                    CustomEventListener(clazz),
                    EventPriority.NORMAL,
                    { _, event ->
                        plugin.loader.triggerEvent(luaName, event)
                    },
                    plugin
                )

                registeredClasses.add(clazz)
                plugin.logger.info("New listener created, ${clazz.simpleName} -> $luaName")
            } else {
                plugin.logger.info("Using existing listener for $luaName ($className)")
            }

        } catch (e: Exception) {
            plugin.logger.severe("Failed to register $luaName: ${e.message}")
        }
    }
}