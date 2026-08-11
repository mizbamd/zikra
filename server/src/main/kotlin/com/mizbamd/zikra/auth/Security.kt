package com.mizbamd.zikra.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import java.time.Instant
import java.util.Date
import java.util.UUID

class Security(secret: String) {
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier = JWT.require(algorithm).withIssuer(ISSUER).build()

    fun hashPassword(plain: String): String =
        BCrypt.withDefaults().hashToString(12, plain.toCharArray())

    fun verifyPassword(plain: String, hash: String): Boolean =
        BCrypt.verifyer().verify(plain.toCharArray(), hash).verified

    fun issueToken(userId: UUID, email: String): String {
        val now = Instant.now()
        return JWT.create()
            .withIssuer(ISSUER)
            .withSubject(userId.toString())
            .withClaim("email", email)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusSeconds(TOKEN_TTL_SECONDS)))
            .sign(algorithm)
    }

    fun decode(token: String): DecodedJWT = verifier.verify(token)

    companion object {
        const val ISSUER = "zikra"
        const val TOKEN_TTL_SECONDS = 60L * 60 * 24 * 14
    }
}
