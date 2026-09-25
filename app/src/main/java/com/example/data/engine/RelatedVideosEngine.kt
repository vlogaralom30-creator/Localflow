package com.example.data.engine

import com.example.data.local.VideoEntity
import kotlin.math.abs

/**
 * Smart Local Related Videos recommendation engine.
 * Computes a relevance score based on local metadata without requiring any cloud/AI server.
 */
object RelatedVideosEngine {

    fun getRelatedVideos(
        currentVideo: VideoEntity,
        allVideos: List<VideoEntity>,
        limit: Int = 10
    ): List<VideoEntity> {
        val candidates = allVideos.filter { it.id != currentVideo.id && it.uri != currentVideo.uri }
        if (candidates.isEmpty()) return emptyList()

        val scored = candidates.map { candidate ->
            val score = calculateScore(currentVideo, candidate)
            Pair(candidate, score)
        }

        // Sort by score descending; if score is tied, sort by date added
        return scored
            .sortedWith(
                compareByDescending<Pair<VideoEntity, Int>> { it.second }
                    .thenByDescending { it.first.dateAdded }
            )
            .take(limit)
            .map { it.first }
    }

    private fun calculateScore(target: VideoEntity, candidate: VideoEntity): Int {
        var score = 0

        // 1. Same Folder (+35 pts)
        if (target.folderName.isNotBlank() && target.folderName.equals(candidate.folderName, ignoreCase = true)) {
            score += 35
        }

        // 2. Keyword overlap in titles (+30 pts max)
        val targetKeywords = extractKeywords(target.title)
        val candidateKeywords = extractKeywords(candidate.title)
        val matchingKeywords = targetKeywords.intersect(candidateKeywords)
        score += (matchingKeywords.size * 10).coerceAtMost(30)

        // 3. Same Resolution or category (+15 pts)
        if (!target.resolution.isNullOrBlank() && target.resolution.equals(candidate.resolution, ignoreCase = true)) {
            score += 15
        } else if (target.isShort == candidate.isShort) {
            score += 10
        }

        // 4. Close Duration proximity within 25% (+15 pts)
        if (target.durationMs > 0 && candidate.durationMs > 0) {
            val diff = abs(target.durationMs - candidate.durationMs)
            val quarter = target.durationMs / 4
            if (diff <= quarter) {
                score += 15
            } else if (diff <= target.durationMs / 2) {
                score += 8
            }
        }

        // 5. Same File Format / MIME (+5 pts)
        if (!target.mimeType.isNullOrBlank() && target.mimeType.equals(candidate.mimeType, ignoreCase = true)) {
            score += 5
        }

        // 6. Download / Creation date proximity (within 7 days) (+5 pts)
        val daysDiff = abs(target.dateAdded - candidate.dateAdded) / (1000 * 60 * 60 * 24)
        if (daysDiff <= 7) {
            score += 5
        }

        return score
    }

    private fun extractKeywords(title: String): Set<String> {
        val stopWords = setOf("the", "and", "for", "with", "video", "clip", "mp4", "mkv", "avi", "mov", "hd", "4k")
        return title
            .lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length >= 3 && it !in stopWords }
            .toSet()
    }
}
