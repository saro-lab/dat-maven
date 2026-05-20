package me.saro.dat.dat

import me.saro.dat.DatUtils
import me.saro.dat.Unixtime
import me.saro.dat.crypto.DatCryptoAlgorithm
import me.saro.dat.crypto.DatCryptoKey
import me.saro.dat.exception.DatException
import me.saro.dat.signature.DatSignatureAlgorithm
import me.saro.dat.signature.DatSignatureKey
import me.saro.dat.signature.DatSignatureKeyExportOption

class DatCertificate private constructor(
    val cid: Long,
    internal val signatureKey: DatSignatureKey,
    internal val cryptoKey: DatCryptoKey,
    internal val datIssueBegin: Long,
    internal val datIssueEnd: Long,
    internal val datTtl: Long,
): Cloneable {
    internal val cidHex = cid.toString(16).toByteArray()

    val expired: Boolean get() = datIssueEnd + datTtl < Unixtime.now()

    val issuable: Boolean get() {
        return this.hasSigningKey() && Unixtime.now() in datIssueBegin..datIssueEnd
    }

    fun hasSigningKey(): Boolean {
        return signatureKey.hasSigningKey()
    }

    fun exports(signatureKeyExportOption: DatSignatureKeyExportOption): String {
        val signatureKeyBase64 = when (signatureKeyExportOption) {
            DatSignatureKeyExportOption.PAIR -> "${DatUtils.encodeBase64Url(signatureKey.getSigningKeyBytes()!!)}~${DatUtils.encodeBase64Url(signatureKey.getVerifyingKeyBytes())}"
            DatSignatureKeyExportOption.SIGNING -> DatUtils.encodeBase64Url(signatureKey.getSigningKeyBytes()!!)
            DatSignatureKeyExportOption.VERIFYING -> "~${DatUtils.encodeBase64Url(signatureKey.getVerifyingKeyBytes())}"
        }
        val cryptKeyBase64 = DatUtils.encodeBase64Url(cryptoKey.toBytes())
        return "${cid.toString(16)}.${signatureKey.algorithm()}.${signatureKeyBase64}.${cryptoKey.algorithm()}.${cryptKeyBase64}.${datIssueBegin}.${datIssueEnd}.${datTtl}"
    }

    public override fun clone(): DatCertificate {
        return DatCertificate(
            cid,
            signatureKey.clone(),
            cryptoKey.clone(),
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
        fun generate(cid: Long, signatureAlgorithm: DatSignatureAlgorithm, cryptoAlgorithm: DatCryptoAlgorithm, datIssueBegin: Long, datIssueEnd: Long, datTtl: Long): DatCertificate {
            return DatCertificate(
                cid,
                DatSignatureKey.generate(signatureAlgorithm),
                DatCryptoKey.generate(cryptoAlgorithm),
                datIssueBegin,
                datIssueEnd,
                datTtl
            )
        }

        @JvmStatic
        fun new(cid: Long, signatureKey: DatSignatureKey, cryptoKey: DatCryptoKey, datIssueBegin: Long, datIssueEnd: Long, datTtl: Long): DatCertificate {
            return DatCertificate(
                cid,
                signatureKey,
                cryptoKey,
                datIssueBegin,
                datIssueEnd,
                datTtl,
            )
        }

        @JvmStatic
        fun parse(format: String): DatCertificate {
            try {
                val parts: List<String> = format.split(".")
                if (parts.size == 8) {
                    val cid = parts[0].toULong(16).toLong()
                    val signatureAlgorithm = DatSignatureAlgorithm.valueOf(parts[1])
                    val signatureKeyStr = parts[2].split('~')
                    val signatureKey = when (signatureKeyStr.size) {
                        2 -> DatSignatureKey.fromBytes(signatureAlgorithm, DatUtils.decodeBase64Url(signatureKeyStr[0]), DatUtils.decodeBase64Url(signatureKeyStr[1]))
                        1 -> DatSignatureKey.fromBytes(signatureAlgorithm, DatUtils.decodeBase64Url(signatureKeyStr[0]), ByteArray(0))
                        else -> throw DatException("Invalid Dat Signature Key Format")
                    }
                    val cryptAlgorithm = DatCryptoAlgorithm.valueOf(parts[3])
                    val cryptoKey = DatCryptoKey.fromBytes(cryptAlgorithm, DatUtils.decodeBase64Url(parts[4]))
                    return DatCertificate(
                        cid,
                        signatureKey,
                        cryptoKey,
                        parts[5].toULong().toLong(),
                        parts[6].toULong().toLong(),
                        parts[7].toULong().toLong()
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