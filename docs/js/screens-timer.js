/* EPV web — timer screen and morning routine. */
(function (w) {
  'use strict';
  const EPV = w.EPV;
  const S = EPV.S;
  const { h, btn, field, select, chip, textarea, confirm, toast } = EPV;
  EPV.screens = EPV.screens || {};

  // ---------------- timer ----------------
  EPV.screens.timer = function () {
    const T = EPV.Timer;
    const root = h('div', { class: 'screen timer' });
    const refs = {};
    let ring = null;
    let showCommentEditor = false;
    let draftComment = '';
    let suppress = false;

    function editPreset(preset) {
      const p = preset ? { ...preset } : { id: 'preset_' + Date.now(), label: '', workMinutes: 25, restMinutes: 5, comment: '' };
      EPV.modal(preset ? 'Изменить таб' : 'Новый таб', (close) => ({
        body: [
          field({ label: 'Название', value: p.label, onInput: (v) => (p.label = v) }),
          h('div', { class: 'grid2' },
            field({ label: 'Работа, мин', type: 'number', inputmode: 'numeric', value: p.workMinutes, onInput: (v) => (p.workMinutes = parseInt(v, 10) || 25) }),
            field({ label: 'Отдых, мин', type: 'number', inputmode: 'numeric', value: p.restMinutes, onInput: (v) => (p.restMinutes = parseInt(v, 10) || 5) })),
          field({ label: 'Комментарий по умолчанию', value: p.comment, onInput: (v) => (p.comment = v) }),
        ],
        actions: [
          preset ? btn('Удалить', { kind: 'danger', onClick: () => { S.presets = S.presets.filter((x) => x.id !== p.id); close(); render(); } }) : null,
          btn('Отмена', { kind: 'ghost', onClick: close }),
          btn('Сохранить', { onClick: () => {
            if (!p.label.trim()) { toast('Введите название'); return; }
            p.label = p.label.trim();
            p.workMinutes = Math.min(180, Math.max(1, p.workMinutes));
            p.restMinutes = Math.min(180, Math.max(1, p.restMinutes));
            S.presets = preset ? S.presets.map((x) => (x.id === p.id ? p : x)) : [...S.presets, p];
            close();
            render();
          } }),
        ],
      }));
    }

    function minutesRow(label, get, set, key) {
      const num = h('input', { class: 'input num-input', type: 'number', inputmode: 'numeric', min: 1, max: 300, value: get(), 'aria-label': label });
      const range = h('input', { type: 'range', min: 5, max: 100, step: 5, value: Math.min(100, Math.max(5, get())), 'aria-label': label });
      const apply = (v) => { suppress = true; set(v); suppress = false; updateLive(); };
      num.addEventListener('input', () => { const v = parseInt(num.value, 10); if (v >= 1 && v <= 300) { apply(v); range.value = Math.min(100, Math.max(5, v)); } });
      range.addEventListener('input', () => { const v = parseInt(range.value, 10); num.value = v; apply(v); });
      refs[key] = { num, range };
      return h('div', { class: 'minutes-row' }, h('div', { class: 'row between' }, h('span', {}, label + ', мин'), num), range);
    }

    function updateLive() {
      const s = T.s;
      if (refs.time) refs.time.textContent = EPV.mmss(s.secondsLeft);
      if (ring) ring.set(1 - s.secondsLeft / Math.max(1, T.phaseSeconds(s.phase)));
    }

    function render() {
      const s = T.s;
      const work = s.phase === 'work';
      root.className = 'screen timer ' + (work ? 'phase-work' : 'phase-rest');
      ring = EPV.ring(260, 16);
      refs.time = h('div', { class: 'ring-time' }, EPV.mmss(s.secondsLeft));
      const cats = S.categories;

      const comment = showCommentEditor
        ? h('div', { class: 'comment-edit' },
            textarea({ label: 'Комментарий к сессии', value: draftComment, rows: 2, onInput: (v) => (draftComment = v) }),
            h('div', { class: 'row end gap' },
              btn('Отмена', { kind: 'ghost', small: true, onClick: () => { showCommentEditor = false; render(); } }),
              btn('Сохранить', { small: true, onClick: () => { T.setComment(draftComment.trim()); showCommentEditor = false; render(); } })))
        : s.comment
          ? h('div', { class: 'row between comment-view' }, h('span', {}, s.comment),
              EPV.iconBtn('edit', 'Изменить комментарий', () => { draftComment = s.comment; showCommentEditor = true; render(); }))
          : h('div', { class: 'center' }, btn('Добавить комментарий', { kind: 'ghost', small: true, onClick: () => { draftComment = ''; showCommentEditor = true; render(); } }));

      EPV.replace(root, 
        !s.running
          ? h('div', { class: 'chips scroll' },
              S.presets.map((p) => h('span', { class: 'chip-group' },
                h('button', { type: 'button', class: 'chip', onClick: () => { T.setWork(p.workMinutes); T.setRest(p.restMinutes); if (p.comment) T.setComment(p.comment); } }, p.label),
                h('button', { type: 'button', class: 'chip-edit', 'aria-label': 'Изменить ' + p.label, onClick: () => editPreset(p) }, EPV.icon('edit', 14)))),
              chip('+ Добавить', false, () => editPreset(null), 'add'))
          : null,

        s.quote
          ? h('div', { class: 'card tint-tertiary quote' },
              h('p', {}, s.quote),
              EPV.iconBtn('close', 'Скрыть', () => T.dismissQuote()))
          : null,

        h('div', { class: 'phase-label' }, work ? 'Работа' : 'Отдых'),
        cats.length
          ? select({ label: 'Чем занимаетесь', value: cats.includes(s.category) ? s.category : cats[0], options: cats, onChange: (v) => T.setCategory(v), cls: 'category' })
          : null,

        h('div', { class: 'ring-wrap' }, ring.el, h('div', { class: 'ring-center' }, refs.time, h('div', { class: 'ring-sub' }, s.running ? 'идёт' : 'на паузе / готов'))),

        h('div', { class: 'row gap center-row' },
          btn(s.running ? 'Пауза' : 'Старт', { icon: s.running ? 'pause' : 'play', onClick: () => (s.running ? T.pause() : T.start()) }),
          btn('Стоп', { kind: 'outline', icon: 'stop', onClick: () => T.stop() })),

        comment,

        !s.running
          ? h('div', { class: 'card minutes' },
              minutesRow('Время работы', () => S.workMinutes, (v) => T.setWork(v), 'work'),
              minutesRow('Время отдыха', () => S.restMinutes, (v) => T.setRest(v), 'rest'))
          : null);
      updateLive();
    }

    render();
    const unsub = T.subscribe((type) => {
      if (type === 'tick') updateLive();
      else if (!suppress) render();
    });
    return { el: root, destroy: unsub };
  };

  // ---------------- morning routine (overlay) ----------------
  EPV.screens.routine = function (ctx) {
    const todayKey = EPV.dateKey(new Date());
    const st = { tasks: S.routineTasks, date: todayKey, done: S.doneIds(todayKey), draft: S.daySummaries[todayKey] || '', editing: false };
    const root = h('div', { class: 'screen' });
    const WEEKDAYS = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];

    function greeting() {
      const hr = new Date().getHours();
      const base = hr < 5 ? 'Доброй ночи' : hr < 12 ? 'Доброе утро' : hr < 18 ? 'Добрый день' : 'Добрый вечер';
      return S.userName ? base + ', ' + S.userName : base;
    }
    function prettyDate(key) {
      const t = new Date(key + 'T00:00:00').toLocaleDateString('ru-RU', { weekday: 'long', day: 'numeric', month: 'long' });
      return t.charAt(0).toUpperCase() + t.slice(1);
    }
    function switchDate(key) {
      st.date = key;
      st.done = S.doneIds(key);
      st.draft = S.daySummaries[key] || '';
      st.editing = false;
      render();
    }
    function weekStrip() {
      const now = new Date();
      const dow = now.getDay();
      const monday = new Date(now);
      monday.setDate(now.getDate() + (dow === 0 ? -6 : 1 - dow));
      const cells = [];
      for (let i = 0; i < 7; i++) {
        const d = new Date(monday);
        d.setDate(monday.getDate() + i);
        const key = EPV.dateKey(d);
        cells.push(h('button', { type: 'button', class: 'day' + (key === st.date ? ' selected' : '') + (key === todayKey ? ' today' : ''), onClick: () => switchDate(key) },
          h('span', { class: 'dow' }, WEEKDAYS[i]), h('span', { class: 'dnum' }, String(d.getDate()))));
      }
      return h('div', { class: 'week' }, cells);
    }

    function addDialog() {
      let mode = 'lib';
      let title = '';
      let minutes = '';
      const existing = new Set(st.tasks.map((t) => t.id));
      EPV.modal('Новое задание', (close) => {
        const body = h('div', {});
        const draw = () => {
          const avail = EPV.ROUTINE_LIBRARY.filter((t) => !existing.has(t.id));
          EPV.replace(body, 
            h('div', { class: 'chips' }, chip('Готовые', mode === 'lib', () => { mode = 'lib'; draw(); }), chip('Своё', mode === 'custom', () => { mode = 'custom'; draw(); })),
            mode === 'lib'
              ? (avail.length
                  ? h('div', { class: 'pick-list' }, avail.map((t) => h('button', { type: 'button', class: 'pick', onClick: () => { add(t); close(); } }, h('span', {}, t.icon), h('span', {}, t.title))))
                  : h('p', { class: 'muted' }, 'Все готовые задания уже добавлены'))
              : [field({ label: 'Название задания', value: title, onInput: (v) => (title = v) }),
                 field({ label: 'Минут (необязательно)', type: 'number', inputmode: 'numeric', value: minutes, onInput: (v) => (minutes = v) }),
                 btn('Добавить', { block: true, onClick: () => { if (!title.trim()) { toast('Введите название'); return; } add({ id: 'routine_custom_' + Date.now(), title: title.trim(), icon: '✅', durationMinutes: parseInt(minutes, 10) || 0 }); close(); } })]);
        };
        draw();
        return { body, actions: [btn('Закрыть', { kind: 'ghost', onClick: close })] };
      });
    }
    function add(task) {
      st.tasks = [...st.tasks, task];
      S.routineTasks = st.tasks;
      render();
    }

    function row(task, index) {
      const done = st.done.has(task.id);
      const editable = st.date === todayKey;
      const streak = S.streak(task.id, todayKey);
      return h('div', { class: 'routine-row' },
        h('div', { class: 'r-icon c' + (index % 3) }, task.icon),
        h('div', { class: 'grow' },
          h('div', { class: 'strong' + (done ? ' struck' : '') }, task.title),
          h('div', { class: 'muted small' }, [streak > 0 ? '🔥 ' + streak + ' дн. подряд' : '', streak > 0 && task.durationMinutes ? ' · ' : '', task.durationMinutes ? task.durationMinutes + ' мин' : ''])),
        EPV.iconBtn('trash', 'Удалить ' + task.title, () => confirm('Удалить задание?', '«' + task.title + '» будет убрано из рутины. История выполнения сохранится.', 'Удалить', () => {
          st.tasks = st.tasks.filter((t) => t.id !== task.id);
          S.routineTasks = st.tasks;
          render();
        }), 'muted-btn'),
        h('button', { type: 'button', class: 'check-circle' + (done ? ' on' : ''), disabled: !editable, 'aria-label': done ? 'Отменить' : 'Готово', 'aria-pressed': done ? 'true' : 'false',
          onClick: () => { S.setRoutineDone(st.date, task.id, !done); st.done = S.doneIds(st.date); render(); } }, done ? EPV.icon('check', 18) : null));
    }

    function summaryCard() {
      const saved = S.daySummaries[st.date] || '';
      return h('div', { class: 'card tint-tertiary' },
        h('div', { class: 'strong' }, '🎉 Рутина выполнена — итог дня'),
        !st.editing && saved
          ? h('div', { class: 'row between top' }, h('p', {}, saved), EPV.iconBtn('edit', 'Изменить итог', () => { st.draft = saved; st.editing = true; render(); }))
          : [textarea({ label: 'Как прошёл день?', value: st.draft, rows: 3, onInput: (v) => (st.draft = v) }),
             h('div', { class: 'row end gap' },
               saved ? btn('Отмена', { kind: 'ghost', small: true, onClick: () => { st.editing = false; render(); } }) : null,
               btn('Сохранить итог', { small: true, onClick: () => {
                 const all = S.daySummaries;
                 if (st.draft.trim()) all[st.date] = st.draft.trim(); else delete all[st.date];
                 S.daySummaries = all;
                 st.editing = false;
                 render();
               } }))]);
    }

    function render() {
      const doneCount = st.tasks.filter((t) => st.done.has(t.id)).length;
      const allDone = st.tasks.length > 0 && doneCount === st.tasks.length;
      EPV.replace(root, 
        h('div', { class: 'row gap' }, EPV.iconBtn('back', 'Назад', ctx.closeOverlay),
          h('div', {}, h('h1', {}, greeting()), h('div', { class: 'muted' }, prettyDate(st.date)))),
        weekStrip(),
        st.tasks.length
          ? h('div', { class: 'card tint-secondary' }, h('div', { class: 'strong' }, 'Выполнено ' + doneCount + ' из ' + st.tasks.length),
              EPV.progressBar(doneCount / st.tasks.length, 'coral'))
          : null,
        h('h3', { class: 'section' }, 'Утренняя рутина'),
        st.tasks.map(row),
        h('button', { type: 'button', class: 'add-row', onClick: addDialog }, '+ Добавить задание'),
        allDone ? summaryCard() : null);
    }
    render();
    return { el: root };
  };
})(window);
