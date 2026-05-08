package me.saro.dat.key.bank

import me.saro.dat.key.dat.Dat
import me.saro.dat.key.dat.DatKey
import me.saro.dat.key.dat.Payload
import me.saro.dat.key.exception.DatException
import me.saro.dat.key.signature.SignatureKeyOutOption
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.stream.Collectors
import kotlin.concurrent.read
import kotlin.concurrent.write

class DatBank(
    private var issuanceKey: DatKey? = null,
    private var verifyingKeys: List<DatKey> = emptyList(),
) {
    constructor() : this(null, emptyList())

    private val lock = ReentrantReadWriteLock()

    fun toDat(plain: String, secure: String): String {
        if (issuanceKey != null) {
            return issuanceKey!!.toDat(plain, secure)
        } else {
            throw DatException("Not Found IssuanceKey(SigningKey)")
        }
    }

    fun toPayload(dat: Dat): Payload {
        return find(dat.kid).toPayload(dat)
    }

    fun toPayload(dat: String): Payload {
        return this.toPayload(Dat(dat))
    }

    fun toPayloadWithoutVerify(dat: Dat): Payload {
        return find(dat.kid).toPayloadWithoutVerifying(dat)
    }

    fun toPayloadWithoutVerify(dat: String): Payload {
        return this.toPayloadWithoutVerify(Dat(dat))
    }


    fun find(kid: String): DatKey {
        return lock.read { verifyingKeys.find { it.kid == kid } } ?: throw DatException("Not Found Kid: $kid")
    }

    fun exportsKids(): List<String> {
        return lock.read { verifyingKeys.map { it.kid } }
    }

    fun exportsDatKeys(): List<DatKey> {
        return lock.read {
            verifyingKeys.map { it.clone() }
        }
    }

    fun exports(signatureKeyOutOption: SignatureKeyOutOption): String {
        return lock.read {
            verifyingKeys.map { it.exports(signatureKeyOutOption) }
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
            .filter { !it.expiredVerifying }
            .sorted(Comparator.comparingLong { it.issueBegin })
            .collect(Collectors.toList())

        val issueKey: DatKey? = list.findLast { it.issuable }?.clone()

        lock.write {
            this.verifyingKeys = list
            this.issuanceKey = issueKey
        }
    }
}
