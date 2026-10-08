import com.microsoft.playwright.*
import com.microsoft.playwright.options.ServiceWorkerPolicy

internal interface WebsiteScanner {
    suspend fun scan(url: String): String
}

internal class PlaywrightWebsiteScanner : WebsiteScanner {

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
        if (request.isNavigationRequest || isValidUrl(request.url())) {
            route.resume()
        } else {
            route.abort()
        }
    }

    private fun Page.extractSiteText(url: String): String {
        setDefaultTimeout(ACTION_TIMEOUT_MILLIS)
        setDefaultNavigationTimeout(NAVIGATION_TIMEOUT_MILLIS)

        navigate(url)

        waitForTimeout(RENDER_WAIT_MILLIS)

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
        const val RENDER_WAIT_MILLIS = 2500.0
        const val ACTION_TIMEOUT_MILLIS = 5000.0
        const val NAVIGATION_TIMEOUT_MILLIS = 15000.0
        const val DESCRIPTION_LOCATOR = "meta[name=description]"
        const val CONTENT_LOCATOR = "main, article"
        const val BODY_LOCATOR = "body"
    }
}
