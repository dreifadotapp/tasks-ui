package dreifa.app.tasks.ui.controllers.ses

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dreifa.app.registry.Registry
import dreifa.app.ses.EventStore
import dreifa.app.ses.EverythingQuery
import dreifa.app.tasks.ui.controllers.BaseController
import org.http4k.core.Request
import org.http4k.core.Response

class AllEventsController(registry: Registry) : BaseController() {
    private val ses = registry.get(EventStore::class.java)

    override fun handle(req: Request): Response {
        val events = ses.read(EverythingQuery)

        val module = KotlinModule()
        val mapper = ObjectMapper()
        mapper.registerModule(module)
        return json(mapper.writeValueAsString(events))
    }
}