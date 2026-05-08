package me.saro.dat.key.dat

import me.saro.dat.key.DatUtils
import me.saro.dat.key.Unixtime
import me.saro.dat.key.exception.DatException

class Dat(
    val dat: String,
): Cloneable {
    val expire: Long
    val kid: String
    val plainBytes: ByteArray
    val secureBytes: ByteArray
    val signatureBytes: ByteArray
    val body: ByteArray

    init {
        val parts = dat.split('.')
        if (parts.size != 5) {
            throw DatException("Invalid Dat Format")
        }
        this.expire = try {
            parts[0].toLong()
        } catch (e: NumberFormatException) {
            throw DatException("Invalid Dat Format")
        }
        if (expire < Unixtime.now()) {
            throw DatException("Expired Dat")
        }
        this.kid = parts[1]
        this.plainBytes = DatUtils.decodeBase64Url(parts[2])
        this.secureBytes = DatUtils.decodeBase64Url(parts[3])
        this.signatureBytes = DatUtils.decodeBase64Url(parts[4])
        this.body = dat.substring(0, dat.lastIndexOf('.')).toByteArray()
    }

    public override fun clone(): Dat {
        return Dat(dat)
    }
}
