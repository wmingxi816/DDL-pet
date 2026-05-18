package com.ddlmouse.app.domain

import kotlin.random.Random

enum class PetLineScene {
    COMPLETE,
    MISSED,
    REMINDER,
    SUMMARY,
    INTERACT,
    FEED,
    PURCHASE
}

object PetLines {
    private val lines = mapOf(
        PetLineScene.COMPLETE to listOf(
            "完成啦！鼠鼠把这口气先替你松掉。",
            "这一笔很漂亮，DDL 今天少凶一点。",
            "打卡成功，鼠鼠给你记一朵小红花。"
        ),
        PetLineScene.MISSED to listOf(
            "没完成也要看一眼原因，鼠鼠陪你重新排一下。",
            "这次扣分了，但不是世界末日，下一项我们稳住。",
            "鼠鼠有点皱眉，不过还愿意继续帮你盯 DDL。"
        ),
        PetLineScene.REMINDER to listOf(
            "这个 DDL 靠近了，鼠鼠轻轻敲桌子。",
            "先别滑走，这个任务快到点啦。",
            "鼠鼠提醒：现在做一点，晚上少焦虑一点。"
        ),
        PetLineScene.SUMMARY to listOf(
            "昨天的账本翻完啦，今天从最小的一步开始。",
            "鼠鼠看过了，有完成也有遗憾，但节奏还能救。",
            "今天优先处理最靠近的 DDL，鼠鼠在旁边蹲好。"
        ),
        PetLineScene.INTERACT to listOf(
            "吱。鼠鼠在线，DDL 也在线。",
            "摸摸可以，任务也要看一眼。",
            "鼠鼠状态良好，正在偷偷监工。"
        ),
        PetLineScene.FEED to listOf(
            "吃饱了，鼠鼠现在更有精神盯任务。",
            "好吃！这个积分花得有回声。",
            "鼠鼠回血，你也回血。"
        ),
        PetLineScene.PURCHASE to listOf(
            "新东西解锁！鼠鼠的衣柜扩大了一点。",
            "买到啦，努力有了可见的形状。",
            "这份奖励很实在，鼠鼠收下了。"
        )
    )

    fun random(scene: PetLineScene, random: Random = Random.Default): String {
        val sceneLines = lines.getValue(scene)
        return sceneLines[random.nextInt(sceneLines.size)]
    }
}

