package me.saro.dat.dat

import me.saro.dat.exception.DatException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock

class DatCmsManager private constructor(
    private val uri: String,
    private var token: String,
    private var version: Long,
    private val manager: DatManager,
    private val client: HttpClient,
    private val scheduler: ScheduledExecutorService?,
) {
    private val lock = ReentrantReadWriteLock()
    private val sync = Runnable { sync() }

    fun getManager() = manager

    fun issue(plain: ByteArray, secure: ByteArray): String = manager.issue(plain, secure)

    fun issue(plain: String, secure: String): String = manager.issue(plain, secure)

    fun parse(dat: Dat): Payload = manager.parse(dat)

    fun parse(dat: String): Payload = manager.parse(dat)

    fun parseWithoutVerifying(dat: Dat): Payload = manager.parseWithoutVerifying(dat)

    fun parseWithoutVerifying(dat: String): Payload = manager.parseWithoutVerifying(dat)

    fun sync() {
        if (!lock.writeLock().tryLock()) {
            log.warn("Last request ignored (Duplicate request)")
            return
        }
        val newUrl = "$uri?version=$version"
        try {
            val request: HttpRequest = HttpRequest.newBuilder()
                .uri(URI.create(newUrl))
                .header("Authorization", token)
                .build()
            val result = client
                .send(request, HttpResponse.BodyHandlers.ofString())
            if (result.statusCode() != 200) {
                throw DatException("response status error, status:${result.statusCode()} in $newUrl");
            }

            val body = result.body()
            val iof = body.indexOf("\n")
            if (iof == 0) {
                throw DatException("invalid response: $newUrl");
            } else if (iof > 0) {
                val newVersion = body.substring(0, iof).trim().toLong()
                val newCertificates = body.substring(iof + 1).trim()
                val renew = manager.imports(newCertificates, false)
                version = newVersion
                log.debug("renew $renew certificates: $newUrl")
            } else {
                log.debug("no new certificate: $newUrl")
            }
        } catch (e: Exception) {
            log.error("[Exception] DAT SMS Sync $newUrl: ", e)
        } finally {
            lock.writeLock().unlock()
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DatCmsManager::class.java)
        private const val DAT_CMS_API_VERSION = "v1"
        @JvmStatic
        fun builder(): DatCmsManagerBuilder = DatCmsManagerBuilder()
    }

    class DatCmsManagerBuilder private constructor(
        private var client: HttpClient = HttpClient.newBuilder().build(),
        private var https: Boolean = false,
        private var host: String = "localhost",
        private var port: Int = 8088,
        private var token: String = "",
        private var verifyOnly: Boolean = false,
        private var intervalSeconds: Long = 60L
    ) {
        constructor(): this(
            client = HttpClient.newBuilder().build()
        )

        fun https(https: Boolean) = this.apply { this.https = https; }
        fun host(host: String) = this.apply { this.host = host; }
        fun port(port: Int) = this.apply { this.port = port; }
        fun token(token: String) = this.apply { this.token = token; }
        fun verifyOnly(verifyOnly: Boolean) = this.apply { this.verifyOnly = verifyOnly; }
        fun intervalSeconds(intervalSeconds: Long) = this.apply { this.intervalSeconds = intervalSeconds; }

        fun build(): DatCmsManager {
            val proto = if (https) "https" else "http"
            val pathVerifyOnly = if (verifyOnly) "/verify-only" else ""
            val uri = "$proto://$host:$port/$DAT_CMS_API_VERSION/certs$pathVerifyOnly"
            val manager = DatManager.newInstance()





            val scheduler: ScheduledExecutorService? = if (intervalSeconds > 0) {
                Executors.newSingleThreadScheduledExecutor { runnable: Runnable ->
                    val thread = Thread(runnable)
                    thread.setDaemon(true)
                    thread.setName("dat-cms-sync-scheduler")
                    thread
                }
            } else {
                null
            }
            val cms = DatCmsManager(uri, token, 0, manager, client, scheduler)
            scheduler?.apply {
                scheduleAtFixedRate(cms.sync, intervalSeconds, intervalSeconds, TimeUnit.SECONDS)
            }
            cms.sync()
            return cms
        }
    }
}