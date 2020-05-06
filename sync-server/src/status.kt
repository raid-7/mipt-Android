package raid.neuroide.reproto.service

import io.ktor.application.ApplicationCall
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.ApplicationRequest
import io.ktor.request.contentType
import io.ktor.util.pipeline.PipelineContext

open class StatusException(val status: HttpStatusCode, override val message: String) : RuntimeException()

class BadRequestException(message: String) : StatusException(HttpStatusCode.BadRequest, message)
class NotFoundException(message: String) : StatusException(HttpStatusCode.NotFound, message)
class UnsupportedMediaTypeException(message: String) : StatusException(HttpStatusCode.UnsupportedMediaType, message)

fun StatusPages.Configuration.catchStatus(
    handler: suspend PipelineContext<Unit, ApplicationCall>.(StatusException) -> Unit
) {
    exception(handler)
}

fun ApplicationRequest.acceptOnly(
    acceptable: ContentType,
    message: String = HttpStatusCode.UnsupportedMediaType.description
) {
    if (contentType().withoutParameters() != acceptable.withoutParameters())
        throw UnsupportedMediaTypeException(message)
}
