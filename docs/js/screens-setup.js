/* EPV web — auth, intro, onboarding and the schedule generator (setup flow steps). */
(function (w) {
  'use strict';
  const EPV = w.EPV;
  const S = EPV.S;
  const { h, btn, field, select, chip, numStepper, stepperHeader, hero, toast } = EPV;
  const STEP_LABELS = ['О себе', 'План на день', 'Готово'];

  // ---------------- auth ----------------
  EPV.screens = EPV.screens || {};

  EPV.screens.auth = function (ctx) {
    let mode = S.isRegistered ? 'login' : 'register';
    let name = '', email = S.email, password = '', password2 = '', consent = false, error = '';
    const root = h('div', { class: 'screen auth' });

    function afterAuth() {
      S.isLoggedIn = true;
      ctx.go(S.isOnboarded ? 'main' : 'intro');
    }

    async function submit() {
      error = '';
      if (mode === 'register') {
        if (!name.trim()) error = 'Введите имя';
        else if (!/^\S+@\S+\.\S+$/.test(email.trim())) error = 'Введите корректный email';
        else if (password.length < 6) error = 'Пароль — минимум 6 символов';
        else if (password !== password2) error = 'Пароли не совпадают';
        else if (!consent) error = 'Нужно согласие на хранение данных на устройстве';
        if (error) return render();
        S.userName = name.trim();
        S.email = email.trim();
        S.passwordHash = await EPV.hash(password);
        S.isRegistered = true;
        S.consent = true;
        afterAuth();
      } else {
        const hash = await EPV.hash(password);
        if (email.trim().toLowerCase() !== S.email.toLowerCase() || hash !== S.passwordHash) {
          error = 'Неверный email или пароль';
          return render();
        }
        afterAuth();
      }
    }

    function render() {
      EPV.replace(root, 
        h('div', { class: 'auth-hero' }, hero('⏱️', 104), h('h1', { class: 'brand' }, 'EPV'),
          h('p', { class: 'muted center' }, 'Дисциплина, время и привычки — в одном месте')),
        h('div', { class: 'card' },
          h('h2', {}, mode === 'register' ? 'Регистрация' : 'Вход'),
          mode === 'register' ? field({ label: 'Имя', value: name, autocomplete: 'given-name', onInput: (v) => (name = v) }) : null,
          field({ label: 'Email', type: 'email', value: email, autocomplete: 'email', onInput: (v) => (email = v) }),
          field({ label: 'Пароль', type: 'password', value: password, autocomplete: mode === 'register' ? 'new-password' : 'current-password', onInput: (v) => (password = v) }),
          mode === 'register' ? field({ label: 'Повторите пароль', type: 'password', value: password2, autocomplete: 'new-password', onInput: (v) => (password2 = v) }) : null,
          mode === 'register'
            ? EPV.toggle('Согласен(на) на хранение данных в этом браузере', consent, (v) => (consent = v), 'Данные не отправляются на сервер')
            : null,
          error ? h('p', { class: 'error', role: 'alert' }, error) : null,
          btn(mode === 'register' ? 'Создать аккаунт' : 'Войти', { block: true, onClick: submit }),
          S.isRegistered
            ? h('button', { type: 'button', class: 'link', onClick: () => { mode = mode === 'login' ? 'register' : 'login'; error = ''; render(); } },
                mode === 'login' ? 'Создать новый аккаунт' : 'У меня уже есть аккаунт')
            : null,
          mode === 'login'
            ? h('button', { type: 'button', class: 'link danger-link', onClick: () => EPV.confirm('Сбросить все данные?', 'Аккаунт, история и планы в этом браузере будут удалены. Действие необратимо.', 'Сбросить', () => { EPV.DB.clearAll(); location.reload(); }) }, 'Забыли пароль? Сбросить данные')
            : null));
    }
    render();
    return { el: root };
  };

  // ---------------- intro ----------------
  EPV.screens.intro = function (ctx) {
    return {
      el: h('div', { class: 'screen intro' },
        hero('🌱', 112),
        h('h1', { class: 'center' }, 'Добро пожаловать в EPV'),
        h('p', { class: 'muted center lead' }, 'Это не просто таймер, а инструмент самодисциплины'),
        h('div', { class: 'card tint-primary' },
          h('p', {}, 'Каждая отмеченная сессия, привычка и план на день — честный учёт того, что вы на самом деле делаете.'),
          h('p', {}, 'Со временем этот учёт складывается в понятную картину: где вы теряете время, что помогает фокусироваться, а что мешает. Регулярность важнее интенсивности — маленькие ежедневные шаги надёжнее редких рывков.')),
        h('p', { class: 'center strong' }, 'Мы дадим структуру и напоминания — результат создаёте именно вы, шаг за шагом.'),
        btn('Начать', { block: true, onClick: () => ctx.go('onboarding') })),
    };
  };

  // ---------------- onboarding: about you ----------------
  EPV.screens.onboarding = function (ctx) {
    const st = {
      gender: S.gender || EPV.GENDER_OPTIONS[0],
      marital: S.maritalStatus || EPV.MARITAL_OPTIONS[0],
      personality: S.personalityType || EPV.PERSONALITY_OPTIONS[2],
      nightWake: S.nightWake || EPV.NIGHT_WAKE_OPTIONS[0],
      waterUnit: S.waterUnit,
    };
    const root = h('div', { class: 'screen' });

    function next() {
      if (!S.weightKg || !S.heightCm || !S.age) { toast('Заполните вес, рост и возраст'); return; }
      S.gender = st.gender;
      S.maritalStatus = st.marital;
      S.personalityType = st.personality;
      S.nightWake = st.nightWake;
      ctx.go('lifestyle');
    }

    function render() {
      EPV.replace(root, 
        stepperHeader(1, STEP_LABELS),
        hero('🙋', 72),
        h('h1', {}, 'Расскажите о себе и своём дне'),
        h('p', { class: 'muted' }, 'Это поможет предложить точный график дня и советы'),

        h('h3', { class: 'section' }, 'О себе'),
        h('div', { class: 'grid3' },
          field({ label: 'Вес, кг', type: 'number', inputmode: 'decimal', min: 35, max: 220, value: S.weightKg, placeholder: '35–220', onInput: (v) => (S.weightKg = v) }),
          field({ label: 'Рост, см', type: 'number', inputmode: 'numeric', min: 110, max: 220, value: S.heightCm, placeholder: '110–220', onInput: (v) => (S.heightCm = v) }),
          field({ label: 'Возраст', type: 'number', inputmode: 'numeric', min: 10, max: 95, value: S.age, placeholder: '10–95', onInput: (v) => (S.age = v) })),
        select({ label: 'Пол', value: st.gender, options: EPV.GENDER_OPTIONS, onChange: (v) => (st.gender = v) }),
        select({ label: 'Семейное положение', value: st.marital, options: EPV.MARITAL_OPTIONS, onChange: (v) => (st.marital = v) }),

        h('h3', { class: 'section' }, 'Психологический портрет'),
        h('div', { class: 'chips wrap equal' }, EPV.PERSONALITY_OPTIONS.map((o) =>
          chip(o, st.personality === o, () => { st.personality = o; render(); }))),

        h('h3', { class: 'section' }, 'Сон'),
        select({ label: 'Как часто просыпаетесь ночью', value: st.nightWake, options: EPV.NIGHT_WAKE_OPTIONS, onChange: (v) => (st.nightWake = v) }),
        h('div', { class: 'grid2' },
          field({ label: 'Во сколько отбой', type: 'time', value: S.bedTime, onChange: (v) => v && (S.bedTime = v) }),
          field({ label: 'Во сколько подъём', type: 'time', value: S.wakeTime, onChange: (v) => v && (S.wakeTime = v) })),

        h('h3', { class: 'section' }, 'Питание'),
        h('div', { class: 'grid3' },
          field({ label: 'Завтрак', type: 'time', value: S.breakfastTime, onChange: (v) => v && (S.breakfastTime = v) }),
          field({ label: 'Обед', type: 'time', value: S.lunchTime, onChange: (v) => v && (S.lunchTime = v) }),
          field({ label: 'Ужин', type: 'time', value: S.dinnerTime, onChange: (v) => v && (S.dinnerTime = v) })),
        numStepper('Приёмов пищи в день', S.mealsPerDay, 1, 6, (v) => (S.mealsPerDay = v)),
        select({ label: 'Считать воду в', value: st.waterUnit, options: ['Бутылки', 'Чай/кофе'], onChange: (v) => { st.waterUnit = v; S.waterUnit = v; render(); } }),
        numStepper(st.waterUnit === 'Бутылки' ? 'Бутылок воды в день' : 'Чашек чая/кофе в день', S.waterCount, 0, 15, (v) => (S.waterCount = v)),

        h('h3', { class: 'section' }, 'Работа'),
        numStepper('Часов работы в день', S.workHours, 1, 16, (v) => (S.workHours = v)),

        btn('Далее', { block: true, onClick: next }));
    }
    render();
    return { el: root };
  };

  // ---------------- lifestyle questions + schedule variants ----------------
  EPV.screens.lifestyle = function (ctx) {
    const opts = ctx.options || {};
    const saved = S.lifestyleAnswers;
    const answers = {};
    EPV.LIFESTYLE_QUESTIONS.forEach((q) => { answers[q.id] = (saved[q.id] || []).slice(); });
    const st = { showResult: false, variants: [], sel: 0, items: [], compare: false };
    const custom = {};
    const root = h('div', { class: 'screen' });

    const timesOf = () => ({ wakeTime: S.wakeTime, bedTime: S.bedTime, breakfastTime: S.breakfastTime, lunchTime: S.lunchTime, dinnerTime: S.dinnerTime });
    const cloneItems = (items) => items.map((i) => ({ ...i, tips: i.tips.slice() }));

    function generateFirst() {
      S.lifestyleAnswers = answers;
      const first = EPV.generateSchedule(answers, timesOf(), 0);
      st.variants = [first];
      st.sel = 0;
      st.items = cloneItems(first);
      st.compare = false;
      st.showResult = true;
      render();
    }
    function selectVariant(i) {
      st.sel = i;
      st.items = cloneItems(st.variants[i]);
      render();
    }
    function more() {
      if (st.variants.length >= EPV.SCHEDULE_VARIANTS) return;
      st.variants.push(EPV.generateSchedule(answers, timesOf(), st.variants.length));
      st.compare = false;
      selectVariant(st.variants.length - 1);
    }
    function save() {
      S.daySchedule = EPV.scheduleToText(st.items);
      S.isOnboarded = true;
      if (opts.onDone) opts.onDone(); else ctx.go('plansetup');
    }

    function questionBlock(q) {
      const sel = answers[q.id];
      const extra = sel.filter((x) => !q.options.includes(x));
      const toggle = (opt, on) => {
        const i = sel.indexOf(opt);
        if (on && i < 0) sel.push(opt);
        if (!on && i >= 0) sel.splice(i, 1);
      };
      return h('div', { class: 'question' },
        h('h3', {}, q.question),
        q.options.map((opt) => h('label', { class: 'check' },
          h('input', { type: 'checkbox', checked: sel.includes(opt), onChange: (e) => toggle(opt, e.target.checked) }),
          h('span', {}, opt))),
        extra.map((x) => h('div', { class: 'row between custom-answer' }, h('span', {}, '• ' + x),
          EPV.iconBtn('close', 'Удалить ' + x, () => { toggle(x, false); render(); }))),
        h('div', { class: 'row gap' },
          field({ label: 'Свой вариант', value: custom[q.id] || '', cls: 'grow', onInput: (v) => (custom[q.id] = v) }),
          btn('Добавить', { kind: 'outline', small: true, onClick: () => {
            const t = (custom[q.id] || '').trim();
            if (t && !sel.includes(t)) sel.push(t);
            custom[q.id] = '';
            render();
          } })));
    }

    function compareTable() {
      const rows = st.variants[0].length;
      const head = h('tr', {}, h('th', {}, ''), st.variants.map((_, i) => h('th', {}, '№' + (i + 1))));
      const body = [];
      for (let r = 0; r < rows; r++) {
        body.push(h('tr', {}, h('td', {}, st.variants[0][r].title), st.variants.map((v) => h('td', { class: 'mono' }, v[r] ? v[r].time : '—'))));
      }
      return h('div', { class: 'table-wrap' }, h('table', { class: 'compare' }, h('thead', {}, head), h('tbody', {}, body)));
    }

    function render() {
      const header = opts.embedded
        ? h('div', { class: 'row gap' }, EPV.iconBtn('back', 'Назад', opts.onBack), h('h1', {}, 'Опрос о режиме'))
        : [stepperHeader(1, STEP_LABELS)];
      if (!st.showResult) {
        EPV.replace(root, 
          header,
          h('h1', {}, 'Немного о вашем режиме'),
          h('p', { class: 'muted' }, 'Это поможет предложить подходящий график дня'),
          EPV.LIFESTYLE_QUESTIONS.map(questionBlock),
          btn('Сгенерировать график', { block: true, onClick: generateFirst }),
          !opts.embedded ? btn('Назад', { kind: 'ghost', block: true, onClick: () => ctx.go('onboarding') }) : null);
        return;
      }
      EPV.replace(root, 
        header,
        h('h1', {}, 'График дня №' + (st.sel + 1)),
        h('div', { class: 'row gap variant-nav' },
          EPV.iconBtn('left', 'Предыдущий график', () => st.sel > 0 && selectVariant(st.sel - 1), st.sel > 0 ? '' : 'disabled'),
          st.variants.map((_, i) => h('button', { type: 'button', class: 'num-chip' + (i === st.sel ? ' selected' : ''), onClick: () => selectVariant(i) }, String(i + 1))),
          EPV.iconBtn('right', 'Следующий график', () => st.sel < st.variants.length - 1 && selectVariant(st.sel + 1), st.sel < st.variants.length - 1 ? '' : 'disabled')),
        st.compare && st.variants.length > 1
          ? compareTable()
          : [
              h('p', { class: 'muted small' }, 'Подстройте время под себя — остальное менять не обязательно'),
              st.items.map((item, idx) => h('div', { class: 'schedule-row' },
                h('input', { class: 'input time', type: 'time', value: item.time, 'aria-label': 'Время: ' + item.title,
                  onChange: (e) => { if (e.target.value) st.items[idx].time = e.target.value; } }),
                h('div', {}, h('div', { class: 'strong' }, item.title), item.tips.map((t) => h('div', { class: 'tip' }, '• ' + t))))),
            ],
        h('div', { class: 'row gap' },
          btn('Ещё графики (' + (EPV.SCHEDULE_VARIANTS - st.variants.length) + ')', { kind: 'outline', disabled: st.variants.length >= EPV.SCHEDULE_VARIANTS, onClick: more }),
          btn(st.compare ? 'К графику' : 'Сравнить графики', { kind: 'outline', disabled: st.variants.length < 2, onClick: () => { st.compare = !st.compare; render(); } })),
        btn('Сохранить и продолжить', { block: true, onClick: save }),
        btn('Изменить ответы', { kind: 'ghost', block: true, onClick: () => { st.showResult = false; render(); } }));
    }
    render();
    return { el: root };
  };
})(window);
