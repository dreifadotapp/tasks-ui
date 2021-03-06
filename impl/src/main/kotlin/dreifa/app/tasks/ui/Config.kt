package dreifa.app.tasks.ui

class Config {
    private val prefix = "dreifa.app.tasks.ui"
    private val env: Map<String, String> = System.getenv() as Map<String, String>

    fun jaegerEndpoint(): String {
        val key = "$prefix.jaegerEndpoint".replace('.','_')
        val result =  env.getOrDefault(key, "http://localhost:14250")
        println ("jaegerEndpoint is `$result`")
        return result
    }
}