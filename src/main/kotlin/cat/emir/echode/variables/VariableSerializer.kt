package cat.emir.echode.variables

abstract class VariableSerializer<T> {

    abstract fun type(): Class<T>

    /**
     * @param source value of variable on database
     * @return value as provided type `T`
     */
    abstract fun deserialize(source: ByteArray): T?

    /**
     * @param source value as provided type `T`
     * @return value of variable on database
     */
    abstract fun serialize(source: T): ByteArray
}