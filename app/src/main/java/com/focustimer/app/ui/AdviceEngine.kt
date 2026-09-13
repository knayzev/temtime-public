package com.focustimer.app.ui

/**
 * Simple rule-based tips derived from basic body/age data. Not medical advice —
 * just sensible defaults to nudge the user toward a healthier routine.
 */
fun buildAdvice(weightKgText: String, heightCmText: String, ageText: String, gender: String): List<String> {
    val advice = mutableListOf<String>()
    val age = ageText.toIntOrNull()
    val weight = weightKgText.toDoubleOrNull()
    val heightM = heightCmText.toDoubleOrNull()?.div(100)

    advice += when {
        age == null -> "Взрослым обычно рекомендуют 7–9 часов сна."
        age < 18 -> "В вашем возрасте рекомендуется 8–10 часов сна."
        age in 18..64 -> "Рекомендуется 7–9 часов сна."
        else -> "Рекомендуется 7–8 часов сна."
    }

    if (weight != null && heightM != null && heightM > 0) {
        val bmi = weight / (heightM * heightM)
        advice += when {
            bmi < 18.5 -> "Индекс массы тела ниже нормы — добавьте силовые тренировки 2–3 раза в неделю и следите за питанием."
            bmi < 25 -> "Индекс массы тела в норме — поддерживайте активность: минимум 150 минут в неделю, около 30 минут в день."
            bmi < 30 -> "Индекс массы тела немного повышен — старайтесь проходить не менее 8000 шагов в день."
            else -> "Индекс массы тела повышен — начните с лёгкой активности: ходьба 20–30 минут в день, постепенно увеличивая нагрузку."
        }
    } else {
        advice += "Заполните вес и рост выше, чтобы получить более точный совет по активности."
    }

    if (gender == "Женский") {
        advice += "Женщинам старше 18 рекомендуется дополнительно следить за железом в питании при высокой физической нагрузке."
    }

    advice += "Работайте блоками по 45–50 минут, затем делайте перерыв 10–15 минут."
    advice += "На перерыве лучше пройтись или почитать, а не листать телефон — так мозг действительно отдыхает."

    return advice
}
