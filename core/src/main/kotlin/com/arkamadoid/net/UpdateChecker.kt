package com.arkamadoid.net

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Net
import com.badlogic.gdx.utils.JsonReader

/**
 * Polling di GitHub Releases per side-loaded APK.
 * Quando l'app è installata da Play Store usa Play Core In-App Updates invece (non implementato qui).
 */
object UpdateChecker {

    data class UpdateInfo(
        val latestVersion: String,   // es. "0.2.0"
        val htmlUrl: String,         // pagina release
        val apkAssetUrl: String?,    // diretto al .apk se trovato fra gli asset
    )

    /**
     * Chiama GitHub /releases/latest in modo asincrono. Il callback riceve null se
     * non c'è update (versione corrente uguale o più nuova di latest), o se la chiamata
     * fallisce.
     */
    fun check(
        currentVersion: String,
        repoOwner: String,
        repoName: String,
        onResult: (UpdateInfo?) -> Unit,
    ) {
        val url = "https://api.github.com/repos/$repoOwner/$repoName/releases/latest"
        val req = Net.HttpRequest("GET").apply {
            this.url = url
            setHeader("Accept", "application/vnd.github+json")
            setHeader("User-Agent", "$repoName-updater")
            timeOut = 4000
        }
        Gdx.net.sendHttpRequest(req, object : Net.HttpResponseListener {
            override fun handleHttpResponse(response: Net.HttpResponse) {
                runCatching {
                    if (response.status.statusCode !in 200..299) {
                        post(null); return
                    }
                    val body = response.resultAsString
                    val root = JsonReader().parse(body)
                    val tag = root.getString("tag_name", "")
                    val htmlUrl = root.getString("html_url", "")
                    val assets = root.get("assets")
                    var apkUrl: String? = null
                    if (assets != null) {
                        for (a in assets) {
                            val name = a.getString("name", "")
                            if (name.endsWith(".apk")) {
                                apkUrl = a.getString("browser_download_url", "")
                                break
                            }
                        }
                    }
                    val latest = tag.removePrefix("v")
                    val info = if (isNewer(latest, currentVersion))
                        UpdateInfo(latest, htmlUrl, apkUrl) else null
                    post(info)
                }.onFailure { post(null) }
            }

            override fun failed(t: Throwable?) { post(null) }
            override fun cancelled() { post(null) }

            private fun post(info: UpdateInfo?) {
                Gdx.app.postRunnable { onResult(info) }
            }
        })
    }

    /** Confronto semver semplice. Tollera suffissi non numerici trattando come 0. */
    internal fun isNewer(latest: String, current: String): Boolean {
        val a = parts(latest)
        val b = parts(current)
        for (i in 0 until maxOf(a.size, b.size)) {
            val ai = a.getOrElse(i) { 0 }
            val bi = b.getOrElse(i) { 0 }
            if (ai != bi) return ai > bi
        }
        return false
    }

    private fun parts(v: String): List<Int> =
        v.split('.', '-', '+').mapNotNull { it.toIntOrNull() }
}
