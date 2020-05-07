package ru.raid.miptandroid

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText

suspend fun ApplicationCall.respondPlainText(text: String, status: HttpStatusCode = HttpStatusCode.OK) {
    respondText(text, ContentType.Text.Plain, status)
}

