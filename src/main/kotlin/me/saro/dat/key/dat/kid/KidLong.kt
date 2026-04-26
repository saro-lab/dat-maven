package me.saro.dat.key.dat.kid

class KidLong(
    private var value: Long
): Kid() {
    constructor(): this(0)

    override fun toString(): String {
        return value.toString()
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other != null) {
            return when (other) {
                is KidLong -> value == other.value
                is Long -> value == other
                is String -> value == other.toLong()
                else -> false
            }
        }
        return false
    }
}