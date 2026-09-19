/* EPV web — timer engine, plan runner, sound/voice/notifications. */
(function (w) {
  'use strict';
  const EPV = w.EPV;
  const S = EPV.S;
  const pick = (a) => a[Math.floor(Math.random() * a.length)];

  // ---------------- audio / alerts ----------------
  let audioCtx = null;
  function unlockAudio() {
    try {
      if (!audioCtx) {
        const AC = w.AudioContext || w.webkitAudioContext;
        if (AC) audioCtx = new AC();
      }
      if (audioCtx && audioCtx.state === 'suspended') audioCtx.resume();
    } catch (e) { /* ignore */ }
  }
  function beepOnce() {
    if (!audioCtx) return;
    const t = audioCtx.currentTime;
    const o = audioCtx.createOscillator();
    const g = audioCtx.createGain();
    o.type = 'sine';
    o.frequency.value = 880;
    g.gain.setValueAtTime(0.0001, t);
    g.gain.exponentialRampToValueAtTime(0.35, t + 0.02);
    g.gain.exponentialRampToValueAtTime(0.0001, t + 0.45);
    o.connect(g);
    g.connect(audioCtx.destination);
    o.start(t);
    o.stop(t + 0.5);
  }
  function alertUser(times) {
    for (let i = 0; i < times; i++) {
      setTimeout(() => {
        if (S.vibration && navigator.vibrate) navigator.vibrate(300);
        if (S.sound) beepOnce();
      }, i * 1200);
    }
  }

  // ---------------- voice ----------------
  let nextFemale = true;
  const FEMALE = ['female', 'irina', 'milena', 'svetlana', 'katya', 'tatyana', 'anna', 'samantha', 'zira', 'aria', 'jenny', 'sonia', 'victoria', 'karen', 'susan', 'allison', 'ava', 'tessa', 'nicky', 'kate'];
  const MALE = ['male', 'pavel', 'yuri', 'dmitry', 'maxim', 'david', 'mark', 'guy', 'alex', 'daniel', 'fred', 'tom', 'aaron', 'nathan', 'evan', 'arthur', 'ryan', 'george'];
  const hasAny = (name, list) => list.some((n) => name.includes(n));

  function pickVoice(lang, female) {
    if (!('speechSynthesis' in w)) return null;
    const voices = (speechSynthesis.getVoices() || []).filter((v) => v.lang.toLowerCase().startsWith(lang));
    if (!voices.length) return null;
    const mine = female ? FEMALE : MALE;
    const other = female ? MALE : FEMALE;
    let best = null;
    let bestScore = -99;
    voices.forEach((v) => {
      const n = v.name.toLowerCase();
      let score = 0;
      if (/natural|neural|online/.test(n)) score += 3;
      if (hasAny(n, mine) && !hasAny(n, other)) score += 2;
      if (hasAny(n, other) && !hasAny(n, mine)) score -= 2;
      if (!v.localService) score += 1;
      if (score > bestScore) { best = v; bestScore = score; }
    });
    return best;
  }

  function speakNative(text, female, lang) {
    if (!('speechSynthesis' in w)) return;
    try {
      speechSynthesis.cancel();
      const u = new SpeechSynthesisUtterance(text);
      u.lang = lang === 'en' ? 'en-US' : 'ru-RU';
      const v = pickVoice(lang, female);
      if (v) u.voice = v;
      u.pitch = female ? 1.12 : 0.86;
      u.rate = 1;
      speechSynthesis.speak(u);
    } catch (e) { /* ignore */ }
  }

  async function speakEleven(text, female, fallback) {
    try {
      const id = female ? S.elevenVoiceFemale : S.elevenVoiceMale;
      const res = await fetch('https://api.elevenlabs.io/v1/text-to-speech/' + encodeURIComponent(id), {
        method: 'POST',
        headers: { 'xi-api-key': S.elevenKey, 'Content-Type': 'application/json', Accept: 'audio/mpeg' },
        body: JSON.stringify({ text, model_id: 'eleven_multilingual_v2', voice_settings: { stability: 0.5, similarity_boost: 0.75 } }),
      });
      if (!res.ok) throw new Error('HTTP ' + res.status);
      const url = URL.createObjectURL(await res.blob());
      const audio = new Audio(url);
      audio.onended = () => URL.revokeObjectURL(url);
      await audio.play();
    } catch (e) {
      fallback();
    }
  }

  function speak(text) {
    const lang = S.voiceLanguage === 'English' ? 'en' : 'ru';
    const female = nextFemale;
    nextFemale = !nextFemale;
    if (S.elevenEnabled && S.elevenKey) speakEleven(text, female, () => speakNative(text, female, lang));
    else speakNative(text, female, lang);
  }
  const phrases = () => EPV.PHRASES[S.voiceLanguage === 'English' ? 'en' : 'ru'];

  // ---------------- notifications / wake lock ----------------
  function notify(title, body) {
    if (!S.notifications || !('Notification' in w) || Notification.permission !== 'granted' || !document.hidden) return;
    try {
      if (navigator.serviceWorker && navigator.serviceWorker.controller) {
        navigator.serviceWorker.ready.then((r) => r.showNotification(title, { body, icon: 'icons/icon-192.png', tag: 'epv' }));
      } else {
        new Notification(title, { body });
      }
    } catch (e) { /* ignore */ }
  }
  EPV.requestNotifications = async function () {
    if (!('Notification' in w)) return false;
    try { return (await Notification.requestPermission()) === 'granted'; } catch (e) { return false; }
  };

  let wakeLock = null;
  async function setWake(on) {
    try {
      if (on && S.keepScreenOn && 'wakeLock' in navigator) {
        if (!wakeLock) {
          wakeLock = await navigator.wakeLock.request('screen');
          wakeLock.addEventListener('release', () => { wakeLock = null; });
        }
      } else if (wakeLock) {
        await wakeLock.release();
        wakeLock = null;
      }
    } catch (e) { /* ignore */ }
  }

  // ---------------- work / rest timer ----------------
  const T = { s: null, iv: null, listeners: new Set() };
  const phaseSeconds = (phase) => (phase === 'work' ? S.workMinutes : S.restMinutes) * 60;
  const leftNow = () => Math.max(0, Math.ceil((T.s.endAt - Date.now()) / 1000));
  const persist = () => { S.timerState = T.s; };
  const emit = (type) => T.listeners.forEach((fn) => { try { fn(type, T.s); } catch (e) { console.error(e); } });

  T.subscribe = (fn) => { T.listeners.add(fn); return () => T.listeners.delete(fn); };
  T.phaseSeconds = phaseSeconds;
  T.isFresh = () => !T.s.running && T.s.secondsLeft === phaseSeconds(T.s.phase);
  T.isActive = () => T.s.running || !T.isFresh();

  function defaultState() {
    return { phase: 'work', running: false, secondsLeft: phaseSeconds('work'), endAt: null, sessionStart: null, category: S.categories[0] || '', comment: '', quote: null, announced: false };
  }

  function updateTitle() {
    const s = T.s;
    document.title = s.running ? EPV.mmss(s.secondsLeft) + ' · ' + (s.phase === 'work' ? 'Работа' : 'Отдых') + ' — EPV' : 'EPV';
  }

  function startTick() {
    if (T.iv) return;
    T.iv = setInterval(tick, 250);
  }
  function stopTick() {
    clearInterval(T.iv);
    T.iv = null;
  }

  function flush(interrupted, quote) {
    const s = T.s;
    if (!s.sessionStart) return;
    const planned = phaseSeconds(s.phase);
    const elapsed = interrupted ? Math.max(0, planned - s.secondsLeft) : planned;
    S.addHistory({
      id: s.sessionStart, phase: s.phase === 'work' ? 'WORK' : 'REST', startTimeMillis: s.sessionStart,
      durationSeconds: elapsed, interrupted, comment: s.comment, category: s.category, quote: quote || '',
    });
    s.sessionStart = null;
    s.comment = '';
  }

  function maybeAnnounce() {
    const s = T.s;
    if (s.announced || !S.voiceEnabled) return;
    const lead = S.voiceUnit === 'Минуты' ? S.voiceLead * 60 : S.voiceLead;
    if (lead <= 0 || phaseSeconds(s.phase) <= lead) return;
    if (s.secondsLeft <= lead) {
      s.announced = true;
      const p = phrases();
      speak(s.phase === 'work' ? p.soonRest : p.soonWork);
    }
  }

  function onPhaseFinished() {
    const s = T.s;
    const finished = s.phase;
    const quote = finished === 'work' ? pick(EPV.QUOTES) : '';
    s.secondsLeft = 0;
    flush(false, quote);
    s.phase = finished === 'work' ? 'rest' : 'work';
    s.secondsLeft = phaseSeconds(s.phase);
    s.announced = false;
    if (quote) s.quote = quote;
    s.sessionStart = Date.now();
    s.endAt = Date.now() + s.secondsLeft * 1000;
    s.running = true;
    alertUser(finished === 'work' ? 3 : 1);
    if (S.voiceEnabled) {
      const p = phrases();
      speak(pick(finished === 'work' ? p.workDone : p.restDone));
    }
    notify(finished === 'work' ? 'Время отдыха' : 'Время работать', finished === 'work' ? 'Рабочий блок завершён' : 'Перерыв закончился');
    persist();
    updateTitle();
    emit('phase');
  }

  function tick() {
    const s = T.s;
    if (!s.running) return;
    s.secondsLeft = leftNow();
    maybeAnnounce();
    if (s.secondsLeft <= 0) onPhaseFinished();
    else { updateTitle(); emit('tick'); }
  }

  T.start = function () {
    const s = T.s;
    if (s.running) return;
    unlockAudio();
    if (!s.sessionStart) s.sessionStart = Date.now();
    s.running = true;
    s.endAt = Date.now() + s.secondsLeft * 1000;
    startTick();
    setWake(true);
    persist();
    updateTitle();
    emit('state');
  };
  T.pause = function () {
    const s = T.s;
    if (!s.running) return;
    s.secondsLeft = leftNow();
    s.running = false;
    s.endAt = null;
    stopTick();
    setWake(false);
    persist();
    updateTitle();
    emit('state');
  };
  T.stop = function () {
    const s = T.s;
    if (s.running) s.secondsLeft = leftNow();
    flush(true, '');
    s.running = false;
    s.phase = 'work';
    s.secondsLeft = phaseSeconds('work');
    s.endAt = null;
    s.announced = false;
    stopTick();
    setWake(false);
    persist();
    updateTitle();
    emit('state');
  };
  T.setWork = function (m) {
    S.workMinutes = m;
    if (!T.s.running && T.s.phase === 'work') T.s.secondsLeft = m * 60;
    persist();
    emit('state');
  };
  T.setRest = function (m) {
    S.restMinutes = m;
    if (!T.s.running && T.s.phase === 'rest') T.s.secondsLeft = m * 60;
    persist();
    emit('state');
  };
  T.setComment = (c) => { T.s.comment = c; persist(); emit('state'); };
  T.setCategory = (c) => { T.s.category = c; persist(); };
  T.dismissQuote = () => { T.s.quote = null; persist(); emit('state'); };

  T.init = function () {
    const st = defaultState();
    const saved = S.timerState;
    if (saved && typeof saved === 'object') Object.assign(st, saved);
    if (!S.categories.includes(st.category)) st.category = S.categories[0] || '';
    T.s = st;
    if (st.running) {
      if (st.endAt && st.endAt > Date.now()) {
        st.secondsLeft = leftNow();
        startTick();
        setWake(true);
      } else {
        const finished = st.phase;
        st.secondsLeft = 0;
        flush(false, finished === 'work' ? pick(EPV.QUOTES) : '');
        st.phase = finished === 'work' ? 'rest' : 'work';
        st.secondsLeft = phaseSeconds(st.phase);
        st.running = false;
        st.endAt = null;
        st.sessionStart = null;
      }
    } else if (!st.secondsLeft || st.secondsLeft > phaseSeconds(st.phase)) {
      st.secondsLeft = phaseSeconds(st.phase);
    }
    persist();
    updateTitle();
  };

  document.addEventListener('visibilitychange', () => {
    if (!document.hidden && T.s && T.s.running) { tick(); setWake(true); }
  });

  // ---------------- plan runner (tasks run one after another, automatically) ----------------
  const R = { st: { templateId: null, index: -1, paused: false, endAt: null, remaining: 0, completed: false }, iv: null, listeners: new Set() };
  const remit = (type) => R.listeners.forEach((fn) => { try { fn(type, R.st); } catch (e) { console.error(e); } });
  R.subscribe = (fn) => { R.listeners.add(fn); return () => R.listeners.delete(fn); };
  R.template = () => S.planTemplates.find((t) => t.id === R.st.templateId) || null;
  R.isActive = () => R.st.index >= 0;
  R.left = () => (R.st.paused ? R.st.remaining : Math.max(0, Math.ceil((R.st.endAt - Date.now()) / 1000)));

  function rStartTick() { if (!R.iv) R.iv = setInterval(rTick, 250); }
  function rStopTick() { clearInterval(R.iv); R.iv = null; }

  function beginTask(index) {
    const t = R.template();
    R.st.index = index;
    R.st.remaining = t.tasks[index].durationMinutes * 60;
    R.st.endAt = Date.now() + R.st.remaining * 1000;
    R.st.paused = false;
  }

  function rTick() {
    if (!R.isActive() || R.st.paused) return;
    if (R.left() <= 0) advance();
    else remit('tick');
  }

  function complete() {
    const t = R.template();
    rStopTick();
    R.st.index = -1;
    R.st.completed = true;
    if (t) {
      const now = Date.now();
      S.addPlanHistory({
        id: now, templateName: t.name, completedAtMillis: now, taskCount: t.tasks.length,
        totalMinutes: t.tasks.reduce((sum, x) => sum + x.durationMinutes, 0), note: '',
      });
    }
    alertUser(3);
    if (S.voiceEnabled) speak(phrases().planDone);
    notify('План выполнен', t ? t.name : '');
    remit('done');
  }

  function advance() {
    const t = R.template();
    if (!t) { R.stop(); return; }
    if (R.st.index < t.tasks.length - 1) {
      beginTask(R.st.index + 1);
      alertUser(1);
      if (S.voiceEnabled) speak(phrases().next + t.tasks[R.st.index].title);
      notify('Дальше: ' + t.tasks[R.st.index].title, t.name);
      remit('task');
    } else {
      complete();
    }
  }

  R.start = function (template) {
    if (!template || !template.tasks.length) return;
    unlockAudio();
    R.st = { templateId: template.id, index: 0, paused: false, endAt: null, remaining: 0, completed: false };
    beginTask(0);
    rStartTick();
    if (S.voiceEnabled) speak(phrases().next + template.tasks[0].title);
    remit('task');
  };
  R.pause = function () {
    if (!R.isActive() || R.st.paused) return;
    R.st.remaining = R.left();
    R.st.paused = true;
    remit('state');
  };
  R.resume = function () {
    if (!R.isActive() || !R.st.paused) return;
    R.st.endAt = Date.now() + R.st.remaining * 1000;
    R.st.paused = false;
    remit('state');
  };
  R.skip = function () { if (R.isActive()) advance(); };
  R.stop = function () {
    rStopTick();
    R.st = { templateId: null, index: -1, paused: false, endAt: null, remaining: 0, completed: false };
    remit('state');
  };

  EPV.speakTest = speak;
  EPV.Timer = T;
  EPV.Runner = R;
  EPV.unlockAudio = unlockAudio;
})(window);
