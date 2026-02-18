package cat.emir.echode.luavm

import cat.emir.echode.variables.VariableManager

class LuaDataAccessor(val variableManager: VariableManager) {
    fun <T> get(name: String): T? {
        return variableManager.getVariable(name)
    }

    fun set(name: String, value: Any?) {
        variableManager.setVariable(name, value)
    }
}