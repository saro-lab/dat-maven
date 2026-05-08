package me.saro.dat.key.dat

class Payload(
    val plainBytes: ByteArray,
    val secureBytes: ByteArray,
) {
    val plain: String get() = String(plainBytes, Charsets.UTF_8)
    val secure: String get() = String(secureBytes, Charsets.UTF_8)

    override fun toString(): String {
        return "$plain/$secure"
    }
}