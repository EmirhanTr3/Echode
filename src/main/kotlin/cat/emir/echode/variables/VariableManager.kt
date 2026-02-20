package cat.emir.echode.variables

import cat.emir.echode.ClassUtils
import cat.emir.echode.Echode
import cat.emir.echode.variables.serializers.StringSerializer
import kotlin.io.path.createFile
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.readLines
import kotlin.io.path.writeLines

class VariableManager(val plugin: Echode) {
    val path = (plugin.dataPath / "variables.txt")
    val variables = mutableMapOf<String, Any>()
    val serializers = mutableMapOf<Class<*>, VariableSerializer<Any>>()

    @Suppress("UNCHECKED_CAST")
    fun <T> getVariable(name: String): T? {
        return variables[name] as T
    }

    fun setVariable(name: String, value: Any?) {
        if (value == null) {
            variables.remove(name)
        } else {
            variables[name] = value
        }
    }

    fun startSaveTask() {
        val time = 1 * 60 * 20L
        plugin.server.scheduler.runTaskTimerAsynchronously(plugin, this::save, time, time)
    }

    fun loadSerializers() {
        ClassUtils.findClasses("cat.emir.echode.variables.serializers", {
            it.extendsSuperclass(VariableSerializer::class.java)
        }, {
            val serializer = it.loadClass().asSubclass(VariableSerializer::class.java).constructors[0].newInstance() as VariableSerializer<Any>
            serializers[serializer.type()] = serializer
        })
    }

    fun loadVariables() {
        if (!path.exists()) path.createFile()

        val startTime = System.currentTimeMillis()

        for (line in path.readLines()) {
            val split = line.split(":")
            val serializer = serializers[Class.forName(split[1])]
            if (serializer == null) {
                plugin.logger.warning("Could not find serializer for class: ${split[1]}")
                return
            }

            variables[StringSerializer().deserialize(split[0].toByteArray())] =
                serializer.deserialize(split[2].toByteArray())!!
        }

        plugin.logger.info("Loaded ${variables.size} variables in ${System.currentTimeMillis() - startTime}ms")
    }

    fun load() {
        loadSerializers()
        loadVariables()
    }

    /**
     * @return number of variables saved
     */
    fun save(): Int {
        val lines = mutableListOf<String>()

        for ((key, value) in variables) {
            val serializer = serializers[value::class.java] ?: continue
            lines.add("${StringSerializer().serialize(key).decodeToString()}:" +
                    "${value::class.java.name}:${serializer.serialize(value).decodeToString()}")
        }

        plugin.logger.info("Saved ${lines.size} variables")
        path.writeLines(lines)

        return lines.size
    }
}