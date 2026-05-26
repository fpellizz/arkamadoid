package com.arkamadoid.services

interface GpgsService {
    val isAvailable: Boolean
    val isSignedIn: Boolean

    fun signIn(onResult: (Boolean) -> Unit = {})
    fun signOut()

    fun submitScore(leaderboardId: String, score: Long)
    fun unlockAchievement(achievementId: String)
    fun incrementAchievement(achievementId: String, steps: Int)

    fun showLeaderboard(leaderboardId: String)
    fun showAchievements()
}

object NoopGpgsService : GpgsService {
    override val isAvailable = false
    override val isSignedIn = false
    override fun signIn(onResult: (Boolean) -> Unit) { onResult(false) }
    override fun signOut() {}
    override fun submitScore(leaderboardId: String, score: Long) {}
    override fun unlockAchievement(achievementId: String) {}
    override fun incrementAchievement(achievementId: String, steps: Int) {}
    override fun showLeaderboard(leaderboardId: String) {}
    override fun showAchievements() {}
}
