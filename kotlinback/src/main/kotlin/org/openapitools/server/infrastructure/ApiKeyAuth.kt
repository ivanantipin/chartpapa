package org.openapitools.server.infrastructure

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.Authentication
import io.ktor.auth.AuthenticationFailedCause
import io.ktor.auth.AuthenticationPipeline
import io.ktor.auth.AuthenticationProvider
import io.ktor.auth.Credential
import io.ktor.auth.Principal
import io.ktor.auth.UnauthorizedResponse
import io.ktor.http.auth.HeaderValueEncoding
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.request.ApplicationRequest
import io.ktor.response.respond

enum class ApiKeyLocation(val location: String) {
    QUERY("query"),
    HEADER("header")
}
data class ApiKeyCredential(val value: String): Credential
data class ApiPrincipal(val apiKeyCredential: ApiKeyCredential?) : Principal





