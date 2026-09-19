/* EPV web — persistence layer (localStorage). Export format is compatible with the Android app. */
(function (w) {
  'use strict';
  const EPV = w.EPV;
  const PREFIX = 'epv:';

  const DB = {
    get(key, def) {
      try {
        const raw = localStorage.getItem(PREFIX + key);
        return raw === null ? clone(def) : JSON.parse(raw);
      } catch (e) {
        return clone(def);
      }
    },
    set(key, value) {
      try {
        localStorage.setItem(PREFIX + key, JSON.stringify(value));
      } catch (e) {
        console.warn('storage failed', e);
      }
    },
    remove(key) {
      try { localStorage.removeItem(PREFIX + key); } catch (e) { /* ignore */ }
    },
    clearAll() {
      try {
        Object.keys(localStorage).filter((k) => k.startsWith(PREFIX)).forEach((k) => localStorage.removeItem(k));
      } catch (e) { /* ignore */ }
    },
  };
  function clone(v) {
    return v !== null && typeof v === 'object' ? JSON.parse(JSON.stringify(v)) : v;
  }
  EPV.DB = DB;

  const S = {};
  function prop(name, key, def) {
    Object.defineProperty(S, name, {
      get: () => DB.get(key, def),
      set: (v) => DB.set(key, v),
      enumerable: true,
    });
  }
  // profile
  prop('userName', 'user_name', '');
  prop('lastName', 'last_name', '');
  prop('email', 'email', '');
  prop('consent', 'data_consent_given', false);
  prop('weightKg', 'weight_kg', '');
  prop('heightCm', 'height_cm', '');
  prop('age', 'age', '');
  prop('gender', 'gender', '');
  prop('maritalStatus', 'marital_status', '');
  prop('personalityType', 'personality_type', '');
  prop('nightWake', 'night_wake_frequency', '');
  prop('wakeTime', 'wake_time', '07:00');
  prop('bedTime', 'bed_time', '23:00');
  prop('isWorking', 'is_working', true);
  prop('breakfastTime', 'breakfast_time', '08:00');
  prop('lunchTime', 'lunch_time', '13:00');
  prop('dinnerTime', 'dinner_time', '19:00');
  prop('workHours', 'work_hours_per_day', 8);
  prop('mealsPerDay', 'meals_per_day', 3);
  prop('waterUnit', 'water_unit', 'Бутылки');
  prop('waterCount', 'water_count', 4);
  prop('photo', 'photo_data', '');
  // account
  prop('passwordHash', 'account_password_hash', '');
  prop('isRegistered', 'is_registered', false);
  prop('isLoggedIn', 'is_logged_in', false);
  prop('isOnboarded', 'is_onboarded', false);
  prop('introSeen', 'intro_seen', false);
  // settings
  prop('workMinutes', 'work_minutes', 25);
  prop('restMinutes', 'rest_minutes', 5);
  prop('sound', 'sound_enabled', true);
  prop('vibration', 'vibration_enabled', true);
  prop('keepScreenOn', 'keep_screen_on', true);
  prop('notifications', 'notifications_enabled', false);
  prop('voiceEnabled', 'voice_announce_enabled', false);
  prop('voiceLead', 'voice_announce_value', 30);
  prop('voiceUnit', 'voice_announce_unit', 'Секунды');
  prop('voiceLanguage', 'voice_language', 'Русский');
  prop('elevenEnabled', 'eleven_labs_enabled', false);
  prop('elevenKey', 'eleven_labs_api_key', '');
  prop('elevenVoiceMale', 'eleven_labs_voice_male', 'pNInz6obpgDQGcFmaJgB');
  prop('elevenVoiceFemale', 'eleven_labs_voice_female', '21m00Tcm4TlvDq8ikWAM');
  prop('presets', 'timer_presets', EPV.DEFAULT_PRESETS);
  prop('categories', 'categories', EPV.DEFAULT_CATEGORIES);
  // data
  prop('history', 'session_history', []);
  prop('daySchedule', 'day_schedule', '');
  prop('lifestyleAnswers', 'lifestyle_answers', {});
  prop('planTemplates', 'plan_templates', []);
  prop('activePlanId', 'active_plan_template_id', null);
  prop('planHistory', 'plan_history', []);
  prop('routineTasks', 'routine_tasks', EPV.DEFAULT_ROUTINE);
  prop('routineDone', 'routine_completions', {});
  prop('daySummaries', 'day_summaries', {});
  prop('timerState', 'timer_state', null);

  const MAX_HISTORY = 300;

  S.addHistory = function (entry) {
    S.history = [entry, ...S.history].slice(0, MAX_HISTORY);
  };
  S.updateHistoryComment = function (id, comment) {
    S.history = S.history.map((e) => (e.id === id ? { ...e, comment } : e));
  };
  S.deleteHistory = function (id) {
    S.history = S.history.filter((e) => e.id !== id);
  };
  S.addPlanHistory = function (entry) {
    S.planHistory = [entry, ...S.planHistory].slice(0, MAX_HISTORY);
  };
  S.updatePlanHistoryNote = function (id, note) {
    S.planHistory = S.planHistory.map((e) => (e.id === id ? { ...e, note } : e));
  };
  S.deletePlanHistory = function (id) {
    S.planHistory = S.planHistory.filter((e) => e.id !== id);
  };

  // routine
  S.doneIds = function (date) {
    return new Set(S.routineDone[date] || []);
  };
  S.setRoutineDone = function (date, taskId, done) {
    const all = S.routineDone;
    const set = new Set(all[date] || []);
    if (done) set.add(taskId); else set.delete(taskId);
    all[date] = [...set];
    S.routineDone = all;
  };
  S.streak = function (taskId, todayKey) {
    let streak = 0;
    const all = S.routineDone;
    const d = new Date();
    if ((all[todayKey] || []).includes(taskId)) streak = 1;
    for (let i = 0; i < 400; i++) {
      d.setDate(d.getDate() - 1);
      const key = dateKey(d);
      if ((all[key] || []).includes(taskId)) streak++;
      else break;
    }
    return streak;
  };

  function dateKey(d) {
    return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
  }
  EPV.dateKey = dateKey;

  // ---- hashing (SHA-256 when available, small fallback on insecure origins) ----
  EPV.hash = async function (text) {
    if (w.crypto && w.crypto.subtle) {
      const buf = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(text));
      return Array.from(new Uint8Array(buf)).map((b) => b.toString(16).padStart(2, '0')).join('');
    }
    let h1 = 0xdeadbeef, h2 = 0x41c6ce57;
    for (let i = 0; i < text.length; i++) {
      const ch = text.charCodeAt(i);
      h1 = Math.imul(h1 ^ ch, 2654435761);
      h2 = Math.imul(h2 ^ ch, 1597334677);
    }
    h1 = Math.imul(h1 ^ (h1 >>> 16), 2246822507) ^ Math.imul(h2 ^ (h2 >>> 13), 3266489909);
    h2 = Math.imul(h2 ^ (h2 >>> 16), 2246822507) ^ Math.imul(h1 ^ (h1 >>> 13), 3266489909);
    return 'f' + (4294967296 * (2097151 & h2) + (h1 >>> 0)).toString(16);
  };

  // ---- export / import (profile, settings, history match the Android format) ----
  S.exportAll = function () {
    return {
      exportVersion: 1,
      profile: {
        userName: S.userName, lastName: S.lastName, email: S.email, dataConsentGiven: S.consent,
        weightKg: S.weightKg, heightCm: S.heightCm, age: S.age, gender: S.gender,
        maritalStatus: S.maritalStatus, wakeTime: S.wakeTime, bedTime: S.bedTime, isWorking: S.isWorking,
        breakfastTime: S.breakfastTime, lunchTime: S.lunchTime, dinnerTime: S.dinnerTime,
        workHoursPerDay: S.workHours, mealsPerDay: S.mealsPerDay, waterUnit: S.waterUnit, waterCount: S.waterCount,
        personalityType: S.personalityType, nightWakeFrequency: S.nightWake,
      },
      settings: {
        workMinutes: S.workMinutes, restMinutes: S.restMinutes, soundEnabled: S.sound,
        vibrationEnabled: S.vibration, keepScreenOn: S.keepScreenOn, categories: S.categories,
      },
      history: S.history,
      web: {
        daySchedule: S.daySchedule, lifestyleAnswers: S.lifestyleAnswers, planTemplates: S.planTemplates,
        planHistory: S.planHistory, routineTasks: S.routineTasks, routineDone: S.routineDone,
        daySummaries: S.daySummaries, presets: S.presets,
      },
    };
  };

  S.importAll = function (json) {
    let root;
    try { root = JSON.parse(json); } catch (e) { return false; }
    if (!root || typeof root !== 'object') return false;
    const p = root.profile;
    if (p) {
      const map = {
        userName: 'userName', lastName: 'lastName', email: 'email', dataConsentGiven: 'consent',
        weightKg: 'weightKg', heightCm: 'heightCm', age: 'age', gender: 'gender', maritalStatus: 'maritalStatus',
        wakeTime: 'wakeTime', bedTime: 'bedTime', isWorking: 'isWorking', breakfastTime: 'breakfastTime',
        lunchTime: 'lunchTime', dinnerTime: 'dinnerTime', workHoursPerDay: 'workHours', mealsPerDay: 'mealsPerDay',
        waterUnit: 'waterUnit', waterCount: 'waterCount', personalityType: 'personalityType', nightWakeFrequency: 'nightWake',
      };
      Object.keys(map).forEach((k) => { if (p[k] !== undefined && p[k] !== null) S[map[k]] = p[k]; });
    }
    const s = root.settings;
    if (s) {
      if (s.workMinutes) S.workMinutes = s.workMinutes;
      if (s.restMinutes) S.restMinutes = s.restMinutes;
      if (typeof s.soundEnabled === 'boolean') S.sound = s.soundEnabled;
      if (typeof s.vibrationEnabled === 'boolean') S.vibration = s.vibrationEnabled;
      if (typeof s.keepScreenOn === 'boolean') S.keepScreenOn = s.keepScreenOn;
      if (Array.isArray(s.categories)) S.categories = s.categories;
    }
    if (Array.isArray(root.history)) {
      S.history = root.history
        .filter((e) => e && e.phase && e.startTimeMillis)
        .map((e) => ({
          id: e.id || e.startTimeMillis, phase: e.phase, startTimeMillis: e.startTimeMillis,
          durationSeconds: e.durationSeconds || 0, interrupted: !!e.interrupted,
          comment: e.comment || '', category: e.category || '', quote: e.quote || '',
        }))
        .sort((a, b) => b.startTimeMillis - a.startTimeMillis);
    }
    const x = root.web;
    if (x) {
      if (typeof x.daySchedule === 'string') S.daySchedule = x.daySchedule;
      if (x.lifestyleAnswers) S.lifestyleAnswers = x.lifestyleAnswers;
      if (Array.isArray(x.planTemplates)) S.planTemplates = x.planTemplates;
      if (Array.isArray(x.planHistory)) S.planHistory = x.planHistory;
      if (Array.isArray(x.routineTasks)) S.routineTasks = x.routineTasks;
      if (x.routineDone) S.routineDone = x.routineDone;
      if (x.daySummaries) S.daySummaries = x.daySummaries;
      if (Array.isArray(x.presets)) S.presets = x.presets;
    }
    return true;
  };

  EPV.S = S;
})(window);
