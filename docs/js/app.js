/* EPV web — router, app shell (top bar, bottom nav, mini timer bars) and bootstrap. */
(function (w) {
  'use strict';
  const EPV = w.EPV;
  const S = EPV.S;
  const { h } = EPV;

  const app = { route: null, tab: 'timer', overlay: null, current: null };
  const rootEl = document.getElementById('app');
  const NAV = [
    { id: 'timer', label: 'Таймер', icon: 'timer' },
    { id: 'plans', label: 'Планы', icon: 'plans' },
    { id: 'stats', label: 'Статистика', icon: 'stats' },
    { id: 'settings', label: 'Настройки', icon: 'settings' },
    { id: 'profile', label: 'Профиль', icon: 'profile' },
  ];

  const ctx = {
    go(route) { app.route = route; app.overlay = null; render(); w.scrollTo(0, 0); },
    setTab(tab) { app.tab = tab; app.overlay = null; render(); w.scrollTo(0, 0); },
    openOverlay(name) { app.overlay = name; render(); w.scrollTo(0, 0); },
    closeOverlay() { app.overlay = null; render(); },
  };

  // ---------- mini bars (running timer / running plan, visible on every tab) ----------
  const bars = h('div', { class: 'mini-bars' });
  let barsKey = '';
  let timeEl = null;
  let runEl = null;

  function updateBars() {
    const T = EPV.Timer;
    const R = EPV.Runner;
    // no need to repeat a running timer/plan on the tab that already shows it in full
    const showT = !!T.s && T.isActive() && !(app.tab === 'timer' && !app.overlay);
    const showR = R.isActive() && !(app.tab === 'plans' && EPV.plansTab === 'plan' && !app.overlay);
    const key = [showT, showT && T.s.phase, showT && T.s.running, showR, showR && R.st.index, showR && R.st.paused].join('|');
    if (key !== barsKey) {
      barsKey = key;
      timeEl = null;
      runEl = null;
      const kids = [];
      if (showT) {
        const work = T.s.phase === 'work';
        timeEl = h('span', { class: 'mono strong' }, EPV.mmss(T.s.secondsLeft));
        kids.push(h('button', { type: 'button', class: 'mini ' + (work ? 'work' : 'rest'), onClick: () => ctx.setTab('timer') },
          h('span', { class: 'row gap' }, EPV.icon(T.s.running ? 'play' : 'pause', 16), h('span', { class: 'strong' }, work ? 'Работа' : 'Отдых')), timeEl));
      }
      if (showR) {
        const t = R.template();
        const task = t && t.tasks[R.st.index];
        runEl = h('span', { class: 'mono strong' }, EPV.mmss(R.left()));
        kids.push(h('button', { type: 'button', class: 'mini plan', onClick: () => { EPV.plansTab = 'plan'; ctx.setTab('plans'); } },
          h('span', { class: 'row gap' }, EPV.icon(R.st.paused ? 'pause' : 'play', 16), h('span', { class: 'strong ellipsis' }, (t ? t.name : 'План') + (task ? ': ' + task.title : ''))), runEl));
      }
      EPV.replace(bars, kids);
    } else {
      if (timeEl) timeEl.textContent = EPV.mmss(T.s.secondsLeft);
      if (runEl) runEl.textContent = EPV.mmss(R.left());
    }
  }

  // ---------- "+" menu ----------
  function openPlusMenu(anchor) {
    const existing = document.getElementById('plus-menu');
    if (existing) { existing.remove(); return; }
    const menu = h('div', { id: 'plus-menu', class: 'menu', role: 'menu' },
      h('button', { type: 'button', role: 'menuitem', onClick: () => { menu.remove(); ctx.openOverlay('routine'); } }, '🌅 Утренняя рутина'),
      h('button', { type: 'button', role: 'menuitem', onClick: () => { menu.remove(); EPV.plansTab = 'plan'; ctx.setTab('plans'); } }, '🗓️ План на день'));
    document.body.appendChild(menu);
    const close = (e) => { if (!menu.contains(e.target) && e.target !== anchor) { menu.remove(); document.removeEventListener('click', close, true); } };
    setTimeout(() => document.addEventListener('click', close, true), 0);
  }

  // ---------- render ----------
  function overlayScreen(name) {
    if (name === 'lifestyle') {
      const c = Object.create(ctx);
      c.options = { embedded: true, onBack: ctx.closeOverlay, onDone: ctx.closeOverlay };
      return EPV.screens.lifestyle(c);
    }
    return EPV.screens[name](ctx);
  }

  function render() {
    if (app.current && app.current.destroy) app.current.destroy();
    app.current = null;
    const menu = document.getElementById('plus-menu');
    if (menu) menu.remove();
    document.getElementById('modal-root').replaceChildren();

    if (app.route !== 'main') {
      EPV.replace(rootEl, h('div', { class: 'page' }, (app.current = EPV.screens[app.route](ctx)).el));
      return;
    }
    app.current = app.overlay ? overlayScreen(app.overlay) : EPV.screens[app.tab](ctx);
    updateBars();
    const plusBtn = h('button', { type: 'button', class: 'icon-btn plus', 'aria-label': 'Добавить', 'aria-haspopup': 'menu', onClick: () => openPlusMenu(plusBtn) }, EPV.icon('plus', 22));
    EPV.replace(rootEl, 
      h('div', { class: 'shell' },
        h('header', { class: 'topbar' }, h('div', { class: 'logo' }, 'EPV'), plusBtn),
        bars,
        h('main', { class: 'content' }, app.current.el),
        h('nav', { class: 'bottom-nav', 'aria-label': 'Основная навигация' }, NAV.map((n) =>
          h('button', { type: 'button', class: 'nav-item' + (n.id === app.tab && !app.overlay ? ' active' : ''), 'aria-current': n.id === app.tab && !app.overlay ? 'page' : null, onClick: () => ctx.setTab(n.id) },
            EPV.icon(n.icon, 22), h('span', {}, n.label))))));
  }

  // ---------- boot ----------
  function initialRoute() {
    if (!S.isRegistered || !S.isLoggedIn) return 'auth';
    if (!S.isOnboarded) return 'intro';
    return 'main';
  }

  EPV.Timer.init();
  EPV.Timer.subscribe(() => { if (app.route === 'main') updateBars(); });
  EPV.Runner.subscribe(() => { if (app.route === 'main') updateBars(); });
  app.route = initialRoute();
  render();

  w.addEventListener('beforeinstallprompt', (e) => { e.preventDefault(); EPV.installPrompt = e; });
  if ('serviceWorker' in navigator && /^https?:$/.test(location.protocol)) {
    w.addEventListener('load', () => navigator.serviceWorker.register('sw.js').catch(() => {}));
  }
  EPV.app = { ctx, render, state: app };
})(window);
