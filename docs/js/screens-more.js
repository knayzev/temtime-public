/* EPV web — statistics, settings and profile. */
(function (w) {
  'use strict';
  const EPV = w.EPV;
  const S = EPV.S;
  const { h, btn, field, select, chip, toggle, confirm, toast } = EPV;
  EPV.screens = EPV.screens || {};
  const DAY = 24 * 60 * 60 * 1000;

  const startOfDay = (ms) => { const d = new Date(ms); d.setHours(0, 0, 0, 0); return d.getTime(); };
  function fmtDuration(sec) {
    const hrs = Math.floor(sec / 3600);
    const min = Math.floor((sec % 3600) / 60);
    return hrs > 0 ? hrs + ' ч ' + min + ' мин' : min + ' мин';
  }

  // ---------------- stats ----------------
  EPV.screens.stats = function () {
    const work = S.history.filter((e) => e.phase === 'WORK');
    const todayStart = startOfDay(Date.now());
    const weekStart = todayStart - 6 * DAY;
    const sum = (list) => list.reduce((a, e) => a + e.durationSeconds, 0);
    const todaySec = sum(work.filter((e) => e.startTimeMillis >= todayStart));
    const weekSec = sum(work.filter((e) => e.startTimeMillis >= weekStart));
    const completed = work.filter((e) => !e.interrupted);
    const days = new Set(completed.map((e) => startOfDay(e.startTimeMillis)));
    let streak = 0;
    for (let c = todayStart; days.has(c); c -= DAY) streak++;

    const byCat = {};
    work.forEach((e) => { const k = e.category || 'Без категории'; byCat[k] = (byCat[k] || 0) + e.durationSeconds; });
    const cats = Object.entries(byCat).sort((a, b) => b[1] - a[1]);

    const perDay = [];
    for (let i = 6; i >= 0; i--) {
      const s0 = todayStart - i * DAY;
      perDay.push({ label: new Date(s0).toLocaleDateString('ru-RU', { weekday: 'short' }), sec: sum(work.filter((e) => e.startTimeMillis >= s0 && e.startTimeMillis < s0 + DAY)) });
    }
    const maxSec = Math.max(1, ...perDay.map((d) => d.sec));

    const ach = [1, 10, 50, 100, 500].map((n) => ({ label: n + ' завершённых сессий', cur: completed.length, target: n }))
      .concat([3, 7, 30, 100].map((n) => ({ label: 'Стрик ' + n + ' ' + EPV.daysWord(n), cur: streak, target: n })));

    const stat = (label, value) => h('div', { class: 'stat-row' }, h('span', {}, label), h('span', { class: 'strong' }, value));

    return {
      el: h('div', { class: 'screen' },
        h('h1', {}, 'Статистика'),
        h('div', { class: 'stat-tiles' },
          h('div', { class: 'tile' }, h('div', { class: 'tile-val' }, fmtDuration(todaySec)), h('div', { class: 'muted small' }, 'Сегодня')),
          h('div', { class: 'tile' }, h('div', { class: 'tile-val' }, fmtDuration(weekSec)), h('div', { class: 'muted small' }, 'За неделю')),
          h('div', { class: 'tile' }, h('div', { class: 'tile-val' }, streak + ' ' + EPV.daysWord(streak)), h('div', { class: 'muted small' }, 'Стрик'))),
        h('div', { class: 'card' },
          h('h3', {}, 'Работа за 7 дней'),
          h('div', { class: 'bars' }, perDay.map((d) => h('div', { class: 'bar-col' },
            h('div', { class: 'bar-track' }, h('div', { class: 'bar-fill', style: { height: Math.round((d.sec / maxSec) * 100) + '%' }, title: fmtDuration(d.sec) })),
            h('div', { class: 'bar-label' }, d.label))))),
        h('div', { class: 'card' },
          stat('Завершено сессий', completed.length + ' из ' + work.length),
          stat('Выполнено планов', String(S.planHistory.length))),
        cats.length ? h('div', { class: 'card' }, h('h3', {}, 'По категориям'), cats.map(([k, v]) => stat(k, fmtDuration(v)))) : null,
        h('div', { class: 'card' }, h('h3', {}, 'Достижения'), ach.map((a) => h('div', { class: 'ach' },
          h('span', { class: 'ach-ico' + (a.cur >= a.target ? ' on' : '') }, a.cur >= a.target ? EPV.icon('check', 16) : EPV.icon('lock', 16)),
          h('div', { class: 'grow' }, h('div', {}, a.label), EPV.progressBar(a.cur / a.target)))))),
    };
  };

  // ---------------- settings ----------------
  function download(name, text) {
    const url = URL.createObjectURL(new Blob([text], { type: 'application/json' }));
    const a = h('a', { href: url, download: name });
    document.body.appendChild(a);
    a.click();
    a.remove();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }

  EPV.screens.settings = function (ctx) {
    const root = h('div', { class: 'screen' });
    let newCat = '';

    function render() {
      const cats = S.categories;
      EPV.replace(root, 
        h('h1', {}, 'Настройки'),
        h('div', { class: 'card' },
          h('h3', {}, 'Сигналы'),
          toggle('Звук по окончании этапа', S.sound, (v) => (S.sound = v)),
          toggle('Вибрация по окончании этапа', S.vibration, (v) => (S.vibration = v), 'Работает не во всех браузерах'),
          toggle('Не выключать экран во время таймера', S.keepScreenOn, (v) => (S.keepScreenOn = v)),
          toggle('Уведомления браузера', S.notifications, async (v) => {
            if (v && !(await EPV.requestNotifications())) { S.notifications = false; toast('Разрешение на уведомления не получено'); render(); return; }
            S.notifications = v;
          }, 'Когда вкладка не в фокусе')),

        h('div', { class: 'card' },
          h('h3', {}, 'Голосовое предупреждение'),
          h('p', { class: 'muted small' }, 'Голосом предупредит о смене этапа и объявит следующее действие плана'),
          toggle('Озвучивать этапы и действия', S.voiceEnabled, (v) => { S.voiceEnabled = v; render(); }),
          S.voiceEnabled ? [
            h('div', { class: 'grid2' },
              field({ label: 'За сколько', type: 'number', inputmode: 'numeric', min: 1, max: 600, value: S.voiceLead, onInput: (v) => { const n = parseInt(v, 10); if (n >= 1 && n <= 600) S.voiceLead = n; } }),
              select({ label: 'Единицы', value: S.voiceUnit, options: ['Секунды', 'Минуты'], onChange: (v) => (S.voiceUnit = v) })),
            select({ label: 'Язык озвучки', value: S.voiceLanguage, options: ['Русский', 'English'], onChange: (v) => (S.voiceLanguage = v) }),
            toggle('Реалистичный голос (ElevenLabs)', S.elevenEnabled, (v) => { S.elevenEnabled = v; render(); }, 'Нужен свой API-ключ с elevenlabs.io и интернет'),
            S.elevenEnabled ? field({ label: 'API-ключ ElevenLabs', type: 'password', value: S.elevenKey, autocomplete: 'off', onInput: (v) => (S.elevenKey = v.trim()) }) : null,
            btn('Проверить голос', { kind: 'outline', small: true, onClick: () => { EPV.unlockAudio(); const p = EPV.PHRASES[S.voiceLanguage === 'English' ? 'en' : 'ru']; EPV.speakTest(p.workDone[0]); } }),
          ] : null),

        h('div', { class: 'card' },
          h('h3', {}, 'Категории активности'),
          h('p', { class: 'muted small' }, 'Выбираются на экране таймера и видны в истории'),
          cats.map((c) => h('div', { class: 'row between' }, h('span', {}, c), EPV.iconBtn('close', 'Удалить ' + c, () => { S.categories = S.categories.filter((x) => x !== c); render(); }))),
          h('div', { class: 'row gap end-align' },
            field({ label: 'Новая категория', value: newCat, cls: 'grow', onInput: (v) => (newCat = v) }),
            btn('Добавить', { kind: 'outline', small: true, onClick: () => { const t = newCat.trim(); if (t && !S.categories.includes(t)) S.categories = [...S.categories, t]; newCat = ''; render(); } }))),

        h('div', { class: 'card' },
          h('h3', {}, 'Экспорт и бэкап'),
          h('p', { class: 'muted small' }, 'Данные хранятся только в этом браузере. Файл совместим с приложением для Android — так можно перенести историю.'),
          h('div', { class: 'row gap' },
            btn('Экспортировать', { small: true, onClick: () => download('epv-backup.json', JSON.stringify(S.exportAll(), null, 2)) }),
            btn('Импортировать', { kind: 'outline', small: true, onClick: () => fileInput.click() })),
          fileInput),

        EPV.installPrompt ? h('div', { class: 'card' }, h('h3', {}, 'Установка'), btn('Установить приложение', { onClick: async () => { const p = EPV.installPrompt; EPV.installPrompt = null; p.prompt(); await p.userChoice; render(); } })) : null,

        h('div', { class: 'card' },
          btn('Выйти из аккаунта', { kind: 'outline', block: true, onClick: () => { S.isLoggedIn = false; ctx.go('auth'); } }),
          btn('Удалить все данные', { kind: 'danger', block: true, onClick: () => confirm('Удалить все данные?', 'Аккаунт, история, планы и настройки в этом браузере будут удалены.', 'Удалить', () => { EPV.DB.clearAll(); location.reload(); }) })),
        h('p', { class: 'muted small center' }, 'Веб-версия: без счётчика шагов, автозвонка и фонового сервиса — таймер точен, но вкладка должна оставаться открытой.'));
    }

    const fileInput = h('input', { type: 'file', accept: 'application/json,.json', hidden: true, onChange: (e) => {
      const f = e.target.files && e.target.files[0];
      if (!f) return;
      const fr = new FileReader();
      fr.onload = () => {
        confirm('Импортировать данные?', 'Профиль, настройки и история будут заменены содержимым файла.', 'Импортировать', () => {
          if (S.importAll(String(fr.result))) { toast('Данные импортированы'); EPV.Timer.init(); render(); } else toast('Не удалось прочитать файл');
        });
      };
      fr.readAsText(f);
      e.target.value = '';
    } });
    render();
    return { el: root };
  };

  // ---------------- profile ----------------
  function readPhoto(file, cb) {
    const fr = new FileReader();
    fr.onload = () => {
      const img = new Image();
      img.onload = () => {
        const size = 256;
        const c = document.createElement('canvas');
        c.width = c.height = size;
        const g = c.getContext('2d');
        const s = Math.min(img.width, img.height);
        g.drawImage(img, (img.width - s) / 2, (img.height - s) / 2, s, s, 0, 0, size, size);
        cb(c.toDataURL('image/jpeg', 0.8));
      };
      img.src = String(fr.result);
    };
    fr.readAsDataURL(file);
  }

  function scheduleView() {
    const text = S.daySchedule;
    if (!text.trim()) return h('p', { class: 'muted' }, 'График пока не создан. Пройдите опрос о режиме — мы предложим варианты.');
    return h('div', {}, text.split('\n').map((line) => {
      const m = line.match(/^(\d{2}:\d{2}) — ([^(]+?)(?: \((.*)\))?$/);
      if (!m) return h('div', {}, line);
      return h('div', { class: 'schedule-row' }, h('span', { class: 'time-badge' }, m[1]),
        h('div', {}, h('div', { class: 'strong' }, m[2]), m[3] ? m[3].split('; ').map((t) => h('div', { class: 'tip' }, '• ' + t)) : null));
    }));
  }

  EPV.screens.profile = function (ctx) {
    let tab = EPV.profileTab || 'profile';
    const root = h('div', { class: 'screen' });
    const photoInput = h('input', { type: 'file', accept: 'image/*', hidden: true, onChange: (e) => {
      const f = e.target.files && e.target.files[0];
      if (f) readPhoto(f, (data) => { S.photo = data; render(); });
      e.target.value = '';
    } });

    function form() {
      const fullName = (S.userName + ' ' + S.lastName).trim() || 'Профиль';
      return h('div', {},
        h('div', { class: 'profile-head' },
          h('button', { type: 'button', class: 'avatar', 'aria-label': 'Изменить фото', onClick: () => photoInput.click() },
            S.photo ? h('img', { src: S.photo, alt: 'Фото профиля' }) : h('span', {}, (S.userName || '🙂').charAt(0).toUpperCase())),
          h('div', {}, h('div', { class: 'strong big' }, fullName), h('div', { class: 'muted small' }, S.email))),
        photoInput,
        h('div', { class: 'grid2' },
          field({ label: 'Имя', value: S.userName, onInput: (v) => (S.userName = v) }),
          field({ label: 'Фамилия', value: S.lastName, onInput: (v) => (S.lastName = v) })),
        field({ label: 'Email', type: 'email', value: S.email, onInput: (v) => (S.email = v) }),
        toggle('Согласие на хранение данных', S.consent, (v) => (S.consent = v)),
        h('div', { class: 'grid3' },
          field({ label: 'Вес, кг', type: 'number', inputmode: 'decimal', value: S.weightKg, onInput: (v) => (S.weightKg = v) }),
          field({ label: 'Рост, см', type: 'number', inputmode: 'numeric', value: S.heightCm, onInput: (v) => (S.heightCm = v) }),
          field({ label: 'Возраст', type: 'number', inputmode: 'numeric', value: S.age, onInput: (v) => (S.age = v) })),
        select({ label: 'Пол', value: S.gender || EPV.GENDER_OPTIONS[0], options: EPV.GENDER_OPTIONS, onChange: (v) => (S.gender = v) }),
        select({ label: 'Семейное положение', value: S.maritalStatus || EPV.MARITAL_OPTIONS[0], options: EPV.MARITAL_OPTIONS, onChange: (v) => (S.maritalStatus = v) }),
        select({ label: 'Психологический портрет', value: S.personalityType || EPV.PERSONALITY_OPTIONS[2], options: EPV.PERSONALITY_OPTIONS, onChange: (v) => (S.personalityType = v) }),
        select({ label: 'Как часто просыпаетесь ночью', value: S.nightWake || EPV.NIGHT_WAKE_OPTIONS[0], options: EPV.NIGHT_WAKE_OPTIONS, onChange: (v) => (S.nightWake = v) }),
        h('div', { class: 'grid2' },
          field({ label: 'Подъём', type: 'time', value: S.wakeTime, onChange: (v) => v && (S.wakeTime = v) }),
          field({ label: 'Отбой', type: 'time', value: S.bedTime, onChange: (v) => v && (S.bedTime = v) })),
        toggle('Работаю', S.isWorking, (v) => (S.isWorking = v)));
    }

    function render() {
      EPV.profileTab = tab;
      EPV.replace(root, 
        EPV.tabs([{ id: 'profile', label: 'Профиль' }, { id: 'schedule', label: 'График дня' }, { id: 'advice', label: 'Рекомендации' }], tab, (id) => { tab = id; render(); }),
        tab === 'profile' ? form()
          : tab === 'schedule'
            ? h('div', {}, h('h2', {}, 'График дня'), scheduleView(), btn('Пройти опрос заново', { kind: 'outline', block: true, onClick: () => ctx.openOverlay('lifestyle') }))
            : h('div', {}, h('h2', {}, 'Персональные рекомендации'),
                h('ul', { class: 'advice' }, EPV.buildAdvice({ weightKg: S.weightKg, heightCm: S.heightCm, age: S.age, gender: S.gender, personalityType: S.personalityType }).map((t) => h('li', {}, t)))));
    }
    render();
    return { el: root };
  };
})(window);
