package com.arkamadoid.android

import android.app.Activity
import com.arkamadoid.services.GpgsService
import com.google.android.gms.games.PlayGames

class AndroidGpgsService(private val activity: Activity) : GpgsService {

    override val isAvailable: Boolean = true
    override var isSignedIn: Boolean = false
        private set

    override fun signIn(onResult: (Boolean) -> Unit) {
        PlayGames.getGamesSignInClient(activity).isAuthenticated
            .addOnCompleteListener { task ->
                isSignedIn = task.isSuccessful && task.result.isAuthenticated
                onResult(isSignedIn)
            }
    }

    override fun signOut() {
        isSignedIn = false
    }

    override fun submitScore(leaderboardId: String, score: Long) {
        if (!isSignedIn) return
        PlayGames.getLeaderboardsClient(activity).submitScore(leaderboardId, score)
    }

    override fun unlockAchievement(achievementId: String) {
        if (!isSignedIn) return
        PlayGames.getAchievementsClient(activity).unlock(achievementId)
    }

    override fun incrementAchievement(achievementId: String, steps: Int) {
        if (!isSignedIn) return
        PlayGames.getAchievementsClient(activity).increment(achievementId, steps)
    }

    override fun showLeaderboard(leaderboardId: String) {
        if (!isSignedIn) return
        PlayGames.getLeaderboardsClient(activity).getLeaderboardIntent(leaderboardId)
            .addOnSuccessListener { activity.startActivityForResult(it, RC_LEADERBOARD) }
    }

    override fun showAchievements() {
        if (!isSignedIn) return
        PlayGames.getAchievementsClient(activity).achievementsIntent
            .addOnSuccessListener { activity.startActivityForResult(it, RC_ACHIEVEMENTS) }
    }

    companion object {
        private const val RC_LEADERBOARD = 9001
        private const val RC_ACHIEVEMENTS = 9002
    }
}
