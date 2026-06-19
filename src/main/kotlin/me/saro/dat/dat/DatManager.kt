package me.saro.dat.dat

import me.saro.dat.DatUtils
import me.saro.dat.Unixtime
import me.saro.dat.exception.DatException
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.stream.Collectors
import kotlin.concurrent.read
import kotlin.concurrent.write

class DatManager private constructor(
    private var issuer: DatCertificate? = null,
    private var certificates: List<DatCertificate> = emptyList(),
) {
    private val lock = ReentrantReadWriteLock()

    fun issue(plain: ByteArray, secure: ByteArray): String {
        lock.read {
            if (issuer != null) {
                return issue(issuer!!, plain, secure)
            }
        }
        throw DatException("Not Found IssuanceKey(SigningKey)")
    }

    fun issue(plain: String, secure: String): String {
        return issue(plain.toByteArray(Charsets.UTF_8), secure.toByteArray(Charsets.UTF_8))
    }

    fun parse(dat: Dat): Payload {
        lock.read {
            return parse(findUnsafe(dat.cid), dat)
        }
    }

    fun parse(dat: String): Payload {
        return parse(Dat(dat))
    }

    fun parseWithoutVerifying(dat: Dat): Payload {
        lock.read {
            return parseWithoutVerifying(findUnsafe(dat.cid), dat)
        }
    }

    fun parseWithoutVerifying(dat: String): Payload {
        return parseWithoutVerifying(Dat(dat))
    }

    internal fun findUnsafe(cid: ULong): DatCertificate {
        return certificates.find { it.cid == cid } ?: throw DatException("Not Found CID(Certificate ID): $cid")
    }

    fun exportsIds(): List<Long> {
        return lock.read { certificates.map { it.cid.toLong() } }
    }

    fun exportsCertificates(): List<DatCertificate> {
        return lock.read {
            certificates.map { it.clone() }
        }
    }

    fun exports(verifyOnly: Boolean): String {
        return lock.read {
            certificates.joinToString("\n") { it.exports(verifyOnly) }
        }
    }

    fun imports(format: String, clear: Boolean): Int {
        val list = if (format.isNotBlank()) {
            format.lineSequence()
                .filter { it.isNotBlank() }
                .map { DatCertificate.parse(it) }
                .toList()
        } else {
            listOf()
        }
        return imports(list, clear)
    }

    fun imports(certificates: List<DatCertificate>, clear: Boolean): Int {
        if (certificates.size != certificates.distinctBy { it.cid }.size) {
            log.error("Duplicate CID(Certificate ID)")
            throw IllegalArgumentException("Duplicate CID(Certificate ID)")
        }

        var renew: Int = 0
        val list = if (clear) {
            certificates.stream()
        } else {
            val inList = exportsCertificates().toMutableList()
            for (certificate in certificates) {
                if (!inList.contains(certificate)) {
                    renew++
                    inList.add(certificate)
                }
            }
            inList.stream()
        }.filter { !it.expired }
            .sorted(Comparator.comparing { it.datIssuanceEndSeconds })
            .collect(Collectors.toList())

        val issuer: DatCertificate? = list.findLast { it.issuable }?.clone()

        lock.write {
            this.certificates = list
            this.issuer = issuer
        }
        return renew
    }

    companion object {
        private const val DOT = '.'.code
        private val log = LoggerFactory.getLogger(DatManager::class.java)

        @JvmStatic
        fun newInstance(): DatManager {
            return DatManager()
        }

        @JvmStatic
        internal fun newInstance(certificates: List<DatCertificate>): DatManager {
            return newInstance().apply { imports(certificates, true) }
        }

        @JvmStatic
        fun issue(certificate: DatCertificate, plain: ByteArray, secure: ByteArray): String {
            val bw = ByteArrayOutputStream(((plain.size * 1.5).toInt() + (secure.size * 2)) + 300)

            // expire
            val expire = (Unixtime.now().toULong() + certificate.datTtlSeconds).toString().toByteArray()
            bw.write(expire)
            bw.write(DOT)

            // kid
            bw.write(certificate.cidHexBytes)
            bw.write(DOT)

            // plain
            bw.write(DatUtils.encodeBase64UrlBytes(plain))
            bw.write(DOT)

            // secure
            bw.write(DatUtils.encodeBase64UrlBytes(certificate.crypto.encrypt(secure)))

            val sign: ByteArray = DatUtils.encodeBase64UrlBytes(certificate.signature.sign(bw.toByteArray()))
            bw.write(DOT)
            bw.write(sign)

            return bw.toString()
        }

        @JvmStatic
        fun issue(certificate: DatCertificate, plain: String, secure: String): String {
            return issue(certificate, plain.toByteArray(Charsets.UTF_8), secure.toByteArray(Charsets.UTF_8))
        }

        @JvmStatic
        fun parse(certificate: DatCertificate, dat: Dat): Payload {
            if (!certificate.signature.verify(dat.body, dat.signatureBytes)) {
                throw DatException("Invalid Dat Signature")
            }
            return parseWithoutVerifying(certificate, dat)
        }

        @JvmStatic
        fun parse(certificate: DatCertificate, dat: String): Payload {
            return parse(certificate, Dat(dat))
        }

        @JvmStatic
        fun parseWithoutVerifying(certificate: DatCertificate, dat: Dat): Payload {
            return Payload(dat.plainBytes, certificate.crypto.decrypt(dat.secureBytes))
        }


        @JvmStatic
        fun parseWithoutVerifying(certificate: DatCertificate, dat: String): Payload {
            return parseWithoutVerifying(certificate, Dat(dat))
        }
    }
}