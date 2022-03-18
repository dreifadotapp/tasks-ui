package dreifa.app.tasks.ui.controllers

import org.http4k.core.Request

abstract class BaseController {
    protected fun buildBaseModel(req: Request): MutableMap<String, Any> {
        return mutableMapOf("foo" to "foo")
    }
}