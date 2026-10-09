import com.microsoft.playwright.*
import com.microsoft.playwright.options.ServiceWorkerPolicy
import kotlin.time.Duration.Companion.seconds

internal interface WebsiteScanner {
    suspend fun scan(url: String): String
}

internal class PlaywrightWebsiteScanner(
    private val inputValidator: InputValidator,
) : WebsiteScanner {

    override suspend fun scan(url: String): String =
        Playwright.create().use { playwright ->
            playwright.chromium().launch().use { browser ->
                browser
                    .newContext(
                        Browser.NewContextOptions()
                            .setAcceptDownloads(false)
                            .setServiceWorkers(ServiceWorkerPolicy.BLOCK)
                    )
                    .use { context ->
                        context.route("**") { route -> validateRoute(route) }

                        context.newPage().use { page ->
                            page.extractSiteText(url)
                        }
                    }
            }
        }

    private fun validateRoute(route: Route) {
        val request = route.request()
        if (request.isNavigationRequest || inputValidator.isValidUrl(request.url())) {
            route.resume()
        } else {
            route.abort()
        }
    }

    private fun Page.extractSiteText(url: String): String {
        setDefaultTimeout(ACTION_TIMEOUT.inWholeMilliseconds.toDouble())
        setDefaultNavigationTimeout(NAVIGATION_TIMEOUT.inWholeMilliseconds.toDouble())

        navigate(url)

        waitForTimeout(RENDER_WAIT.inWholeMilliseconds.toDouble())

        val text = listOfNotNull(
            pageTitle(),
            pageDescription(),
            pageContent()
        ).joinToString("\n")

        validateContent(text)

        return text
    }

    private fun Page.pageTitle(): String = title()

    private fun Page.pageDescription(): String? = descriptionLocator.getAttribute("content")

    private fun Page.pageContent(): String =
        try {
            contentLocator.first().innerText()
        } catch (_: Exception) {
            bodyLocator.innerText()
        }

    private val Page.descriptionLocator: Locator get() = locator(DESCRIPTION_LOCATOR)

    private val Page.contentLocator: Locator get() = locator(CONTENT_LOCATOR)

    private val Page.bodyLocator: Locator get() = locator(BODY_LOCATOR)

    private companion object {
        val RENDER_WAIT = 2.5.seconds
        val ACTION_TIMEOUT = 5.seconds
        val NAVIGATION_TIMEOUT = 15.seconds
        const val DESCRIPTION_LOCATOR = "meta[name=description]"
        const val CONTENT_LOCATOR = "main, article"
        const val BODY_LOCATOR = "body"
    }
}
