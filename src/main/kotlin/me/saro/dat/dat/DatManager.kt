package me.saro.dat.dat

import me.saro.dat.DatUtils
import me.saro.dat.Unixtime
import me.saro.dat.exception.DatException
import me.saro.dat.exception.DatResult
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

    fun issue(plain: ByteArray, secure: ByteArray): DatResult<String> {
        return DatResult.parse(runCatching {
            lock.read {
                if (issuer != null) {
                    issue(issuer!!, plain, secure).toResult()
                } else {
                    Result.failure(DatException("Not Found IssuanceKey(SigningKey)"))
                }
            }
        }.getOrElse { Result.failure(it) })
    }

    fun issue(plain: String, secure: String): DatResult<String> {
        return issue(plain.toByteArray(Charsets.UTF_8), secure.toByteArray(Charsets.UTF_8))
    }

    fun parse(dat: Dat): DatResult<Payload> {
        return DatResult.parse(runCatching {
            lock.read {
                findUnsafeThread(dat.cid).fold(
                    onSuccess = { certificate -> parse(certificate, dat).toResult() },
                    onFailure = { exception -> Result.failure(exception) }
                )
            }
        }.getOrElse { Result.failure(it) })
    }

    fun parse(dat: String): DatResult<Payload> {
        return Dat.parse(dat).fold(
            onSuccess = { parsedDat -> parse(parsedDat) },
        )
    }

    fun parseWithoutVerifying(dat: Dat): DatResult<Payload> {
        return DatResult.parse(runCatching {
            lock.read {
                findUnsafeThread(dat.cid).fold(
                    onSuccess = { certificate -> parseWithoutVerifying(certificate, dat).toResult() },
                    onFailure = { exception -> Result.failure(exception) }
                )
            }
        }.getOrElse { Result.failure(it) })
    }

    fun parseWithoutVerifying(dat: String): DatResult<Payload> {
        return Dat.parse(dat).fold(
            onSuccess = { parsedDat -> parseWithoutVerifying(parsedDat) },
        )
    }

    internal fun findUnsafeThread(cid: ULong): Result<DatCertificate> {
        return certificates.find { it.cid == cid }
            ?.run { Result.success(this) }
            ?: Result.failure(DatException("Not Found CID(Certificate ID): $cid"))
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
        fun issue(certificate: DatCertificate, plain: ByteArray, secure: ByteArray): DatResult<String> {
            return DatResult.parse(runCatching {
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

                bw.toString()
            })
        }

        @JvmStatic
        fun issue(certificate: DatCertificate, plain: String, secure: String): DatResult<String> {
            return issue(certificate, plain.toByteArray(Charsets.UTF_8), secure.toByteArray(Charsets.UTF_8))
        }

        @JvmStatic
        fun parse(certificate: DatCertificate, dat: Dat): DatResult<Payload> {
            if (!certificate.signature.verify(dat.body, dat.signatureBytes)) {
                return DatResult.failure(DatException("Invalid Dat Signature"))
            }
            return parseWithoutVerifying(certificate, dat)
        }

        @JvmStatic
        fun parse(certificate: DatCertificate, dat: String): DatResult<Payload> {
            return Dat.parse(dat).fold(
                onSuccess = { parsedDat -> parse(certificate, parsedDat) },
            )
        }

        @JvmStatic
        fun parseWithoutVerifying(certificate: DatCertificate, dat: Dat): DatResult<Payload> {
            return DatResult.parse(runCatching {
                Payload(dat.plainBytes, certificate.crypto.decrypt(dat.secureBytes))
            })
        }

        @JvmStatic
        fun parseWithoutVerifying(certificate: DatCertificate, dat: String): DatResult<Payload> {
            return Dat.parse(dat).fold(
                onSuccess = { parsedDat -> parseWithoutVerifying(certificate, parsedDat) },
            )
        }
    }
}