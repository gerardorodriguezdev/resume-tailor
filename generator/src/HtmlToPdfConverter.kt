import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.Margin
import com.microsoft.playwright.options.ServiceWorkerPolicy
import com.microsoft.playwright.options.WaitUntilState

internal interface HtmlToPdfConverter {
    suspend fun convert(htmlContent: String): ByteArray
}

internal class PlaywrightHtmlToPdfConverter : HtmlToPdfConverter {

    override suspend fun convert(htmlContent: String): ByteArray =
        Playwright.create().use { playwright ->
            playwright.chromium().launch().use { browser ->
                browser.newContext(
                    Browser.NewContextOptions()
                        .setJavaScriptEnabled(false)
                        .setAcceptDownloads(false)
                        .setServiceWorkers(ServiceWorkerPolicy.BLOCK)
                ).use { context ->
                    context.route("**") { route -> route.abort() }

                    context.newPage().use { page ->
                        page.setContent(
                            htmlContent,
                            Page.SetContentOptions().setWaitUntil(WaitUntilState.NETWORKIDLE),
                        )
                        page.pdf(
                            Page.PdfOptions()
                                .setFormat("A4")
                                .setPrintBackground(true)
                                .setMargin(
                                    Margin()
                                        .setTop(TOP_BOTTOM_MARGIN)
                                        .setBottom(TOP_BOTTOM_MARGIN),
                                ),
                        )
                    }
                }
            }
        }

    private companion object {
        const val TOP_BOTTOM_MARGIN = "12mm"
    }
}