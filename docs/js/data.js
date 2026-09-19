/* EPV web — static data and pure logic (mirrors the Android app). */
(function (w) {
  'use strict';
  const EPV = (w.EPV = w.EPV || {});

  EPV.DEFAULT_CATEGORIES = ['Работа', 'Учёба', 'Соцсети', 'Прокрастинация', 'Другое'];

  EPV.DEFAULT_PRESETS = [
    { id: 'preset_work25', label: 'Работа 25 мин', workMinutes: 25, restMinutes: 5, comment: '' },
    { id: 'preset_deep50', label: 'Глубокая работа 50 мин', workMinutes: 50, restMinutes: 10, comment: '' },
    { id: 'preset_study45', label: 'Учёба 45 мин', workMinutes: 45, restMinutes: 15, comment: '' },
    { id: 'preset_sprint15', label: 'Спринт 15 мин', workMinutes: 15, restMinutes: 5, comment: '' },
  ];

  EPV.QUOTES = [
    'Успех — это способность идти от одной неудачи к другой, не теряя энтузиазма. — Уинстон Черчилль',
    'Единственный способ сделать великую работу — любить то, что ты делаешь. — Стив Джобс',
    'Не бойтесь совершенства — вам его не достичь. — Сальвадор Дали',
    'Дисциплина — это мост между целями и результатом. — Джим Рон',
    'Я не терпел неудачу. Я просто нашёл 10 000 способов, которые не работают. — Томас Эдисон',
    'Секрет продвижения вперёд — начать. — Марк Твен',
    'Тяжело в учении — легко в бою. — Александр Суворов',
    'Будущее принадлежит тем, кто верит в красоту своей мечты. — Элеонора Рузвельт',
    'Маленькие ежедневные улучшения со временем дают потрясающие результаты. — Робин Шарма',
    'Ты никогда не будешь готов на 100%. Начни с тем, что есть. — Наполеон Хилл',
    'Не считай дни, делай дни значимыми. — Мухаммед Али',
    'Лучшее время посадить дерево было 20 лет назад. Второе лучшее — сейчас. — китайская пословица',
    'Делай то, что можешь, с тем, что имеешь, там, где ты есть. — Теодор Рузвельт',
    'Мотивация — то, что заставляет тебя начать. Привычка — то, что заставляет продолжать. — Джим Рон',
    'Единственный, кто может остановить тебя, — это ты сам. — неизвестный автор',
  ];

  EPV.PHRASES = {
    ru: {
      workDone: [
        'Хэй, привет! Ну что, поработал? Пора бы и отдохнуть!',
        'Стоп машина! Работа подождёт — самое время выдохнуть.',
        'Есть! Рабочий блок закрыт. Отдых, встречай!',
        'Отличная работа. Дайте себе немного тишины и покоя.',
        'Всё, шабаш! Заслуженный перерыв уже ждёт.',
        'Мозг просит паузы — дайте ему то, что он хочет.',
        'Рабочий этап завершён. Забота о себе — тоже часть продуктивности.',
        'Ты справился! Теперь можно и ноги на стол.',
        'Тайм-аут! Тело и голова скажут спасибо за пару минут отдыха.',
        'Готово! Сделайте паузу — вы это заслужили.',
      ],
      restDone: [
        'Так, так, время пришло работать! Вперёд и с песней!',
        'Отдых закончен, будильник для мозга прозвенел. За работу!',
        'Перерыв — в архив. Погнали делать великие дела!',
        'Время снова включиться в работу. У вас точно получится.',
        'Батарейка заряжена на сто процентов — пора выдавать результат!',
        'Отдохнули — красота. Теперь покажем, на что способны!',
        'Рабочий режим активирован. Приступаем!',
        'Хватит бездельничать — шучу! Но работать и правда пора.',
        'Соберитесь — сейчас будет продуктивно и красиво.',
        'Вперёд, покоритель дедлайнов! Работа ждёт.',
      ],
      soonRest: 'Приближается время отдыха',
      soonWork: 'Приближается время работы',
      next: 'Дальше: ',
      planDone: 'Все действия выполнены. Отличная работа!',
    },
    en: {
      workDone: [
        'Hey there! You worked hard, huh? Time to rest!',
        'Stop the presses! Work can wait — time to breathe out.',
        'Done and done! Work block closed. Hello, rest!',
        'Great work. Give yourself a little peace and quiet.',
        "That's a wrap! Your well-earned break is waiting.",
        'Your brain is asking for a pause — give it what it wants.',
        'Work block complete. Self-care is productivity too.',
        'You did it! Time to kick back for a bit.',
        'Time out! Your body and mind will thank you for a short rest.',
        "All done! Take a break — you've earned it.",
      ],
      restDone: [
        "Alright, alright, it's time to work! Onward, with a song!",
        "Break's over, the brain alarm just went off. Let's work!",
        "Break — archived. Let's go make great things happen!",
        "Time to get back into it. You've got this.",
        "Battery's at one hundred percent — time to deliver!",
        "Nicely rested. Now let's show what you can do!",
        "Work mode: activated. Let's go!",
        "Enough lounging around — just kidding! But it really is time to work.",
        'Get focused — this is about to be productive and great.',
        'Onward, deadline conqueror! Work awaits.',
      ],
      soonRest: 'Rest time is approaching',
      soonWork: 'Work time is approaching',
      next: 'Next: ',
      planDone: 'All actions are done. Great job!',
    },
  };

  EPV.GENDER_OPTIONS = ['Мужской', 'Женский'];
  EPV.MARITAL_OPTIONS = ['Не женат / не замужем', 'В отношениях', 'Женат / замужем', 'Разведён(а)', 'Вдовец / вдова'];
  EPV.PERSONALITY_OPTIONS = ['Интроверт', 'Экстраверт', 'Амбиверт'];
  EPV.NIGHT_WAKE_OPTIONS = ['Не просыпаюсь', '1 раз', '2–3 раза', 'Часто (4+)'];

  EPV.ROUTINE_LIBRARY = [
    { id: 'routine_wake7', title: 'Проснуться в 7 утра', icon: '⏰', durationMinutes: 0 },
    { id: 'routine_lie5', title: 'Полежать 5 минут', icon: '🛌', durationMinutes: 5 },
    { id: 'routine_sit5', title: 'Посидеть 5 минут', icon: '🧘', durationMinutes: 5 },
    { id: 'routine_exercise', title: 'Сделать зарядку', icon: '🤸', durationMinutes: 10 },
    { id: 'routine_teeth', title: 'Почистить зубы', icon: '🪥', durationMinutes: 3 },
    { id: 'routine_water', title: 'Выпить стакан воды', icon: '💧', durationMinutes: 1 },
    { id: 'routine_walk', title: 'Прогуляться', icon: '🚶', durationMinutes: 15 },
    { id: 'routine_stretch', title: 'Растяжка', icon: '🤾', durationMinutes: 10 },
    { id: 'routine_shower', title: 'Принять душ', icon: '🚿', durationMinutes: 10 },
    { id: 'routine_breakfast', title: 'Позавтракать', icon: '🍳', durationMinutes: 15 },
    { id: 'routine_journal', title: 'Записать мысли в дневник', icon: '📓', durationMinutes: 5 },
    { id: 'routine_plan', title: 'Составить план на день', icon: '📝', durationMinutes: 5 },
  ];
  EPV.DEFAULT_ROUTINE = EPV.ROUTINE_LIBRARY.slice(0, 5);

  EPV.PLAN_LIBRARY = [
    { id: 'plantask_wake', title: 'Проснуться', durationMinutes: 1 },
    { id: 'plantask_water', title: 'Стакан воды', durationMinutes: 1 },
    { id: 'plantask_stretch', title: 'Растяжка', durationMinutes: 10 },
    { id: 'plantask_exercise', title: 'Зарядка', durationMinutes: 15 },
    { id: 'plantask_shower', title: 'Душ', durationMinutes: 10 },
    { id: 'plantask_breakfast', title: 'Завтрак', durationMinutes: 20 },
    { id: 'plantask_plan', title: 'Планирование дня', durationMinutes: 5 },
    { id: 'plantask_meditate', title: 'Медитация', durationMinutes: 10 },
    { id: 'plantask_walk', title: 'Прогулка', durationMinutes: 15 },
    { id: 'plantask_read', title: 'Чтение', durationMinutes: 15 },
    { id: 'plantask_teeth', title: 'Почистить зубы', durationMinutes: 3 },
    { id: 'plantask_work', title: 'Рабочий блок', durationMinutes: 45 },
    { id: 'plantask_sport', title: 'Спорт', durationMinutes: 40 },
    { id: 'plantask_lunch', title: 'Обед', durationMinutes: 30 },
  ];

  EPV.LIFESTYLE_QUESTIONS = [
    { id: 'q1', question: 'Что мешает пробудиться?', options: ['Поздно лёг спать', 'Будильник не слышу / просыпаю', 'Нет сил встать сразу', 'Комната тёмная, нет света', 'Хочется доспать ещё'] },
    { id: 'q2', question: 'Что помогает пробудиться?', options: ['Будильник со звуком/светом', 'Стакан воды сразу', 'Открыть шторы / дневной свет', 'Зарядка или растяжка', 'Кофе/чай'] },
    { id: 'q3', question: 'Как заканчивается вечер?', options: ['Листаю телефон в кровати', 'Смотрю сериалы/видео', 'Читаю книгу', 'Готовлюсь к следующему дню', 'Засыпаю случайно, без чёткого времени'] },
    { id: 'q4', question: 'Как начинается утро?', options: ['Сразу хватаю телефон', 'Завтракаю не спеша', 'Тороплюсь, не завтракаю', 'Делаю зарядку/растяжку', 'Планирую день'] },
    { id: 'q5', question: 'Что мешает рано засыпать?', options: ['Долго сижу в телефоне', 'Много дел вечером', 'Тревожные мысли', 'Поздний ужин/кофе', 'Нет режима — ложусь в разное время'] },
    { id: 'q6', question: 'Что помогает рано засыпать?', options: ['Фиксированное время отбоя', 'Убрать телефон за час до сна', 'Тёплый душ/ванна', 'Проветрить комнату', 'Чтение книги перед сном'] },
  ];

  const TIP = {
    'Поздно лёг спать': ['evening', 'Старайтесь ложиться вовремя — это первый шаг к лёгкому пробуждению'],
    'Будильник не слышу / просыпаю': ['morning', 'Поставьте будильник подальше от кровати'],
    'Нет сил встать сразу': ['morning', 'Сразу после будильника — стакан воды и лёгкая растяжка'],
    'Комната тёмная, нет света': ['morning', 'Откройте шторы или используйте лампу дневного света'],
    'Хочется доспать ещё': ['morning', 'Не переносите будильник — вставайте по первому сигналу'],
    'Будильник со звуком/светом': ['morning', 'Будильник с нарастающим звуком/светом облегчает пробуждение'],
    'Стакан воды сразу': ['morning', 'Держите стакан воды у кровати с вечера'],
    'Открыть шторы / дневной свет': ['morning', 'Откройте шторы сразу после пробуждения'],
    'Зарядка или растяжка': ['morning', '5–10 минут зарядки взбодрят лучше кофе'],
    'Кофе/чай': ['morning', 'Кофе — не раньше чем через 60–90 минут после пробуждения'],
    'Листаю телефон в кровати': ['evening', 'Уберите телефон за час до сна'],
    'Смотрю сериалы/видео': ['evening', 'Замените экран на подкаст или книгу за час до сна'],
    'Читаю книгу': ['evening', 'Отличная привычка — сохраните её'],
    'Готовлюсь к следующему дню': ['evening', 'Планирование с вечера снижает тревожность утром'],
    'Засыпаю случайно, без чёткого времени': ['evening', 'Установите фиксированное время отбоя'],
    'Сразу хватаю телефон': ['morning', 'Отложите телефон на 20–30 минут после пробуждения'],
    'Завтракаю не спеша': ['morning', 'Хорошая привычка — сохраните её'],
    'Тороплюсь, не завтракаю': ['morning', 'Попробуйте вставать на 15 минут раньше ради завтрака'],
    'Делаю зарядку/растяжку': ['morning', 'Отлично — так и продолжайте'],
    'Планирую день': ['morning', 'Хорошая привычка — сохраните её'],
    'Долго сижу в телефоне': ['evening', 'Уберите телефон за час до сна'],
    'Много дел вечером': ['evening', 'Перенесите часть дел на утро'],
    'Тревожные мысли': ['evening', 'Выпишите тревожные мысли на бумагу перед сном'],
    'Поздний ужин/кофе': ['evening', 'Ужинайте не позднее чем за 3 часа до сна, без кофеина'],
    'Нет режима — ложусь в разное время': ['evening', 'Ложитесь и вставайте в одно и то же время каждый день'],
    'Фиксированное время отбоя': ['evening', 'Сохраняйте фиксированное время отбоя'],
    'Убрать телефон за час до сна': ['evening', 'Продолжайте убирать телефон заранее'],
    'Тёплый душ/ванна': ['evening', 'Тёплый душ за 30–60 минут до сна помогает уснуть быстрее'],
    'Проветрить комнату': ['evening', 'Проветривайте спальню перед сном'],
    'Чтение книги перед сном': ['evening', 'Чтение — отличная альтернатива экрану'],
  };

  const PHONE_TIPS = ['Уберите телефон за час до сна', 'Продолжайте убирать телефон заранее'];

  // Each variant changes block gaps too, so variants are visibly different, not just a time shift.
  const PROFILES = [
    { wake: 0, bed: 0, breakfast: 0, lunch: 0, dinner: 0, morning: 20, postBreakfast: 30, postLunch: 60, postDinner: 120 },
    { wake: -20, bed: -20, breakfast: -10, lunch: -15, dinner: -20, morning: 15, postBreakfast: 20, postLunch: 45, postDinner: 90 },
    { wake: 20, bed: 20, breakfast: 15, lunch: 15, dinner: 15, morning: 30, postBreakfast: 40, postLunch: 75, postDinner: 150 },
    { wake: 0, bed: 0, breakfast: 10, lunch: -10, dinner: 5, morning: 25, postBreakfast: 35, postLunch: 90, postDinner: 105 },
  ];
  EPV.SCHEDULE_VARIANTS = PROFILES.length;

  function toMin(text) {
    const p = String(text || '').split(':');
    const h = parseInt(p[0], 10);
    const m = parseInt(p[1], 10);
    return (isNaN(h) ? 8 : h) * 60 + (isNaN(m) ? 0 : m);
  }
  function toTime(total) {
    const n = ((total % 1440) + 1440) % 1440;
    return String(Math.floor(n / 60)).padStart(2, '0') + ':' + String(n % 60).padStart(2, '0');
  }
  EPV.toMin = toMin;
  EPV.toTime = toTime;

  EPV.generateSchedule = function (answers, t, variant) {
    const p = PROFILES[((variant % PROFILES.length) + PROFILES.length) % PROFILES.length];
    const all = Object.values(answers || {}).flat();
    const tips = all.map((a) => TIP[a]).filter(Boolean);
    const morning = [...new Set(tips.filter((x) => x[0] === 'morning').map((x) => x[1]))];
    const evening = [...new Set(tips.filter((x) => x[0] === 'evening').map((x) => x[1]))].filter((x) => !PHONE_TIPS.includes(x));
    const wake = toMin(t.wakeTime) + p.wake;
    const bed = toMin(t.bedTime) + p.bed;
    const breakfast = toMin(t.breakfastTime) + p.breakfast;
    const lunch = toMin(t.lunchTime) + p.lunch;
    const dinner = toMin(t.dinnerTime) + p.dinner;
    return [
      { time: toTime(wake), title: 'Подъём', tips: morning.slice(0, 2) },
      { time: toTime(wake + p.morning), title: 'Утренняя рутина', tips: [] },
      { time: toTime(breakfast), title: 'Завтрак', tips: [] },
      { time: toTime(breakfast + p.postBreakfast), title: 'Работа', tips: ['Блоками по 45–50 минут с перерывами 10–15 минут'] },
      { time: toTime(lunch), title: 'Обед', tips: [] },
      { time: toTime(lunch + p.postLunch), title: 'Работа', tips: [] },
      { time: toTime(dinner), title: 'Ужин', tips: [] },
      { time: toTime(dinner + p.postDinner), title: 'Вечер', tips: evening.slice(0, 2) },
      { time: toTime(bed - 60), title: 'Отключить уведомления на телефоне', tips: ['Включите «Не беспокоить» или авиарежим — так точно уснёте вовремя'] },
      { time: toTime(bed), title: 'Отбой', tips: evening.slice(2, 3) },
    ];
  };

  EPV.scheduleToText = function (items) {
    return items
      .map((i) => i.time + ' — ' + i.title + (i.tips.length ? ' (' + i.tips.join('; ') + ')' : ''))
      .join('\n');
  };

  // ---------- Advice engine ----------
  function sleepAdvice(age) {
    if (age == null) return 'Взрослым обычно рекомендуют 7–9 часов сна.';
    if (age < 13) return 'В возрасте 10–12 лет организму нужно 9–12 часов сна — старайтесь укладываться спать не позднее 21:30.';
    if (age < 16) return 'Подросткам 13–15 лет рекомендуется 8–10 часов сна, особенно в учебные дни.';
    if (age < 18) return 'В 16–17 лет всё ещё важно спать 8–10 часов — недосып в этом возрасте сильнее всего бьёт по концентрации.';
    if (age < 26) return 'В 18–25 лет обычно достаточно 7–9 часов, но соблазн лечь позже велик — держите постоянный режим.';
    if (age < 36) return 'В 26–35 лет рекомендуется 7–9 часов сна: это база для восстановления при высокой рабочей нагрузке.';
    if (age < 46) return 'В 36–45 лет держите 7–8 часов сна и старайтесь не брать работу в постель — это заметно влияет на качество сна.';
    if (age < 56) return 'В 46–55 лет рекомендуется 7–8 часов; в этом возрасте особенно важен стабильный ритм отхода ко сну.';
    if (age < 66) return 'В 56–65 лет обычно достаточно 7–8 часов, но чувствительность к кофеину вечером возрастает — ограничьте его после обеда.';
    if (age < 76) return 'В 66–75 лет рекомендуется 7–8 часов ночного сна; короткий дневной сон 20–30 минут — это нормально и полезно.';
    return 'В возрасте 76+ часто достаточно 7–8 часов с более гибким графиком; дневной отдых 20–30 минут помогает компенсировать более чуткий ночной сон.';
  }

  function childBmi(age, weight, heightM) {
    const base = age < 13
      ? 'Детям рекомендуется минимум 60 минут активной игры или спорта в день.'
      : 'Подросткам рекомендуется минимум 60 минут умеренной или высокой физической активности в день, включая силовые упражнения 2–3 раза в неделю.';
    if (weight == null || heightM == null || heightM <= 0) return base;
    return base + ' Индекс массы тела у детей и подростков оценивается по возрастным перцентилям, а не по взрослым нормам — если вес заметно выходит за пределы возрастной нормы, стоит обсудить это с педиатром.';
  }

  function adultBmi(bmi, age, isFemale) {
    const senior = age != null && age >= 65
      ? ' После 65 лет для суставов особенно хорошо подходят низкоударные нагрузки: плавание, скандинавская ходьба, велотренажёр.'
      : '';
    let t;
    if (bmi < 16) t = 'Индекс массы тела сильно ниже нормы (выраженный дефицит массы) — это повод обсудить питание и самочувствие с врачом; самостоятельно наращивать нагрузку в этом состоянии не стоит.';
    else if (bmi < 17) t = 'Индекс массы тела заметно ниже нормы — сделайте акцент на регулярном полноценном питании и лёгких силовых упражнениях 2 раза в неделю, без интенсивного кардио.';
    else if (bmi < 18.5) t = 'Индекс массы тела немного ниже нормы — добавьте силовые тренировки 2–3 раза в неделю и следите, чтобы питание покрывало расход энергии.';
    else if (bmi < 25) t = 'Индекс массы тела в норме — поддерживайте активность: минимум 150 минут в неделю умеренной нагрузки, около 20–30 минут в день.' + senior;
    else if (bmi < 30) t = 'Индекс массы тела немного повышен — старайтесь проходить не менее 8000 шагов в день и добавьте 2 силовые тренировки в неделю.' + senior;
    else if (bmi < 35) t = 'Индекс массы тела повышен (ожирение I степени) — начните с лёгкой активности: ходьба 20–30 минут в день, постепенно увеличивая длительность и темп.' + senior;
    else if (bmi < 40) t = 'Индекс массы тела значительно повышен (ожирение II степени) — низкоударные нагрузки (плавание, велосипед, ходьба) безопаснее для суставов, чем бег; имеет смысл обсудить план с врачом.' + senior;
    else t = 'Индекс массы тела существенно повышен (ожирение III степени) — прежде чем наращивать нагрузку, стоит проконсультироваться с врачом и подобрать безопасный для суставов формат активности.' + senior;
    if (isFemale && bmi < 18.5) t += ' Женщинам с дефицитом массы особенно важно следить за железом и кальцием в рационе.';
    return t;
  }

  function weightExtreme(weight, age) {
    if (weight < 35) return 'Указанный вес очень низкий — рекомендуем обсудить питание и самочувствие с врачом.';
    if (weight < 40 && (age == null || age >= 18)) return 'Вес заметно ниже типичного для взрослого человека — стоит проверить, хватает ли калорий и белка в рационе.';
    if (weight > 180) return 'При весе в этом диапазоне особенно полезны низкоударные тренировки (плавание, велосипед) — они снижают нагрузку на суставы и позвоночник.';
    if (weight > 150) return 'Начинайте активность постепенно и отдавайте предпочтение низкоударным нагрузкам — так суставы адаптируются безопаснее.';
    return null;
  }

  function heightAdvice(h) {
    if (h < 150) return 'При невысоком росте особенно важно настроить рабочее место: высота стула и монитора должны позволять держать спину прямо, а ноги — полностью стоять на полу или подставке.';
    if (h > 195) return 'При высоком росте проверьте рабочее место: слишком низкий стол или монитор заставляют сутулиться — поднимите монитор на уровень глаз и следите за пространством для ног.';
    if (h > 185) return 'При вашем росте обратите внимание на высоту стола и монитора, чтобы не сутулиться во время долгой работы.';
    return null;
  }

  function genderAdvice(isFemale, isMale, age) {
    if (isFemale && age != null && age >= 45) return 'Женщинам после 45 особенно важны кальций, витамин D и силовые упражнения — они поддерживают плотность костей.';
    if (isFemale) return 'Женщинам, особенно при высокой физической нагрузке, стоит следить за железом в рационе.';
    if (isMale && age != null && age >= 40) return 'Мужчинам после 40 рекомендуется регулярно проверять сердечно-сосудистую систему и не пренебрегать кардионагрузкой 2–3 раза в неделю.';
    if (isMale) return 'Мужчинам стоит сочетать силовые тренировки с кардионагрузкой — это лучше поддерживает сердце и сосуды в долгосрочной перспективе.';
    return 'Сочетайте силовые упражнения с кардионагрузкой — так поддерживаются и мышцы, и сердечно-сосудистая система.';
  }

  function focusAdvice(age) {
    if (age != null && age < 16) return 'Для учёбы подойдут блоки покороче — 20–30 минут работы, затем 5–10 минут перерыва.';
    if (age != null && age < 18) return 'Попробуйте блоки по 30–40 минут с перерывами 10 минут — так легче удерживать концентрацию на учёбе.';
    if (age != null && age >= 66) return 'Блоки по 30–40 минут с перерывом 10–15 минут обычно комфортнее для долгой концентрации, чем длинные подходы.';
    return 'Работайте блоками по 45–50 минут, затем делайте перерыв 10–15 минут. На перерыве лучше пройтись или почитать, а не листать телефон — так мозг действительно отдыхает.';
  }

  function personalityAdvice(p) {
    if (p === 'Интроверт') return 'Как интроверту, вам может требоваться больше времени в одиночестве для восстановления энергии — закладывайте в график паузы без общения между рабочими блоками.';
    if (p === 'Экстраверт') return 'Как экстраверту, вам может помогать смена обстановки и общение — короткая пауза с разговором восстанавливает концентрацию лучше, чем тихий отдых в одиночестве.';
    if (p === 'Амбиверт') return 'Как амбиверту, вам стоит ориентироваться на своё текущее состояние — иногда лучше отдохнуть в тишине, иногда — переключиться на общение.';
    return null;
  }

  EPV.buildAdvice = function (p) {
    const age = parseInt(p.age, 10);
    const ageV = isNaN(age) ? null : age;
    const weight = parseFloat(p.weightKg);
    const weightV = isNaN(weight) ? null : weight;
    const hcm = parseFloat(p.heightCm);
    const hcmV = isNaN(hcm) ? null : hcm;
    const hm = hcmV == null ? null : hcmV / 100;
    const isFemale = p.gender === 'Женский';
    const isMale = p.gender === 'Мужской';
    const out = [sleepAdvice(ageV)];

    if (ageV != null && ageV < 18) out.push(childBmi(ageV, weightV, hm));
    else if (weightV != null && hm != null && hm > 0) out.push(adultBmi(weightV / (hm * hm), ageV, isFemale));
    else out.push('Заполните вес и рост в профиле, чтобы получить более точный совет по активности.');

    if (weightV != null) { const x = weightExtreme(weightV, ageV); if (x) out.push(x); }
    if (hcmV != null) { const x = heightAdvice(hcmV); if (x) out.push(x); }
    out.push(genderAdvice(isFemale, isMale, ageV));
    out.push(focusAdvice(ageV));
    const pa = personalityAdvice(p.personalityType);
    if (pa) out.push(pa);
    return out;
  };
})(window);
