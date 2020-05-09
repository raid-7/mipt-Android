package ru.raid.miptandroid

import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
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
import org.slf4j.Logger

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val serviceConfig = environment.config.config("ktor.application.service")
    val service = NoteSharingService(serviceConfig)

    install(ContentNegotiation) {
        gson {}
    }

    install(StatusPages) {
        catchStatus {
            call.respondPlainText(it.message, it.status)
        }
    }

    routing {
        trace {
            application.log.trace(it.buildText())
        }

        route("/shared-notes") {
            post {
                val multipart = call.receiveMultipart()
                var data: NoteData? = null
                var bytes: ByteArray? = null
                multipart.forEachPart { part ->
                    try {
                        if (part.name == "data" && part is PartData.FormItem) {
                            data = Gson().fromJson(part.value, NoteData::class.java)
                        } else if (part.name == "image" && part is PartData.FileItem) {
                            bytes = part.streamProvider().readAllBytes()
                        }
                    } finally {
                        part.dispose()
                    }
                }

                if (data == null || bytes == null) {
                    throw BadRequestException("Note must contain data and image")
                }

                val id = service.addSharedNote(data!!, bytes!!)
                call.respondPlainText(id, HttpStatusCode.Created)
            }
            get("/{id}/data") {
                val id = call.parameters["id"] ?: throw BadRequestException("Specify prototype id")
                val data = service.getNoteData(id)
                call.respond(HttpStatusCode.OK, data)
            }
            get("/{id}/image") {
                val id = call.parameters["id"] ?: throw BadRequestException("Specify prototype id")
                val image = service.getNoteImage(id)
                call.respondBytes(image, ContentType.Image.PNG, HttpStatusCode.OK)
            }
        }
    }
}
