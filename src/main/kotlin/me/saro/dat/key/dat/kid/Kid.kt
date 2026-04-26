package me.saro.dat.key.dat.kid

abstract class Kid {
    abstract override fun toString(): String
    abstract override fun hashCode(): Int
    abstract override fun equals(other: Any?): Boolean
    fun toBytes(): ByteArray {
        return toString().toByteArray()
    }

    companion object {
        @JvmField
        val BY_STRING = object: ToKid {
            override fun toKid(kid: String): Kid {
                return KidString(kid)
            }
        }

        @JvmField
        val BY_LONG = object: ToKid {
            override fun toKid(kid: String): Kid {
                return KidLong(kid.toLong())
            }
        }
    }
}