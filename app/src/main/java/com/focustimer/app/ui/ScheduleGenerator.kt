package com.focustimer.app.ui

data class LifestyleQuestion(val id: String, val question: String, val options: List<String>)

val LIFESTYLE_QUESTIONS = listOf(
    LifestyleQuestion(
        "q1", "Что мешает пробудиться?",
        listOf(
            "Поздно лёг спать",
            "Будильник не слышу / просыпаю",
            "Нет сил встать сразу",
            "Комната тёмная, нет света",
            "Хочется доспать ещё"
        )
    ),
    LifestyleQuestion(
        "q2", "Что помогает пробудиться?",
        listOf(
            "Будильник со звуком/светом",
            "Стакан воды сразу",
            "Открыть шторы / дневной свет",
            "Зарядка или растяжка",
            "Кофе/чай"
        )
    ),
    LifestyleQuestion(
        "q3", "Как заканчивается вечер?",
        listOf(
            "Листаю телефон в кровати",
            "Смотрю сериалы/видео",
            "Читаю книгу",
            "Готовлюсь к следующему дню",
            "Засыпаю случайно, без чёткого времени"
        )
    ),
    LifestyleQuestion(
        "q4", "Как начинается утро?",
        listOf(
            "Сразу хватаю телефон",
            "Завтракаю не спеша",
            "Тороплюсь, не завтракаю",
            "Делаю зарядку/растяжку",
            "Планирую день"
        )
    ),
    LifestyleQuestion(
        "q5", "Что мешает рано засыпать?",
        listOf(
            "Долго сижу в телефоне",
            "Много дел вечером",
            "Тревожные мысли",
            "Поздний ужин/кофе",
            "Нет режима — ложусь в разное время"
        )
    ),
    LifestyleQuestion(
        "q6", "Что помогает рано засыпать?",
        listOf(
            "Фиксированное время отбоя",
            "Убрать телефон за час до сна",
            "Тёплый душ/ванна",
            "Проветрить комнату",
            "Чтение книги перед сном"
        )
    )
)

data class ScheduleItem(val time: String, val title: String, val tips: List<String> = emptyList())

private val TIP_LOOKUP: Map<String, Pair<String, String>> = mapOf(
    "Поздно лёг спать" to ("evening" to "Старайтесь ложиться вовремя — это первый шаг к лёгкому пробуждению"),
    "Будильник не слышу / просыпаю" to ("morning" to "Поставьте будильник подальше от кровати"),
    "Нет сил встать сразу" to ("morning" to "Сразу после будильника — стакан воды и лёгкая растяжка"),
    "Комната тёмная, нет света" to ("morning" to "Откройте шторы или используйте лампу дневного света"),
    "Хочется доспать ещё" to ("morning" to "Не переносите будильник — вставайте по первому сигналу"),

    "Будильник со звуком/светом" to ("morning" to "Будильник с нарастающим звуком/светом облегчает пробуждение"),
    "Стакан воды сразу" to ("morning" to "Держите стакан воды у кровати с вечера"),
    "Открыть шторы / дневной свет" to ("morning" to "Откройте шторы сразу после пробуждения"),
    "Зарядка или растяжка" to ("morning" to "5–10 минут зарядки взбодрят лучше кофе"),
    "Кофе/чай" to ("morning" to "Кофе — не раньше чем через 60–90 минут после пробуждения"),

    "Листаю телефон в кровати" to ("evening" to "Уберите телефон за час до сна"),
    "Смотрю сериалы/видео" to ("evening" to "Замените экран на подкаст или книгу за час до сна"),
    "Читаю книгу" to ("evening" to "Отличная привычка — сохраните её"),
    "Готовлюсь к следующему дню" to ("evening" to "Планирование с вечера снижает тревожность утром"),
    "Засыпаю случайно, без чёткого времени" to ("evening" to "Установите фиксированное время отбоя"),

    "Сразу хватаю телефон" to ("morning" to "Отложите телефон на 20–30 минут после пробуждения"),
    "Завтракаю не спеша" to ("morning" to "Хорошая привычка — сохраните её"),
    "Тороплюсь, не завтракаю" to ("morning" to "Попробуйте вставать на 15 минут раньше ради завтрака"),
    "Делаю зарядку/растяжку" to ("morning" to "Отлично — так и продолжайте"),
    "Планирую день" to ("morning" to "Хорошая привычка — сохраните её"),

    "Долго сижу в телефоне" to ("evening" to "Уберите телефон за час до сна"),
    "Много дел вечером" to ("evening" to "Перенесите часть дел на утро"),
    "Тревожные мысли" to ("evening" to "Выпишите тревожные мысли на бумагу перед сном"),
    "Поздний ужин/кофе" to ("evening" to "Ужинайте не позднее чем за 3 часа до сна, без кофеина"),
    "Нет режима — ложусь в разное время" to ("evening" to "Ложитесь и вставайте в одно и то же время каждый день"),

    "Фиксированное время отбоя" to ("evening" to "Сохраняйте фиксированное время отбоя"),
    "Убрать телефон за час до сна" to ("evening" to "Продолжайте убирать телефон заранее"),
    "Тёплый душ/ванна" to ("evening" to "Тёплый душ за 30–60 минут до сна помогает уснуть быстрее"),
    "Проветрить комнату" to ("evening" to "Проветривайте спальню перед сном"),
    "Чтение книги перед сном" to ("evening" to "Чтение — отличная альтернатива экрану")
)

// These are now represented by their own dedicated "turn off notifications" step instead of a
// generic tip line, so they're filtered out of the regular evening tips list.
private val PHONE_TIP_TEXTS = setOf(
    "Уберите телефон за час до сна",
    "Продолжайте убирать телефон заранее"
)

/**
 * Each variant changes more than a uniform time shift — the gaps between blocks (morning routine
 * length, work-block start delays, lunch/evening buffer) differ too, so consecutive variants are
 * actually distinguishable instead of collapsing into a barely-noticeable ±30 minute shift.
 */
private data class ScheduleProfile(
    val wakeShift: Int,
    val bedShift: Int,
    val breakfastShift: Int,
    val lunchShift: Int,
    val dinnerShift: Int,
    val morningRoutineOffset: Int,
    val postBreakfastGap: Int,
    val postLunchGap: Int,
    val postDinnerGap: Int
)

private val SCHEDULE_PROFILES = listOf(
    ScheduleProfile(0, 0, 0, 0, 0, 20, 30, 60, 120),
    ScheduleProfile(-20, -20, -10, -15, -20, 15, 20, 45, 90),
    ScheduleProfile(20, 20, 15, 15, 15, 30, 40, 75, 150),
    ScheduleProfile(0, 0, 10, -10, 5, 25, 35, 90, 105)
)

private fun timeToMinutes(text: String): Int {
    val parts = text.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: 8
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return h * 60 + m
}

private fun minutesToTime(total: Int): String {
    val normalized = ((total % 1440) + 1440) % 1440
    return "%02d:%02d".format(normalized / 60, normalized % 60)
}

fun generateSchedule(
    answers: Map<String, List<String>>,
    wakeTime: String,
    bedTime: String,
    breakfastTime: String,
    lunchTime: String,
    dinnerTime: String,
    variant: Int
): List<ScheduleItem> {
    val profile = SCHEDULE_PROFILES[((variant % SCHEDULE_PROFILES.size) + SCHEDULE_PROFILES.size) % SCHEDULE_PROFILES.size]

    val allSelected = answers.values.flatten()
    val morningTips = allSelected.mapNotNull { TIP_LOOKUP[it] }
        .filter { it.first == "morning" }.map { it.second }.distinct()
    val eveningTips = allSelected.mapNotNull { TIP_LOOKUP[it] }
        .filter { it.first == "evening" }.map { it.second }.distinct()
        .filterNot { it in PHONE_TIP_TEXTS }

    val wake = timeToMinutes(wakeTime) + profile.wakeShift
    val bed = timeToMinutes(bedTime) + profile.bedShift
    val breakfast = timeToMinutes(breakfastTime) + profile.breakfastShift
    val lunch = timeToMinutes(lunchTime) + profile.lunchShift
    val dinner = timeToMinutes(dinnerTime) + profile.dinnerShift

    return listOf(
        ScheduleItem(minutesToTime(wake), "Подъём", morningTips.take(2)),
        ScheduleItem(minutesToTime(wake + profile.morningRoutineOffset), "Утренняя рутина"),
        ScheduleItem(minutesToTime(breakfast), "Завтрак"),
        ScheduleItem(
            minutesToTime(breakfast + profile.postBreakfastGap),
            "Работа",
            listOf("Блоками по 45–50 минут с перерывами 10–15 минут")
        ),
        ScheduleItem(minutesToTime(lunch), "Обед"),
        ScheduleItem(minutesToTime(lunch + profile.postLunchGap), "Работа"),
        ScheduleItem(minutesToTime(dinner), "Ужин"),
        ScheduleItem(minutesToTime(dinner + profile.postDinnerGap), "Вечер", eveningTips.take(2)),
        ScheduleItem(
            minutesToTime(bed - 60),
            "Отключить уведомления на телефоне",
            listOf("Включите «Не беспокоить» или авиарежим — так точно уснёте вовремя")
        ),
        ScheduleItem(minutesToTime(bed), "Отбой", eveningTips.drop(2).take(1))
    )
}

fun scheduleToText(items: List<ScheduleItem>): String {
    return items.joinToString("\n") { item ->
        val tipsText = if (item.tips.isNotEmpty()) " (" + item.tips.joinToString("; ") + ")" else ""
        "${item.time} — ${item.title}$tipsText"
    }
}
