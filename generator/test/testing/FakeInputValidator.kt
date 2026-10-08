package testing

import InputValidator

internal class FakeInputValidator : InputValidator {
    var isValidApiKeyResult: Boolean = true
    var isValidFileNameResult: Boolean = true
    var isValidPathResult: Boolean = true
    var isValidUrlResult: Boolean = true

    val validatedApiKeys: List<String>
        field = MutableList(0) { "" }
    val validatedFileNames: List<String>
        field = MutableList(0) { "" }
    val validatedPaths: List<String>
        field = MutableList(0) { "" }
    val validatedUrls: List<String>
        field = MutableList(0) { "" }

    override fun isValidApiKey(apiKey: String): Boolean {
        validatedApiKeys.add(apiKey)
        return isValidApiKeyResult
    }

    override fun isValidFileName(fileName: String): Boolean {
        validatedFileNames.add(fileName)
        return isValidFileNameResult
    }

    override fun isValidPath(path: String): Boolean {
        validatedPaths.add(path)
        return isValidPathResult
    }

    override fun isValidUrl(url: String): Boolean {
        validatedUrls.add(url)
        return isValidUrlResult
    }
}
