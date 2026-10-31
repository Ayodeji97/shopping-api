package com.danzucker.shopping.common.web

import com.danzucker.shopping.common.core.DomainException
import org.slf4j.LoggerFactory
import org.springframework.beans.TypeMismatchException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.net.URI

/**
 * Turns every failure into one error shape: an RFC 9457 `application/problem+json` body with a
 * stable `code` that clients can switch on, and, when a request parameter is at fault, an `errors`
 * array naming it.
 *
 * ```json
 * { "title": "Bad Request", "status": 400,
 *   "detail": "price_min (50) must not be greater than price_max (10).",
 *   "instance": "/products", "code": "INVALID_PRICE_RANGE",
 *   "errors": [{ "field": "price_min", "message": "price_min (50) must not be greater than price_max (10)." }] }
 * ```
 *
 * Domain failures carry a [DomainException.Kind], which is mapped to a status here and only here.
 * Spring's own errors (unknown route, wrong method, and so on) are rebuilt into the same shape, so a
 * client never has to parse two formats.
 */
@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(DomainException::class)
    fun handleDomainException(
        ex: DomainException,
        request: WebRequest,
    ): ResponseEntity<Any> {
        val status =
            when (ex.kind) {
                DomainException.Kind.INVALID_INPUT -> HttpStatus.BAD_REQUEST
                DomainException.Kind.NOT_FOUND -> HttpStatus.NOT_FOUND
                DomainException.Kind.CONFLICT -> HttpStatus.CONFLICT
            }
        log.info("{} -> {} {}", ex.code, status.value(), ex.message)
        val errors = ex.field?.let { listOf(FieldViolation(it, ex.message)) }
        return respond(status, ex.code, ex.message, request, errors)
    }

    /** A query or path parameter that is not of the expected type, e.g. `/products/abc` or `price_min=cheap`. */
    override fun handleTypeMismatch(
        ex: TypeMismatchException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        val name = (ex as? MethodArgumentTypeMismatchException)?.name ?: ex.propertyName ?: "parameter"
        val expected =
            when (ex.requiredType?.kotlin) {
                Int::class, Long::class -> "a whole number"
                java.math.BigDecimal::class -> "a number, e.g. 19.99"
                else -> "a valid value"
            }
        val detail = "$name must be $expected; got '${ex.value}'."
        return respond(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", detail, request, listOf(FieldViolation(name, detail)))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(
        ex: Exception,
        request: WebRequest,
    ): ResponseEntity<Any> {
        log.error("Unhandled exception", ex)
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Something went wrong on our side. Please try again.", request)
    }

    /** Rebuilds Spring's own problem bodies (404 for unknown routes, 405, 415, ...) into our shape. */
    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        val response = super.handleExceptionInternal(ex, body, headers, statusCode, request)
        val springsOwn = (response?.body as? ProblemDetail)?.takeUnless { it.properties?.containsKey("code") == true }
        return if (response == null || springsOwn == null) response else rebuilt(springsOwn, statusCode, response.headers, request)
    }

    private fun rebuilt(
        springsOwn: ProblemDetail,
        statusCode: HttpStatusCode,
        headers: HttpHeaders,
        request: WebRequest,
    ): ResponseEntity<Any> {
        val status = HttpStatus.valueOf(statusCode.value())
        val code =
            when (status) {
                HttpStatus.NOT_FOUND -> "ROUTE_NOT_FOUND"
                HttpStatus.METHOD_NOT_ALLOWED -> "METHOD_NOT_ALLOWED"
                HttpStatus.UNSUPPORTED_MEDIA_TYPE, HttpStatus.NOT_ACCEPTABLE -> "UNSUPPORTED_MEDIA_TYPE"
                else -> if (status.is4xxClientError) "INVALID_REQUEST" else "INTERNAL_ERROR"
            }
        val detail =
            if (status == HttpStatus.NOT_FOUND) {
                "No endpoint matches this path. See the README or /swagger-ui.html for available endpoints."
            } else {
                springsOwn.detail ?: "The request could not be handled."
            }
        return ResponseEntity.status(status).headers(headers).body(problem(status, code, detail, request, null))
    }

    private fun respond(
        status: HttpStatus,
        code: String,
        detail: String,
        request: WebRequest,
        errors: List<FieldViolation>? = null,
    ): ResponseEntity<Any> = ResponseEntity.status(status).body(problem(status, code, detail, request, errors))

    private fun problem(
        status: HttpStatus,
        code: String,
        detail: String,
        request: WebRequest,
        errors: List<FieldViolation>?,
    ): ProblemDetail =
        ProblemDetail.forStatusAndDetail(status, detail).apply {
            instance = (request as? ServletWebRequest)?.request?.requestURI?.let(URI::create)
            setProperty("code", code)
            errors?.let { setProperty("errors", it) }
        }
}

/** One rejected request parameter. */
data class FieldViolation(
    val field: String,
    val message: String,
)
