/* EPV web — tiny DOM helpers and reusable components. */
(function (w) {
  'use strict';
  const EPV = w.EPV;

  function h(tag, props, ...children) {
    const el = document.createElement(tag);
    let deferredValue;
    Object.entries(props || {}).forEach(([k, v]) => {
      if (v === null || v === undefined || v === false) return;
      if (k === 'class') el.className = v;
      else if (k === 'style' && typeof v === 'object') Object.assign(el.style, v);
      else if (k.length > 2 && k.startsWith('on') && typeof v === 'function') el.addEventListener(k.slice(2).toLowerCase(), v);
      else if (k === 'value') deferredValue = v;
      else if (k === 'checked' || k === 'disabled' || k === 'selected' || k === 'readOnly') el[k] = !!v;
      else el.setAttribute(k, v === true ? '' : v);
    });
    (function add(list) {
      list.forEach((c) => {
        if (c === null || c === undefined || c === false) return;
        if (Array.isArray(c)) add(c);
        else el.appendChild(c instanceof Node ? c : document.createTextNode(String(c)));
      });
    })(children);
    if (deferredValue !== undefined) el.value = deferredValue;
    return el;
  }
  EPV.h = h;

  // replaceChildren() that also flattens nested arrays and skips null/false, like h() does.
  function flat(list, out) {
    list.forEach((c) => {
      if (c === null || c === undefined || c === false) return;
      if (Array.isArray(c)) flat(c, out);
      else out.push(c instanceof Node ? c : document.createTextNode(String(c)));
    });
    return out;
  }
  EPV.replace = function (el, ...children) {
    el.replaceChildren(...flat(children, []));
  };

  const ICONS = {
    timer: '<circle cx="12" cy="13" r="8"/><polyline points="12 9 12 13 15 15"/><line x1="9" y1="2" x2="15" y2="2"/>',
    plans: '<polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>',
    stats: '<line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/>',
    settings: '<line x1="4" y1="21" x2="4" y2="14"/><line x1="4" y1="10" x2="4" y2="3"/><line x1="12" y1="21" x2="12" y2="12"/><line x1="12" y1="8" x2="12" y2="3"/><line x1="20" y1="21" x2="20" y2="16"/><line x1="20" y1="12" x2="20" y2="3"/><line x1="1" y1="14" x2="7" y2="14"/><line x1="9" y1="8" x2="15" y2="8"/><line x1="17" y1="16" x2="23" y2="16"/>',
    profile: '<path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>',
    plus: '<line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>',
    back: '<line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/>',
    check: '<polyline points="20 6 9 17 4 12"/>',
    edit: '<path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4z"/>',
    trash: '<polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6M14 11v6"/>',
    close: '<line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>',
    play: '<polygon points="6 4 20 12 6 20 6 4"/>',
    pause: '<line x1="8" y1="5" x2="8" y2="19"/><line x1="16" y1="5" x2="16" y2="19"/>',
    stop: '<rect x="6" y="6" width="12" height="12" rx="1"/>',
    skip: '<polygon points="5 4 15 12 5 20 5 4"/><line x1="19" y1="5" x2="19" y2="19"/>',
    left: '<polyline points="15 18 9 12 15 6"/>',
    right: '<polyline points="9 18 15 12 9 6"/>',
    lock: '<rect x="4" y="11" width="16" height="10" rx="2"/><path d="M8 11V7a4 4 0 0 1 8 0v4"/>',
    minus: '<line x1="5" y1="12" x2="19" y2="12"/>',
  };
  EPV.icon = function (name, size) {
    const s = document.createElement('span');
    s.className = 'ico';
    s.innerHTML = '<svg viewBox="0 0 24 24" width="' + (size || 22) + '" height="' + (size || 22) +
      '" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' +
      (ICONS[name] || '') + '</svg>';
    return s;
  };

  EPV.btn = function (label, opts) {
    const o = opts || {};
    return h('button', {
      type: 'button',
      class: 'btn ' + (o.kind || 'primary') + (o.block ? ' block' : '') + (o.small ? ' sm' : ''),
      disabled: o.disabled,
      onClick: o.onClick,
      title: o.title,
      'aria-label': o.title,
    }, o.icon ? EPV.icon(o.icon, 18) : null, label);
  };

  EPV.iconBtn = function (icon, label, onClick, cls) {
    return h('button', { type: 'button', class: 'icon-btn ' + (cls || ''), 'aria-label': label, title: label, onClick }, EPV.icon(icon, 20));
  };

  EPV.chip = function (label, selected, onClick, extra) {
    return h('button', { type: 'button', class: 'chip' + (selected ? ' selected' : '') + (extra ? ' ' + extra : ''), onClick }, label);
  };

  EPV.field = function (o) {
    const input = h('input', {
      class: 'input', type: o.type || 'text', value: o.value == null ? '' : o.value, placeholder: o.placeholder,
      inputmode: o.inputmode, min: o.min, max: o.max, step: o.step, maxlength: o.maxlength,
      autocomplete: o.autocomplete || 'off',
      onInput: (e) => o.onInput && o.onInput(e.target.value),
      onChange: (e) => o.onChange && o.onChange(e.target.value),
    });
    return h('label', { class: 'field' + (o.cls ? ' ' + o.cls : '') }, o.label ? h('span', { class: 'field-label' }, o.label) : null, input);
  };

  EPV.textarea = function (o) {
    return h('label', { class: 'field' },
      o.label ? h('span', { class: 'field-label' }, o.label) : null,
      h('textarea', {
        class: 'input', rows: o.rows || 3, placeholder: o.placeholder, value: o.value || '',
        onInput: (e) => o.onInput && o.onInput(e.target.value),
      }));
  };

  EPV.select = function (o) {
    const opts = o.options.map((x) => {
      const value = typeof x === 'string' ? x : x.value;
      const label = typeof x === 'string' ? x : x.label;
      return h('option', { value, selected: value === o.value }, label);
    });
    return h('label', { class: 'field' + (o.cls ? ' ' + o.cls : '') },
      o.label ? h('span', { class: 'field-label' }, o.label) : null,
      h('select', { class: 'input', value: o.value, onChange: (e) => o.onChange && o.onChange(e.target.value) }, opts));
  };

  EPV.toggle = function (label, checked, onChange, hint) {
    const input = h('input', { type: 'checkbox', checked: !!checked, onChange: (e) => onChange(e.target.checked) });
    return h('label', { class: 'toggle-row' },
      h('span', { class: 'grow' }, h('span', { class: 'toggle-label' }, label), hint ? h('span', { class: 'hint' }, hint) : null),
      h('span', { class: 'switch' }, input, h('span', { class: 'slider' })));
  };

  EPV.numStepper = function (label, value, min, max, onChange) {
    let v = value;
    const out = h('span', { class: 'num-val' }, String(v));
    const set = (n) => {
      v = Math.max(min, Math.min(max, n));
      out.textContent = String(v);
      onChange(v);
    };
    return h('div', { class: 'num-row' },
      h('span', { class: 'grow' }, label),
      h('div', { class: 'num-ctl' },
        EPV.iconBtn('minus', 'Меньше', () => set(v - 1)),
        out,
        EPV.iconBtn('plus', 'Больше', () => set(v + 1))));
  };

  EPV.tabs = function (items, active, onSelect) {
    return h('div', { class: 'tabs', role: 'tablist' }, items.map((it) =>
      h('button', {
        type: 'button', role: 'tab', class: 'tab' + (it.id === active ? ' active' : ''),
        'aria-selected': it.id === active ? 'true' : 'false', onClick: () => onSelect(it.id),
      }, it.label)));
  };

  EPV.stepperHeader = function (current, labels) {
    const nodes = [];
    labels.forEach((label, i) => {
      const n = i + 1;
      if (i > 0) nodes.push(h('div', { class: 'step-line' + (n <= current ? ' on' : '') }));
      nodes.push(h('div', { class: 'step' + (n < current ? ' done' : '') + (n === current ? ' current' : '') },
        h('div', { class: 'dot' }, n < current ? EPV.icon('check', 16) : String(n)),
        h('div', { class: 'step-label' }, label)));
    });
    return h('div', { class: 'stepper', role: 'list' }, nodes);
  };

  EPV.hero = function (emoji, size) {
    const s = size || 96;
    return h('div', { class: 'hero', style: { width: s + 'px', height: s + 'px', fontSize: Math.round(s * 0.45) + 'px' } }, emoji);
  };

  EPV.progressBar = function (frac, cls) {
    return h('div', { class: 'progress ' + (cls || '') },
      h('div', { class: 'bar', style: { width: Math.round(Math.max(0, Math.min(1, frac)) * 100) + '%' } }));
  };

  EPV.ring = function (size, stroke) {
    const NS = 'http://www.w3.org/2000/svg';
    const r = (size - stroke) / 2;
    const c = 2 * Math.PI * r;
    const svg = document.createElementNS(NS, 'svg');
    svg.setAttribute('viewBox', '0 0 ' + size + ' ' + size);
    svg.setAttribute('class', 'ring-svg');
    const mk = (cls) => {
      const el = document.createElementNS(NS, 'circle');
      el.setAttribute('cx', size / 2); el.setAttribute('cy', size / 2); el.setAttribute('r', r);
      el.setAttribute('fill', 'none'); el.setAttribute('stroke-width', stroke); el.setAttribute('class', cls);
      return el;
    };
    const track = mk('ring-track');
    const bar = mk('ring-bar');
    bar.setAttribute('stroke-linecap', 'round');
    bar.setAttribute('stroke-dasharray', c);
    bar.setAttribute('transform', 'rotate(-90 ' + size / 2 + ' ' + size / 2 + ')');
    svg.append(track, bar);
    return { el: svg, set(frac) { bar.setAttribute('stroke-dashoffset', c * (1 - Math.max(0, Math.min(1, frac)))); } };
  };

  EPV.modal = function (title, build) {
    const root = document.getElementById('modal-root');
    let backdrop;
    const close = () => { if (backdrop) backdrop.remove(); };
    const parts = build(close);
    backdrop = h('div', { class: 'modal-backdrop', onClick: (e) => { if (e.target === backdrop) close(); } },
      h('div', { class: 'modal', role: 'dialog', 'aria-modal': 'true', 'aria-label': title },
        h('h3', {}, title),
        h('div', { class: 'modal-body' }, parts.body),
        parts.actions ? h('div', { class: 'modal-actions' }, parts.actions) : null));
    root.appendChild(backdrop);
    return close;
  };

  EPV.confirm = function (title, text, okLabel, onOk) {
    EPV.modal(title, (close) => ({
      body: h('p', { class: 'muted' }, text),
      actions: [
        EPV.btn('Отмена', { kind: 'ghost', onClick: close }),
        EPV.btn(okLabel || 'Удалить', { kind: 'danger', onClick: () => { close(); onOk(); } }),
      ],
    }));
  };

  let toastTimer;
  EPV.toast = function (msg) {
    let t = document.getElementById('toast');
    if (!t) { t = h('div', { id: 'toast', class: 'toast', role: 'status' }); document.body.appendChild(t); }
    t.textContent = msg;
    t.classList.add('show');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => t.classList.remove('show'), 2200);
  };

  EPV.fmtDateTime = function (ms) {
    return new Date(ms).toLocaleString('ru-RU', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' });
  };
  EPV.mmss = function (sec) {
    const s = Math.max(0, sec);
    return String(Math.floor(s / 60)).padStart(2, '0') + ':' + String(s % 60).padStart(2, '0');
  };
  EPV.daysWord = function (n) {
    const m100 = n % 100, m10 = n % 10;
    if (m100 >= 11 && m100 <= 14) return 'дней';
    if (m10 === 1) return 'день';
    if (m10 >= 2 && m10 <= 4) return 'дня';
    return 'дней';
  };
})(window);
