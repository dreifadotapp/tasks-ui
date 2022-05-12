package dreifa.app.tasks.ui.controllers

import dreifa.app.opentelemetry.OpenTelemetryContext
import dreifa.app.opentelemetry.OpenTelemetryProvider
import dreifa.app.registry.Registry
import dreifa.app.tasks.ui.TemplateProcessor
import dreifa.app.types.UniqueId
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

abstract class BaseController(registry: Registry) {
    private val tracer = registry.getOrNull(Tracer::class.java)
    private val provider = registry.getOrNull(OpenTelemetryProvider::class.java)
    fun runWithTelemetry(
        spanName: String,
        block: ((telemetryContext: OpenTelemetryContext) -> Response)
    ): Response {
        return if (provider != null && tracer != null) {
            val span = tracer.spanBuilder(spanName).startSpan()
            try {
                val telemetryContext = OpenTelemetryContext.fromSpan(span)
                val result = block.invoke(
                    telemetryContext
                )  // what if the result is streaming ? are we closing the span too soon?
                completeSpan(span)
                result
            } catch (ex: Exception) {
                completeSpan(span, ex)
                throw ex
            }
        } else {
            block.invoke(OpenTelemetryContext.root())
        }
    }


    protected fun buildBaseModel(@Suppress("UNUSED_PARAMETER") req: Request): MutableMap<String, Any> {
        return mutableMapOf("currentViewTask" to "view")
    }

    fun setMenuFlags(model: MutableMap<String, Any>, vararg flags: String) {
        flags.asList().forEach {
            model["menuFlag_$it"] = true
        }
    }

    fun setActiveTask(model: MutableMap<String, Any>, providerId: String, taskClazz: String) {
        model["currentViewTask"] = "/$providerId/$taskClazz/view"
    }

    fun setActiveTask(model: MutableMap<String, Any>, providerId: UniqueId, taskClazz: String) {
        setActiveTask(model, providerId.toString(), taskClazz)
    }

    abstract fun handle(req: Request): Response


    fun html(content: String): Response {
        return Response(Status.OK)
            .header("content-type", "text/html")
            .body(content)
    }

    fun json(content: String): Response {
        return Response(Status.OK)
            .header("content-type", "application/json")
            .body(content)
    }

    fun templateEngine(): TemplateProcessor = templateEngine

    private fun completeSpan(span: Span) {
        span.setStatus(StatusCode.OK)
        span.end()
    }

    private fun completeSpan(span: Span, ex: Throwable) {
        span.recordException(ex)
        span.setStatus(StatusCode.ERROR, ex.message)
        span.end()
    }

    companion object {
        private val templateEngine = TemplateProcessor()
    }
}