package com.example.myapplication.arithmetic

import kotlin.random.Random

data class RoundComment(val stars: Int, val title: String, val comment: String)

object ResultComment {

    private data class Tier(
        val stars: Int,
        val minRatio: Double,
        val titles: List<String>,
        val comments: List<String>
    )

    private val tiers = listOf(
        Tier(5, 0.95, listOf("口算小状元", "算术小天才", "心算小达人"), listOf("太棒了！五星全收！", "厉害！满分实力！", "无人能挡的解题高手！")),
        Tier(4, 0.90, listOf("计算小高手", "数理小能手"), listOf("非常优秀，离满分一步之遥！", "再冲一把就能全对啦！")),
        Tier(3, 0.80, listOf("进步之星", "勤练小勇士"), listOf("不错哦，继续保持！", "越练越熟练，加油！")),
        Tier(2, 0.60, listOf("坚持小达人", "慢功出细活"), listOf("再加油，多练几次就能全对！", "熟能生巧，继续冲！")),
        Tier(1, 0.00, listOf("锲而不舍小英雄", "明日之星"), listOf("别灰心，熟能生巧！", "每多练一次就更进一步！"))
    )

    fun forRound(total: Int, score: Int, random: Random = Random.Default): RoundComment {
        val ratio = if (total <= 0) 0.0 else score.toDouble() / total
        val tier = tiers.first { ratio >= it.minRatio }
        return RoundComment(
            stars = tier.stars,
            title = tier.titles.random(random),
            comment = tier.comments.random(random)
        )
    }
}