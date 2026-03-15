package cat.emir.echode

import cat.emir.echode.commands.MainCommand
import cat.emir.echode.listener.LuaEventManager
import cat.emir.echode.luavm.LuaEngine
import cat.emir.echode.script.ScriptLoader
import cat.emir.echode.variables.VariableManager
import io.papermc.paper.plugin.entrypoint.classloader.group.LockingClassLoaderGroup
import io.papermc.paper.plugin.entrypoint.classloader.group.PaperPluginClassLoaderStorage
import io.papermc.paper.plugin.entrypoint.classloader.group.SimpleListPluginClassLoaderGroup
import io.papermc.paper.plugin.entrypoint.classloader.group.SingletonPluginClassLoaderGroup
import io.papermc.paper.plugin.entrypoint.classloader.group.StaticPluginClassLoaderGroup
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.plugin.provider.classloader.PaperClassLoaderStorage
import io.papermc.paper.plugin.provider.classloader.PluginClassLoaderGroup
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.net.URLClassLoader
import java.util.jar.JarFile
import kotlin.io.path.div

class Echode : JavaPlugin(), Listener {
    companion object {
        lateinit var instance: Echode
            private set
    }

    val scriptsPath = (dataPath.toAbsolutePath() / "scripts")
    val variableManager = VariableManager(this)
    val engine = LuaEngine(this)
    val loader = ScriptLoader(this)
    val eventManager = LuaEventManager(this)
    val classes = mutableMapOf<String, URLClassLoader>()

    override fun onEnable() {
        instance = this
        loadClasses()

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(MainCommand().getCommand().build())
        }

        variableManager.load()
        variableManager.startSaveTask()
        loader.load()
        eventManager.load()

    }

    override fun onDisable() {
        variableManager.save()
    }

    fun createListener(className: String, luaName: String) = eventManager.registerCustom(className, luaName)

    fun getClass(className: String): Class<*> {
        return classes[className]?.loadClass(className) ?: Class.forName(className)
    }

    @Suppress("UnstableApiUsage")
    private fun loadClasses() {
        val pluginClassLoader = PaperClassLoaderStorage.instance() as PaperPluginClassLoaderStorage
        var pluginCount = 0

        pluginClassLoader.groups
            .flatMap(this::extractLoaders)
            .forEach { classLoader ->
//                slF4JLogger.info("classloader: $classLoader")

                classLoader.urLs.forEach { url ->
                    try {
                        val file = File(url.toURI())
                        if (file.isFile && file.extension.equals("jar", ignoreCase = true)) {
                            JarFile(file).use { jar ->
                                val entries = jar.entries()
                                while (entries.hasMoreElements()) {
                                    val entry = entries.nextElement()
                                    val name = entry.name

                                    if (name.endsWith(".class") && !entry.isDirectory
                                        && !name.startsWith("META-INF")) {
                                        val className = name
                                            .removeSuffix(".class")
                                            .replace("/", ".")

                                        classes[className] = classLoader
                                        pluginCount++
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        slF4JLogger.error("Unable to scan JAR $url:", e)
                    }
                }
            }
    }

    @Suppress("UnstableApiUsage")
    private fun extractLoaders(group: PluginClassLoaderGroup): List<URLClassLoader> {
        return when (group) {
            is SingletonPluginClassLoaderGroup -> listOfNotNull(group.access as? URLClassLoader)
            is StaticPluginClassLoaderGroup -> listOfNotNull(group.access as? URLClassLoader)
            is SimpleListPluginClassLoaderGroup -> group.classLoaders.mapNotNull { it as? URLClassLoader }
            is LockingClassLoaderGroup -> extractLoaders(group.parent)
            else -> emptyList()
        }
    }
}
