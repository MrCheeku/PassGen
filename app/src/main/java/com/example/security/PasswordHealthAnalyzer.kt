package com.example.security

import androidx.compose.ui.graphics.Color
import com.example.data.VaultItem

data class PasswordHealthReport(
    val totalCount: Int,
    val strongCount: Int,
    val weakCount: Int,
    val shortCount: Int,
    val reusedCount: Int,
    val oldCount: Int,
    val missingWebsiteCount: Int,
    val needingAttentionCount: Int,
    val summaryLabel: String,
    val summaryColor: Color,
    val overallScore: Int = if (totalCount > 0) ((strongCount.toFloat() / totalCount) * 100).toInt() else 100
)

object PasswordHealthAnalyzer {

    fun analyze(items: List<VaultItem>): PasswordHealthReport {
        if (items.isEmpty()) {
            return PasswordHealthReport(
                totalCount = 0,
                strongCount = 0,
                weakCount = 0,
                shortCount = 0,
                reusedCount = 0,
                oldCount = 0,
                missingWebsiteCount = 0,
                needingAttentionCount = 0,
                summaryLabel = "Vault is empty",
                summaryColor = Color(0xFF94A3B8)
            )
        }

        val total = items.size
        var strong = 0
        var weak = 0
        var short = 0
        var old = 0
        var missingWebsite = 0

        // Count password occurrences to detect reused passwords
        val passwordCounts = mutableMapOf<String, Int>()
        for (item in items) {
            val count = passwordCounts.getOrDefault(item.password, 0)
            passwordCounts[item.password] = count + 1
        }

        var reused = 0
        val attentionItemIds = mutableSetOf<Long>()

        for (item in items) {
            var itemHasIssue = false

            if (item.strength.score >= 3) {
                strong++
            } else {
                weak++
                itemHasIssue = true
            }

            if (item.isShort) {
                short++
                itemHasIssue = true
            }

            if ((passwordCounts[item.password] ?: 0) > 1) {
                reused++
                itemHasIssue = true
            }

            if (item.isOld) {
                old++
                itemHasIssue = true
            }

            if (item.isMissingWebsite) {
                missingWebsite++
            }

            if (itemHasIssue) {
                attentionItemIds.add(item.id)
            }
        }

        val needingAttention = attentionItemIds.size

        val (summaryLabel, summaryColor) = when {
            needingAttention == 0 -> "Password Health: Excellent" to Color(0xFF10B981)
            needingAttention <= (total * 0.25).toInt().coerceAtLeast(1) -> "Password Health: Good" to Color(0xFF38BDF8)
            needingAttention <= (total * 0.5).toInt().coerceAtLeast(2) -> "$needingAttention passwords need attention" to Color(0xFFF59E0B)
            else -> "$needingAttention passwords need urgent attention" to Color(0xFFEF4444)
        }

        return PasswordHealthReport(
            totalCount = total,
            strongCount = strong,
            weakCount = weak,
            shortCount = short,
            reusedCount = reused,
            oldCount = old,
            missingWebsiteCount = missingWebsite,
            needingAttentionCount = needingAttention,
            summaryLabel = summaryLabel,
            summaryColor = summaryColor
        )
    }
}
