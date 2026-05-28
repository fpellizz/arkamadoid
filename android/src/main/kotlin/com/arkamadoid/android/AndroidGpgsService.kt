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

    /**
     * Il parametro `leaderboardId` qui è l'ID locale logico (es. "arcade",
     * "endless", "daily"). Viene tradotto al vero ID generato dal Play Games
     * Console via resource string `gpgs_leaderboard_<id>`. Se la resource non
     * esiste o è vuota, skip silenzioso.
     */
    override fun submitScore(leaderboardId: String, score: Long) {
        if (!isSignedIn) return
        val gpgsId = resolveResource("gpgs_leaderboard_$leaderboardId") ?: return
        PlayGames.getLeaderboardsClient(activity).submitScore(gpgsId, score)
    }

    override fun unlockAchievement(achievementId: String) {
        if (!isSignedIn) return
        val gpgsId = resolveResource("gpgs_achievement_$achievementId") ?: return
        PlayGames.getAchievementsClient(activity).unlock(gpgsId)
    }

    override fun incrementAchievement(achievementId: String, steps: Int) {
        if (!isSignedIn) return
        val gpgsId = resolveResource("gpgs_achievement_$achievementId") ?: return
        PlayGames.getAchievementsClient(activity).increment(gpgsId, steps)
    }

    override fun showLeaderboard(leaderboardId: String) {
        if (!isSignedIn) return
        val gpgsId = resolveResource("gpgs_leaderboard_$leaderboardId") ?: return
        PlayGames.getLeaderboardsClient(activity).getLeaderboardIntent(gpgsId)
            .addOnSuccessListener { activity.startActivityForResult(it, RC_LEADERBOARD) }
    }

    override fun showAchievements() {
        if (!isSignedIn) return
        PlayGames.getAchievementsClient(activity).achievementsIntent
            .addOnSuccessListener { activity.startActivityForResult(it, RC_ACHIEVEMENTS) }
    }

    /** Risolve un nome di resource string in valore, oppure null se assente/vuoto. */
    private fun resolveResource(name: String): String? {
        val resId = activity.resources.getIdentifier(name, "string", activity.packageName)
        if (resId == 0) return null
        return activity.getString(resId).trim().takeIf { it.isNotEmpty() }
    }

    companion object {
        private const val RC_LEADERBOARD = 9001
        private const val RC_ACHIEVEMENTS = 9002
    }
}
