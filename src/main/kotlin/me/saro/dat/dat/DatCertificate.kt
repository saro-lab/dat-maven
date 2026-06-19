package me.saro.dat.dat

import me.saro.dat.DatUtils
import me.saro.dat.Unixtime
import me.saro.dat.crypto.DatCrypto
import me.saro.dat.crypto.DatCryptoAlgorithm
import me.saro.dat.exception.DatException
import me.saro.dat.signature.DatSignature
import me.saro.dat.signature.DatSignatureAlgorithm

class DatCertificate private constructor(
    val cid: ULong,
    internal val signature: DatSignature,
    internal val crypto: DatCrypto,
    internal val datIssueBegin: ULong,
    internal val datIssueEnd: ULong,
    internal val datTtl: ULong,
): Cloneable {
    internal val cidHex = cid.toString(16)
    internal val cidHexBytes = cidHex.toByteArray()

    val expired: Boolean get() = (datIssueEnd + datTtl) < Unixtime.now().toULong()

    val issuable: Boolean get() {
        return this.signable() && Unixtime.now().toULong() in datIssueBegin..datIssueEnd
    }

    val cidLong: Long get() = cid.toLong()

    fun signable(): Boolean {
        return signature.signable()
    }
    fun supportVerifyOnly(): Boolean {
        return signature.signable()
    }

    fun exports(verifyOnly: Boolean = false): String {
        return StringBuffer()
            .append(cidHex).append('.') // cid
            .append(datIssueBegin).append('.')
            .append(datIssueEnd - datIssueBegin).append('.')
            .append(datTtl).append('.')
            .append(signature.algorithm()).append('.')
            .append(crypto.algorithm()).append('.')
            .append(DatUtils.encodeBase64Url(signature.exportKey(verifyOnly))).append('.')
            .append(DatUtils.encodeBase64Url(crypto.toBytes()))
            .toString()
    }

    override fun toString(): String {
        return exports(false)
    }

    public override fun clone(): DatCertificate {
        return DatCertificate(
            cid,
            signature.clone(),
            crypto.clone(),
            datIssueBegin,
            datIssueEnd,
            datTtl
        )
    }

    override fun equals(other: Any?): Boolean {
        if (other is DatCertificate) {
            return other.cid == this.cid
        }
        return false
    }

    override fun hashCode(): Int {
        return cid.hashCode()
    }

    companion object {
        @JvmStatic
        fun generate(cid: Long, issuedAt: Long, issuanceDuration: Long, datTtl: Long, signatureAlgorithm: DatSignatureAlgorithm, cryptoAlgorithm: DatCryptoAlgorithm): DatCertificate {
            return new(
                cid,
                issuedAt,
                issuanceDuration,
                datTtl,
                DatSignature.generate(signatureAlgorithm),
                DatCrypto.generate(cryptoAlgorithm),
            )
        }

        @JvmStatic
        fun new(cid: Long, issuedAt: Long, issuanceDuration: Long, datTtl: Long, signatureKey: DatSignature, cryptoKey: DatCrypto): DatCertificate {
            return new(cid.toULong(), issuedAt.toULong(), issuanceDuration.toULong(), datTtl.toULong(), signatureKey, cryptoKey)
        }

        @JvmStatic
        fun new(cid: ULong, issuedAt: ULong, issuanceDuration: ULong, datTtl: ULong, signatureKey: DatSignature, cryptoKey: DatCrypto): DatCertificate {
            if (issuedAt < 0UL) {
                throw DatException("issuedAt must >= 0")
            }
            if (datTtl < 1UL) {
                throw DatException("datTtl must > 0")
            }
            if (issuanceDuration < (datTtl * 2UL) && issuanceDuration < (datTtl + 3600UL)) {
                throw DatException("issuanceDuration must > (datTtl * 2) or (datTtl + 3600)")
            }
            return DatCertificate(
                cid,
                signatureKey,
                cryptoKey,
                issuedAt,
                issuedAt + issuanceDuration,
                datTtl,
            )
        }

        @JvmStatic
        fun parse(format: String): DatCertificate {
            try {
                val parts: List<String> = format.split(".")
                if (parts.size == 8) {
                    val cid = parts[0].toULong(16).toLong()
                    val issuedAt = parts[1].toULong().toLong()
                    val issuanceDuration = issuedAt - parts[2].toULong().toLong()
                    val datTtl = parts[3].toULong().toLong()
                    val signatureAlgorithm = DatSignatureAlgorithm.fromString(parts[4])
                    val cryptAlgorithm = DatCryptoAlgorithm.fromString(parts[5])
                    val signatureKey = DatSignature.fromKey(signatureAlgorithm, DatUtils.decodeBase64Url(parts[6]))
                    val cryptoKey = DatCrypto.fromBytes(cryptAlgorithm, DatUtils.decodeBase64Url(parts[7]))
                    return new(
                        cid,
                        issuedAt,
                        issuanceDuration,
                        datTtl,
                        signatureKey,
                        cryptoKey,
                    )
                }
            } catch (e: Exception) {
                if (e is DatException) {
                    throw e
                }
            }
            throw DatException("Invalid Dat Certificate Format")
        }
    }
}