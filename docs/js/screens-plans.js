/* EPV web — plans tab: history, plan templates builder and the auto-chaining runner. */
(function (w) {
  'use strict';
  const EPV = w.EPV;
  const S = EPV.S;
  const R = EPV.Runner;
  const { h, btn, field, chip, textarea, confirm, toast, stepperHeader, hero } = EPV;
  EPV.screens = EPV.screens || {};
  const STEP_LABELS = ['О себе', 'План на день', 'Готово'];

  // ---------------- day plan (templates + runner) ----------------
  function dayPlan(opts) {
    const setup = !!opts.setup;
    if (R.st.completed) R.stop();
    const st = {
      templates: S.planTemplates,
      activeId: S.activePlanId,
      building: false,
      editingId: null,
      draft: [],
      name: '',
      customTitle: '',
      customMin: '10',
    };
    if (R.st.templateId && st.templates.some((t) => t.id === R.st.templateId)) st.activeId = R.st.templateId;
    if (!st.templates.some((t) => t.id === st.activeId)) st.activeId = st.templates[0] ? st.templates[0].id : null;
    st.building = st.templates.length === 0;

    const root = h('div', { class: 'dayplan' });
    const live = {};

    const active = () => st.templates.find((t) => t.id === st.activeId) || null;
    const runnerHere = () => R.st.templateId === st.activeId;

    function persist(list) {
      st.templates = list;
      S.planTemplates = list;
    }
    function select(id) {
      if (R.st.templateId && R.st.templateId !== id) R.stop();
      st.activeId = id;
      S.activePlanId = id;
      render();
    }
    function startBuild(template) {
      st.editingId = template ? template.id : null;
      st.draft = template ? template.tasks.map((t) => ({ ...t })) : [];
      st.name = template ? template.name : '';
      st.customTitle = '';
      st.customMin = '10';
      st.building = true;
      render();
    }
    function saveDraft() {
      if (!st.draft.length) return;
      const name = st.name.trim() || 'Список ' + (st.templates.length + 1);
      const id = st.editingId || 'plan_' + Date.now();
      const tpl = { id, name, tasks: st.draft.map((t) => ({ ...t, durationMinutes: Math.min(240, Math.max(1, parseInt(t.durationMinutes, 10) || 1)) })) };
      persist(st.editingId ? st.templates.map((t) => (t.id === id ? tpl : t)) : [...st.templates, tpl]);
      if (R.st.templateId === id) R.stop();
      st.activeId = id;
      S.activePlanId = id;
      st.building = false;
      render();
    }

    // ---- builder ----
    function builder() {
      const used = new Set(st.draft.map((t) => t.id));
      const avail = EPV.PLAN_LIBRARY.filter((t) => !used.has(t.id));
      return h('div', { class: 'builder' },
        h('h3', { class: 'section' }, 'Готовые действия'),
        h('div', { class: 'chips wrap' }, avail.map((t) => chip(t.title + ' · ' + t.durationMinutes + 'м', false, () => { st.draft.push({ ...t }); render(); }))),
        h('h3', { class: 'section' }, 'Своё действие'),
        h('div', { class: 'row gap end-align' },
          field({ label: 'Действие', value: st.customTitle, cls: 'grow', onInput: (v) => (st.customTitle = v) }),
          field({ label: 'Мин', type: 'number', inputmode: 'numeric', value: st.customMin, cls: 'w80', onInput: (v) => (st.customMin = v) }),
          btn('+', { kind: 'outline', title: 'Добавить', onClick: () => {
            if (!st.customTitle.trim()) { toast('Введите название действия'); return; }
            st.draft.push({ id: 'plantask_custom_' + Date.now(), title: st.customTitle.trim(), durationMinutes: Math.min(240, Math.max(1, parseInt(st.customMin, 10) || 10)) });
            st.customTitle = '';
            st.customMin = '10';
            render();
          } })),
        st.draft.length ? h('h3', { class: 'section' }, 'Список действий') : null,
        st.draft.map((t, i) => h('div', { class: 'draft-row' },
          h('span', { class: 'idx' }, (i + 1) + '.'),
          h('span', { class: 'grow' }, t.title),
          h('input', { class: 'input num-input', type: 'number', inputmode: 'numeric', min: 1, max: 240, value: t.durationMinutes, 'aria-label': 'Минуты: ' + t.title,
            onInput: (e) => { const v = parseInt(e.target.value, 10); if (v >= 1) t.durationMinutes = Math.min(240, v); } }),
          EPV.iconBtn('close', 'Удалить ' + t.title, () => { st.draft.splice(i, 1); render(); }))),
        field({ label: 'Название списка', value: st.name, onInput: (v) => (st.name = v) }),
        h('div', { class: 'row gap' },
          st.templates.length ? btn('Отмена', { kind: 'outline', onClick: () => { st.building = false; render(); } }) : null,
          btn('Сохранить список', { disabled: !st.draft.length, onClick: saveDraft })));
    }

    // ---- active template view ----
    function taskRow(task, index) {
      const here = runnerHere();
      const isCurrent = here && R.st.index === index;
      const isDone = here && (R.st.completed || R.st.index > index);
      const row = h('div', { class: 'plan-task' + (isCurrent ? ' current' : '') + (isDone ? ' done' : '') },
        h('span', { class: 'dot' }),
        h('div', { class: 'grow' },
          h('div', { class: isCurrent ? 'strong' : '' }, task.title),
          isCurrent
            ? [h('div', { class: 'plan-time' }, EPV.mmss(R.left())), (live.bar = EPV.progressBar(0))]
            : h('div', { class: 'muted small' }, task.durationMinutes + ' мин')));
      if (isCurrent) live.time = row.querySelector('.plan-time');
      return row;
    }

    function updateLive() {
      const t = active();
      if (!t || !R.isActive() || !runnerHere()) return;
      const total = Math.max(1, t.tasks[R.st.index].durationMinutes * 60);
      if (live.time) live.time.textContent = EPV.mmss(R.left());
      if (live.bar) live.bar.firstChild.style.width = Math.round((1 - R.left() / total) * 100) + '%';
    }

    function runControls(t) {
      if (runnerHere() && R.st.completed) {
        return h('div', { class: 'card tint-tertiary' }, h('div', { class: 'strong' }, '🎉 Все действия выполнены!'),
          btn('Начать заново', { onClick: () => R.start(t) }));
      }
      if (runnerHere() && R.isActive()) {
        return h('div', { class: 'row gap' },
          btn(R.st.paused ? 'Продолжить' : 'Пауза', { kind: 'outline', icon: R.st.paused ? 'play' : 'pause', onClick: () => (R.st.paused ? R.resume() : R.pause()) }),
          btn('Дальше', { kind: 'outline', icon: 'skip', onClick: () => R.skip() }),
          btn('Стоп', { kind: 'outline', icon: 'stop', onClick: () => R.stop() }));
      }
      return btn('Начать выполнение', { block: true, disabled: !t.tasks.length, onClick: () => R.start(t) });
    }

    function activeView(t) {
      return h('div', {},
        h('div', { class: 'row between' },
          h('h2', {}, t.name),
          h('div', { class: 'row' },
            EPV.iconBtn('edit', 'Изменить список', () => startBuild(t)),
            EPV.iconBtn('trash', 'Удалить список', () => confirm('Удалить список?', '«' + t.name + '» будет удалён без возможности восстановления.', 'Удалить', () => {
              if (R.st.templateId === t.id) R.stop();
              persist(st.templates.filter((x) => x.id !== t.id));
              st.activeId = st.templates[0] ? st.templates[0].id : null;
              S.activePlanId = st.activeId;
              if (!st.templates.length) st.building = true;
              render();
            })))),
        h('div', { class: 'plan-tasks' }, t.tasks.map(taskRow)),
        runControls(t));
    }

    function render() {
      const t = active();
      const step = st.templates.length === 0 || st.building ? 2 : 3;
      EPV.replace(root, 
        setup ? [stepperHeader(step, STEP_LABELS), hero(step === 2 ? '🗓️' : '✨', 72),
                 h('h1', {}, 'План на день'),
                 h('p', { class: 'muted' }, 'Соберите список действий на утро или на день — с таймером на каждое. Они запускаются одно за другим автоматически.')]
          : null,
        !st.building && st.templates.length
          ? h('div', { class: 'chips scroll' },
              st.templates.map((x) => chip(x.name, x.id === st.activeId, () => select(x.id))),
              chip('+ Новый список', false, () => startBuild(null), 'add'))
          : null,
        st.building ? builder()
          : t ? activeView(t)
          : [h('p', { class: 'muted' }, 'У вас пока нет списков дел. Создайте первый — из готовых действий или своих.'), btn('Создать список', { onClick: () => startBuild(null) })],
        setup ? btn('Готово, начать пользоваться', { block: true, disabled: !st.templates.length || st.building, onClick: opts.onDone }) : null);
      updateLive();
    }

    render();
    const unsub = R.subscribe((type) => { if (type === 'tick') updateLive(); else render(); });
    return { el: root, destroy: unsub };
  }

  EPV.screens.plansetup = function (ctx) {
    const inner = dayPlan({ setup: true, onDone: () => ctx.go('main') });
    return { el: h('div', { class: 'screen' }, inner.el), destroy: inner.destroy };
  };

  // ---------------- history ----------------
  function historyTab() {
    const st = { plans: 20, sessions: 20 };
    const root = h('div', {});

    function planRow(e) {
      return h('div', { class: 'hist-row' },
        h('div', { class: 'row between' }, h('div', { class: 'strong' }, '🎉 ' + e.templateName),
          h('div', { class: 'row' }, h('span', { class: 'muted small' }, EPV.fmtDateTime(e.completedAtMillis)),
            EPV.iconBtn('trash', 'Удалить запись', () => { S.deletePlanHistory(e.id); render(); }, 'muted-btn'))),
        h('div', { class: 'accent' }, e.taskCount + ' действий · ' + e.totalMinutes + ' мин'),
        field({ label: 'Заметка', value: e.note || '', onInput: (v) => S.updatePlanHistoryNote(e.id, v) }));
    }
    function sessionRow(e) {
      return h('div', { class: 'hist-row' },
        h('div', { class: 'row between' },
          h('div', { class: 'strong' }, (e.phase === 'WORK' ? 'Работа' : 'Отдых') + (e.interrupted ? ' (прервано)' : '')),
          h('div', { class: 'row' }, h('span', { class: 'muted small' }, EPV.fmtDateTime(e.startTimeMillis)),
            EPV.iconBtn('trash', 'Удалить запись', () => { S.deleteHistory(e.id); render(); }, 'muted-btn'))),
        h('div', { class: 'row gap' }, h('span', {}, Math.floor(e.durationSeconds / 60) + ':' + String(e.durationSeconds % 60).padStart(2, '0')),
          e.category ? h('span', { class: 'accent' }, '• ' + e.category) : null),
        e.quote ? h('div', { class: 'quote-line' }, e.quote) : null,
        field({ label: 'Комментарий', value: e.comment || '', onInput: (v) => S.updateHistoryComment(e.id, v) }));
    }

    function render() {
      const plans = S.planHistory;
      const sessions = S.history;
      EPV.replace(root, 
        h('h3', { class: 'section' }, 'Завершённые планы'),
        plans.length
          ? [plans.slice(0, st.plans).map(planRow), plans.length > st.plans ? btn('Показать ещё', { kind: 'ghost', block: true, onClick: () => { st.plans += 20; render(); } }) : null]
          : h('p', { class: 'muted' }, 'Здесь появятся планы на день, которые вы выполнили полностью'),
        h('h3', { class: 'section' }, 'Сессии таймера'),
        sessions.length
          ? [sessions.slice(0, st.sessions).map(sessionRow), sessions.length > st.sessions ? btn('Показать ещё', { kind: 'ghost', block: true, onClick: () => { st.sessions += 20; render(); } }) : null]
          : h('p', { class: 'muted' }, 'Пока пусто — здесь появится история ваших сессий работы и отдыха'));
    }
    render();
    return { el: root };
  }

  // ---------------- plans tab wrapper ----------------
  EPV.plansTab = 'history';
  EPV.screens.plans = function () {
    const root = h('div', { class: 'screen' });
    let inner = null;
    function render() {
      if (inner && inner.destroy) inner.destroy();
      inner = EPV.plansTab === 'history' ? historyTab() : dayPlan({ setup: false });
      EPV.replace(root, 
        h('h1', {}, 'Планы'),
        EPV.tabs([{ id: 'history', label: 'История' }, { id: 'plan', label: 'План на день' }], EPV.plansTab, (id) => { EPV.plansTab = id; render(); }),
        inner.el);
    }
    render();
    return { el: root, destroy: () => inner && inner.destroy && inner.destroy() };
  };
})(window);
