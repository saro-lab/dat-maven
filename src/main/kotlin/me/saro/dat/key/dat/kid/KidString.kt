package me.saro.dat.key.dat.kid

class KidString(
    private var value: String
): Kid() {
    constructor(): this("")

    override fun toString(): String {
        return value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other != null) {
            return when (other) {
                is KidString -> value == other.value
                is String -> value == other
                else -> false
            }
        }
        return false
    }
}