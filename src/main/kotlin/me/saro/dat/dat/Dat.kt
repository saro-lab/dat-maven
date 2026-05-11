package me.saro.dat.dat

import me.saro.dat.DatUtils
import me.saro.dat.Unixtime
import me.saro.dat.exception.DatException

class Dat(
    val dat: String,
): Cloneable {
    val expire: Long
    val cid: Long
    internal val plainBytes: ByteArray
    internal val secureBytes: ByteArray
    internal val signatureBytes: ByteArray
    internal val body: ByteArray

    init {
        val parts = dat.split('.')
        if (parts.size != 5) {
            throw DatException("Invalid Dat Format")
        }
        try {
            this.expire = parts[0].toLong()
            if (expire < Unixtime.now()) {
                throw DatException("Expired Dat")
            }
            this.cid = parts[1].toULong(16).toLong()
            this.plainBytes = DatUtils.decodeBase64Url(parts[2])
            this.secureBytes = DatUtils.decodeBase64Url(parts[3])
            this.signatureBytes = DatUtils.decodeBase64Url(parts[4])
            this.body = dat.substring(0, dat.lastIndexOf('.')).toByteArray()
        } catch (e: Exception) {
            if (e is DatException) {
                throw e
            }
            throw DatException("Invalid Dat Format")
        }
    }

    public override fun clone(): Dat {
        return Dat(dat)
    }
}
