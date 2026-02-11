package cat.emir.echode.script

import cat.emir.echode.Echode

class ScriptLogger(val plugin: Echode) {
    fun info(message: Any) = plugin.logger.info(message.toString())
    fun warning(message: Any) = plugin.logger.warning(message.toString())
    fun error(message: Any) = plugin.logger.severe(message.toString())
}