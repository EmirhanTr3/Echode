package cat.emir.echode.variables.serializers

import cat.emir.echode.variables.VariableSerializer

class IntegerSerializer : VariableSerializer<Int>() {
    override fun type(): Class<Int> {
        return Int::class.java
    }

    override fun deserialize(source: ByteArray): Int {
        return source.decodeToString().toInt()
    }

    override fun serialize(source: Int): ByteArray {
        return source.toString().encodeToByteArray()
    }
}