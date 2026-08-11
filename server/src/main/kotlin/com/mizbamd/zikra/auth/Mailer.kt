package com.mizbamd.zikra.auth

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class MailerException(message: String) : RuntimeException(message)

interface Mailer {
    val configured: Boolean
    fun sendOtp(to: String, code: String)
}

/** Local/dev only. Never constructed for production without a real mailer. */
class LogMailer : Mailer {
    private val log = LoggerFactory.getLogger(LogMailer::class.java)
    override val configured: Boolean = false

    override fun sendOtp(to: String, code: String) {
        log.debug("Local OTP for {} is {} (email not configured; debug/local only)", to, code)
    }
}

class ResendMailer(
    private val apiKey: String,
    private val from: String,
    private val http: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build(),
) : Mailer {
    private val log = LoggerFactory.getLogger(ResendMailer::class.java)
    private val json = Json { encodeDefaults = true }
    override val configured: Boolean = true

    override fun sendOtp(to: String, code: String) {
        val payload = json.encodeToString(
            ResendEmail(
                from = from,
                to = listOf(to),
                subject = "Your Zikra sign-in code",
                text = "Your Zikra sign-in code is $code.\n\n" +
                    "It expires in 10 minutes. If you did not request this, you can ignore this email.",
            ),
        )
        val request = HttpRequest.newBuilder()
            .uri(URI.create(RESEND_URL))
            .timeout(Duration.ofSeconds(15))
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build()
        val response = try {
            http.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (t: Throwable) {
            log.warn("Resend request failed: {}", t.javaClass.simpleName)
            throw MailerException("email send failed")
        }
        if (response.statusCode() !in 200..299) {
            log.warn("Resend email failed: HTTP {}", response.statusCode())
            throw MailerException("email send failed")
        }
    }

    @Serializable
    private data class ResendEmail(
        val from: String,
        val to: List<String>,
        val subject: String,
        val text: String,
    )

    companion object {
        const val RESEND_URL = "https://api.resend.com/emails"
    }
}
