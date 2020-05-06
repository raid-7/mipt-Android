package ru.raid.miptandroid

import io.ktor.application.*
import io.ktor.features.StatusPages
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.util.hex
import io.ktor.utils.io.core.readBytes

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val serviceConfig = environment.config.config("ktor.application.service")
    val service = NoteSharingService(serviceConfig)

    install(StatusPages) {
        catchStatus {
            call.respondPlainText(it.message, it.status)
        }
    }

    routing {
        route("/shared-notes") {
            post {
                val multipart = call.receiveMultipart()
                val out = arrayListOf<String>()
                multipart.forEachPart {part ->
                    out += when (part) {
                        is PartData.FormItem -> {
                            "FormItem(${part.name},${part.value})"
                        }
                        is PartData.FileItem -> {
                            val bytes = part.streamProvider().readBytes()
                            "FileItem(${part.name},${part.originalFileName},${hex(bytes)})"
                        }
                        is PartData.BinaryItem -> {
                            "BinaryItem(${part.name},${hex(part.provider().readBytes())})"
                        }
                    }

                    part.dispose()
                }

                call.respondText(out.joinToString("; "))
            }
            get("/{id}") {
                // TODO
            }
        }
    }
}
