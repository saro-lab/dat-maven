package me.saro.dat.key.dat

import me.saro.dat.key.DatUtils
import me.saro.dat.key.Unixtime
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
        if (!DatUtils.isKidFormat(kid)) {
            throw DatException("Invalid Kid Format [A-Z0-9_-]: but $kid")
        }
    }

    fun hasSigningKey(): Boolean {
        return signatureKey.hasSigningKey()
    }

    val keyExpire: Long get() = issueEnd + tokenTtl

    val expiredVerifying: Boolean get() = keyExpire < keyExpire

    val issuable: Boolean get() {
        val now = Unixtime.now()
        return this.hasSigningKey() && issueBegin <= now && issueEnd > now
    }

    fun exports(signatureKeyOutOption: SignatureKeyOutOption): String {
        val signatureKeyBase64 = when (signatureKeyOutOption) {
            SignatureKeyOutOption.FULL -> "${DatUtils.encodeBase64Url(signatureKey.getSigningKeyBytes()!!)}~${DatUtils.encodeBase64Url(signatureKey.getVerifyingKeyBytes())}"
            SignatureKeyOutOption.SIGNING -> DatUtils.encodeBase64Url(signatureKey.getSigningKeyBytes()!!)
            SignatureKeyOutOption.VERIFYING -> "~${DatUtils.encodeBase64Url(signatureKey.getVerifyingKeyBytes())}"
        }
        val cryptKeyBase64 = DatUtils.encodeBase64Url(cryptoKey.toBytes())
        return "${CONV_VERSION}.${kid}.${signatureAlgorithm}.${signatureKeyBase64}.${cryptoAlgorithm}.${cryptKeyBase64}.${issueBegin}.${issueEnd}.${tokenTtl}"
    }

    fun toDat(plain: ByteArray, secure: ByteArray): String {
        val bw: ByteArrayOutputStream = ByteArrayOutputStream()

        // expire
        val expire = (Unixtime.now() + tokenTtl).toString().toByteArray(Charsets.UTF_8)
        bw.write(expire)
        bw.write(DOT)

        // kid
        bw.write(kidBytes)
        bw.write(DOT)

        // plain
        bw.write(DatUtils.encodeBase64UrlBytes(plain))
        bw.write(DOT)

        // secure
        bw.write(DatUtils.encodeBase64UrlBytes(cryptoKey.encrypt(secure)))

        val sign: ByteArray = DatUtils.encodeBase64UrlBytes(this.signatureKey.sign(bw.toByteArray()))
        bw.write(DOT)
        bw.write(sign)

        return bw.toString()
    }

    fun toDat(plain: String, secure: String): String {
        return toDat(plain.toByteArray(Charsets.UTF_8), secure.toByteArray(Charsets.UTF_8))
    }

    fun toPayload(dat: Dat): Payload {
        if (!signatureKey.verify(dat.body, dat.signatureBytes)) {
            throw DatException("Invalid Dat Signature")
        }
        return toPayloadWithoutVerifying(dat)
    }

    fun toPayload(dat: String): Payload {
        return this.toPayload(Dat(dat))
    }

    fun toPayloadWithoutVerifying(dat: String): Payload {
        return this.toPayloadWithoutVerifying(Dat(dat))
    }

    fun toPayloadWithoutVerifying(dat: Dat): Payload {
        return Payload(dat.plainBytes, cryptoKey.decrypt(dat.secureBytes))
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

    override fun equals(other: Any?): Boolean {
        if (other is DatKey) {
            return other.kid == this.kid
        }
        return false
    }

    override fun hashCode(): Int {
        return kid.hashCode()
    }

    companion object {
        const val CONV_VERSION: String = "2"
        val DOT: ByteArray = ".".toByteArray()

        internal fun split(dat: String): List<String> {
            val parts = dat.split('.')
            if (parts.size != 5) {
                throw DatException("Invalid Dat Format")
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
        fun generate(kid: String, signatureAlgorithm: String, cryptoAlgorithm: String, issueBegin: Long, issueEnd: Long, tokenTtl: Long): DatKey {
            val sa = SignatureAlgorithm.valueOf(signatureAlgorithm)
            val ca = CryptoAlgorithm.valueOf(cryptoAlgorithm)
            return DatKey(
                kid,
                sa,
                SignatureKey.generate(sa),
                ca,
                CryptoKey.generate(ca),
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
                        2 -> SignatureKey.fromBytes(signAlg, DatUtils.decodeBase64Url(signatureKeyStr[0]), DatUtils.decodeBase64Url(signatureKeyStr[1]))
                        1 -> SignatureKey.fromBytes(signAlg, DatUtils.decodeBase64Url(signatureKeyStr[0]), ByteArray(0))
                        else -> throw DatException("Invalid Dat Signature Key Format")
                    }
                    val cryptAlg = CryptoAlgorithm.valueOf(parts[4])
                    val cryptoKey = CryptoKey.fromBytes(cryptAlg, DatUtils.decodeBase64Url(parts[5]));
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
            throw DatException("Invalid Dat Key Format")
        }
    }
}