package me.saro.dat.key.bank

import me.saro.dat.key.dat.DatKey
import me.saro.dat.key.dat.Payload
import me.saro.dat.key.exception.DatException
import me.saro.dat.key.signature.SignatureKeyOutOption
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.stream.Collectors
import kotlin.concurrent.read
import kotlin.concurrent.write

class DatBank(
    private var issueKey: DatKey? = null,
    private var verifyKeys: List<DatKey> = emptyList(),
) {
    constructor() : this(null, emptyList())

    private val lock = ReentrantReadWriteLock()

    fun toDat(plain: String, secure: String): String {
        if (issueKey != null) {
            return issueKey!!.toDat(plain, secure)
        } else {
            throw DatException("IssueKeyIsNull")
        }
    }

    fun toPayload(dat: String): Payload {
        val parts = DatKey.split(dat)
        return find(parts[1]).toPayload(dat, parts)
    }

    fun toPayloadWithoutVerify(dat: String): Payload {
        val parts = DatKey.split(dat)
        return find(parts[1]).toPayloadWithoutVerifying(parts)
    }

    internal fun find(kid: String): DatKey {
        return lock.read {
            verifyKeys.find { it.kid == kid }
        } ?: throw DatException("KidNotFound")
    }

    fun exportsKids(): List<String> {
        return lock.read {
            verifyKeys.map { it.kid }
        }
    }

    fun exportsDatKeys(): List<DatKey> {
        return lock.read {
            verifyKeys.map { it.clone() }
        }
    }

    fun exports(signatureKeyOutOption: SignatureKeyOutOption): String {
        return lock.read {
            verifyKeys.map { it.exports(signatureKeyOutOption) }
        }.joinToString("\n")
    }

    fun imports(format: String, clear: Boolean) {
        val list = if (format.isNotBlank()) {
            format.lineSequence()
                .filter { it.isNotBlank() }
                .map { DatKey.parse(it) }
                .toList()
        } else {
            listOf()
        }
        imports(list, clear)
    }

    fun imports(newKeys: List<DatKey>, clear: Boolean) {
        var list = if (clear) {
            mutableListOf<DatKey>()
        } else {
            exportsDatKeys().toMutableList()
        }

        for (key in newKeys) {
            if (!list.contains(key)) {
                list.add(key)
            }
        }

        list = list.stream()
            .filter { !it.expired() }
            .sorted(Comparator.comparingLong { it.issueBegin })
            .collect(Collectors.toList())

        val now = System.currentTimeMillis() / 1000L

        val issueKey: DatKey? = list.findLast {
            it.hasSigningKey() && it.issueBegin <= now && it.issueEnd > now
        }?.clone()

        lock.write {
            this.verifyKeys = list
            this.issueKey = issueKey
        }
    }
}
