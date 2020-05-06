package ru.raid.miptandroid

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText

suspend fun ApplicationCall.respondJson(json: String, status: HttpStatusCode = HttpStatusCode.OK) {
    respondText(json, ContentType.Application.Json, status)
}

suspend fun ApplicationCall.respondPlainText(text: String, status: HttpStatusCode = HttpStatusCode.OK) {
    respondText(text, ContentType.Text.Plain, status)
}

suspend fun ApplicationCall.respondJson(status: HttpStatusCode = HttpStatusCode.OK, provider: suspend () -> String) {
    respondText(ContentType.Application.Json, status, provider)
}
