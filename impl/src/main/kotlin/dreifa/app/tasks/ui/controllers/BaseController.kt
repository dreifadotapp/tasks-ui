package dreifa.app.tasks.ui.controllers

import dreifa.app.opentelemetry.OpenTelemetryContext
import dreifa.app.opentelemetry.OpenTelemetryProvider
import dreifa.app.registry.Registry
import dreifa.app.tasks.ui.TemplateProcessor
import dreifa.app.types.UniqueId
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.path
import java.util.regex.Matcher
import java.util.regex.Pattern

abstract class BaseController(registry: Registry) {
    private val tracer = registry.getOrNull(Tracer::class.java)
    private val provider = registry.getOrNull(OpenTelemetryProvider::class.java)
    fun runWithTelemetry(
        trc: TelemetryRequestContext, block: ((tec: TelemetryExecutionContext) -> Response)
    ): Response {
        return if (provider != null && tracer != null) {
            val span = tracer.spanBuilder(trc.spanName).setSpanKind(SpanKind.SERVER).startSpan()
            span.setAttribute("http.method", trc.req.method.name)
            extractUrlParametersForTelemetry(trc.spanName, trc.req).forEach {
                span.setAttribute("http.param.${it.first}", it.second)
            }

            try {
                val telemetryContext = OpenTelemetryContext.fromSpan(span)
                val result = block.invoke(
                    TelemetryExecutionContext(telemetryContext),
                )  // what if the result is streaming ? are we closing the span too soon?
                completeSpan(span)
                result
            } catch (ex: Exception) {
                completeSpan(span, ex)
                throw ex
            }
        } else {
            block.invoke(TelemetryExecutionContext(OpenTelemetryContext.root()))
        }
    }


    protected fun buildBaseModel(@Suppress("UNUSED_PARAMETER") req: Request): MutableMap<String, Any> {
        return mutableMapOf("currentViewTask" to "view")
    }

    private fun extractUrlParametersForTelemetry(path: String, req: Request): List<Pair<String, String>> {
        val parts = ArrayList<String>()
        var capture = false
        var buffer = StringBuffer()
        path.chars().forEach {
            if (it == '}'.code && capture) {
                capture = false
                parts.add(buffer.toString())
                buffer = StringBuffer()
                capture = false
            }
            if (capture) {
                buffer.append(it.toChar())
            }
            if (it == '{'.code) {
                capture = true
            }
        }

        return parts.map { Pair(it, req.path(it) ?: "???") }
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
        return Response(Status.OK).header("content-type", "text/html").body(content)
    }

    fun json(content: String): Response {
        return Response(Status.OK).header("content-type", "application/json").body(content)
    }

    fun templateEngine(): TemplateProcessor = templateEngine

    private fun completeSpan(span: Span) {
        span.setStatus(StatusCode.OK)
        span.end()
    }

    private fun completeSpan(span: Span, ex: Throwable) {
        span.recordException(ex)
        span.setStatus(StatusCode.ERROR, ex.message!!)
        span.end()
    }

    companion object {
        private val templateEngine = TemplateProcessor()
    }

    /**
     * The data a controller must pass INTO the telemetry handler
     */
    data class TelemetryRequestContext(val req: Request, val spanName: String)

    /**
     * The data the telemetry handler passes back to the code block
     */
    data class TelemetryExecutionContext(val otc: OpenTelemetryContext)

}