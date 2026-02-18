package cat.emir.echode.variables.serializers

import cat.emir.echode.variables.VariableSerializer
import kotlin.io.encoding.Base64

class StringSerializer : VariableSerializer<String>() {
    override fun type(): Class<String> {
        return String::class.java
    }

    override fun deserialize(source: ByteArray): String {
        return Base64.decode(source).decodeToString()
    }

    override fun serialize(source: String): ByteArray {
        return Base64.encodeToByteArray(source.toByteArray())
    }
}