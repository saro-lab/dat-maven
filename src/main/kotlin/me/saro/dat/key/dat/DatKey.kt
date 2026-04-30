package me.saro.dat.key.dat

import me.saro.dat.key.DatUtils
import me.saro.dat.key.crypto.CryptoAlgorithm
import me.saro.dat.key.crypto.CryptoKey
import me.saro.dat.key.exception.DatException
import me.saro.dat.key.signature.SignatureAlgorithm
import me.saro.dat.key.signature.SignatureKey
import me.saro.dat.key.signature.SignatureKeyOutOption
import java.io.ByteArrayOutputStream

class DatKey(
    val kid: String,
    val signatureAlgorithm: SignatureAlgorithm,
    val signatureKey: SignatureKey,
    val cryptoAlgorithm: CryptoAlgorithm,
    val cryptoKey: CryptoKey,
    val issueBegin: Long,
    val issueEnd: Long,
    val tokenTtl: Long,
): Cloneable {
    private val kidBytes: ByteArray = kid.toByteArray()

    init {
        if (DatUtils.hasEscapeDatChars(kid)) {
            throw DatException("kid contains escape characters")
        }
    }

    fun exports(signatureKeyOutOption: SignatureKeyOutOption): String {
        val signatureKeyBase64 = when (signatureKeyOutOption) {
            SignatureKeyOutOption.FULL -> "${DatUtils.encodeBase64UrlWp(signatureKey.getSigningKeyBytes()!!)}~${DatUtils.encodeBase64UrlWp(signatureKey.getVerifyingKeyBytes())}"
            SignatureKeyOutOption.SIGNING -> DatUtils.encodeBase64UrlWp(signatureKey.getSigningKeyBytes()!!)
            SignatureKeyOutOption.VERIFYING -> "~${DatUtils.encodeBase64UrlWp(signatureKey.getVerifyingKeyBytes())}"
        }
        val cryptKeyBase64 = DatUtils.encodeBase64UrlWp(cryptoKey.toBytes())
        return "${CONV_VERSION}.${kid}.${signatureAlgorithm}.${signatureKeyBase64}.${cryptoAlgorithm}.${cryptKeyBase64}.${issueBegin}.${issueEnd}.${tokenTtl}"
    }

    fun toDat(plain: ByteArray, secure: ByteArray): String {
        val bw: ByteArrayOutputStream = ByteArrayOutputStream()

        // expire
        val expire = ((System.currentTimeMillis() / 1000L) + tokenTtl).toString().toByteArray(Charsets.UTF_8)
        bw.write(expire)
        bw.write(DOT)

        // kid
        bw.write(kidBytes)
        bw.write(DOT)

        // plain
        bw.write(DatUtils.encodeBase64UrlWpBytes(plain))
        bw.write(DOT)

        // secure
        bw.write(DatUtils.encodeBase64UrlWpBytes(cryptoKey.encrypt(secure)))

        val sign: ByteArray = DatUtils.encodeBase64UrlWpBytes(this.signatureKey.sign(bw.toByteArray()))
        bw.write(DOT)
        bw.write(sign)

        return bw.toString()
    }

    fun toDat(plain: String, secure: String): String {
        return toDat(plain.toByteArray(Charsets.UTF_8), secure.toByteArray(Charsets.UTF_8))
    }

    internal fun toPayload(dat: String, parts: List<String>): Payload {
        val expire = parts[0].toLong()
        if (expire < (System.currentTimeMillis() / 1000)) {
            throw DatException("Expired")
        }
        if (!signatureKey.verify(dat.substring(0, dat.length - parts[4].length - 1).toByteArray(), DatUtils.decodeBase64UrlWp(parts[4]))) {
            throw DatException("InvalidSignature")
        }
        return toPayloadWithoutVerifying(parts)
    }

    fun toPayload(dat: String): Payload {
        return toPayload(dat, split(dat))
    }

    fun hasSigningKey(): Boolean {
        return signatureKey.hasSigningKey()
    }

    public override fun clone(): DatKey {
        return DatKey(
            kid,
            signatureAlgorithm,
            signatureKey.clone(),
            cryptoAlgorithm,
            cryptoKey.clone(),
            issueBegin,
            issueEnd,
            tokenTtl
        )
    }

    internal fun toPayloadWithoutVerifying(parts: List<String>): Payload {
        val plain = DatUtils.decodeBase64UrlWp(parts[2])
        val secure = cryptoKey.decrypt(DatUtils.decodeBase64UrlWp(parts[3]))
        return Payload(plain, secure)
    }

    override fun equals(other: Any?): Boolean {
        if (other is DatKey) {
            return other.kid == this.kid
        }
        return false
    }

    override fun hashCode(): Int {
        return kid.hashCode()
    }

    fun expired(): Boolean {
        return (System.currentTimeMillis() / 1000L) > (issueEnd + tokenTtl)
    }

    companion object {
        const val CONV_VERSION: String = "2"
        val DOT: ByteArray = ".".toByteArray(Charsets.UTF_8)

        internal fun split(dat: String): List<String> {
            val parts = dat.split('.')
            if (parts.size != 5) {
                throw DatException("InvalidDatFormat")
            }
            return parts
        }

        @JvmStatic
        fun generate(kid: String, signatureAlgorithm: SignatureAlgorithm, cryptoAlgorithm: CryptoAlgorithm, issueBegin: Long, issueEnd: Long, tokenTtl: Long): DatKey {
            return DatKey(
                kid,
                signatureAlgorithm,
                SignatureKey.generate(signatureAlgorithm),
                cryptoAlgorithm,
                CryptoKey.generate(cryptoAlgorithm),
                issueBegin,
                issueEnd,
                tokenTtl
            )
        }

        @JvmStatic
        fun parse(format: String): DatKey {
            val parts: List<String> = format.split(".")
            when (parts[0]) {
                "2", "1" -> {
                    val kid = parts[1]
                    val signAlg = SignatureAlgorithm.valueOf(parts[2])
                    val signatureKeyStr = parts[3].split('~')
                    val signatureKey = when (signatureKeyStr.size) {
                        2 -> SignatureKey.fromBytes(signAlg, DatUtils.decodeBase64UrlWp(signatureKeyStr[0]), DatUtils.decodeBase64UrlWp(signatureKeyStr[1]))
                        1 -> SignatureKey.fromBytes(signAlg, DatUtils.decodeBase64UrlWp(signatureKeyStr[0]), ByteArray(0))
                        else -> throw DatException("invalid sign key format")
                    }
                    val cryptAlg = CryptoAlgorithm.valueOf(parts[4])
                    val cryptoKey = CryptoKey.fromBytes(cryptAlg, DatUtils.decodeBase64UrlWp(parts[5]));
                    return DatKey(
                        kid,
                        signAlg,
                        signatureKey,
                        cryptAlg,
                        cryptoKey,
                        parts[6].toLong(),
                        parts[7].toLong(),
                        parts[8].toLong()
                    )
                }
            }
            throw DatException("InvalidDatKeyFormat")
        }
    }
}