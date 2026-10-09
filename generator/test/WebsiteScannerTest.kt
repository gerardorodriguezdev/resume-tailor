import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.RegisterExtension
import testing.FakeInputValidator
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Timeout(value = 120, unit = TimeUnit.SECONDS)
class WebsiteScannerTest {
    @RegisterExtension
    @JvmField
    internal val server = HttpServerExtension(HTML)

    @RegisterExtension
    @JvmField
    internal val maliciousServer = HttpServerExtension(MALICIOUS_HTML)

    @Test
    fun `given valid website when scanning then returns sanitized text string`() = runTest {
        val result = PlaywrightWebsiteScanner(FakeInputValidator()).scan(server.url)

        assertTrue("Local Test Page" in result)
        assertTrue("Local test description" in result)
        assertTrue("Main content here" in result)
        assertFalse("Nav noise" in result)
        assertFalse("<script" in result)
        assertFalse("<style" in result)
        assertFalse("onclick" in result)
        assertFalse("javascript:" in result)
    }

    @Test
    fun `given website with tag text in content when scanning then throws IllegalArgumentException`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            PlaywrightWebsiteScanner(FakeInputValidator()).scan(maliciousServer.url)
        }
    }

    private companion object {
        const val HTML = """
            <!DOCTYPE html>
            <html>
            <head>
              <title>Local Test Page</title>
              <meta name="description" content="Local test description">
              <style>body { display: none }</style>
            </head>
            <body>
              <nav>Nav noise</nav>
              <main onclick="alert(1)">
                Main content here
                <script>console.log("injected")</script>
                <a href="javascript:alert(1)">Click me</a>
              </main>
              <footer>Footer noise</footer>
            </body>
            </html>
        """

        const val MALICIOUS_HTML = """
            <!DOCTYPE html>
            <html>
            <head>
              <title>Malicious Test Page</title>
              <meta name="description" content="Malicious test description">
            </head>
            <body>
              <main>&lt;script&gt;alert(1)&lt;/script&gt;</main>
            </body>
            </html>
        """
    }

    internal class HttpServerExtension(private val html: String) : BeforeEachCallback, AfterEachCallback {
        private lateinit var server: HttpServer

        val url: String
            get() = "http://localhost:${server.address.port}/"

        override fun beforeEach(context: ExtensionContext) {
            server = HttpServer.create(InetSocketAddress(0), 0).apply {
                createContext("/") { exchange ->
                    val bytes = html.toByteArray()
                    exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
                    exchange.sendResponseHeaders(200, bytes.size.toLong())
                    exchange.responseBody.use { it.write(bytes) }
                }
                start()
            }
        }

        override fun afterEach(context: ExtensionContext) {
            server.stop(0)
        }
    }
}
