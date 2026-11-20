package de.homework.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Hausaufgaben und Einstellungen, gespeichert in SharedPreferences. */
final class Store {

    static final class Task {
        long id;
        String subject = "", text = "";
        long day;       // LocalDate.toEpochDay()
        boolean done;
    }

    final List<Task> tasks = new ArrayList<>();
    private final SharedPreferences prefs;
    private long lastId;

    Store(Context c) {
        prefs = c.getSharedPreferences("homework", Context.MODE_PRIVATE);
        try {
            JSONArray a = new JSONArray(prefs.getString("tasks", "[]"));
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                Task t = new Task();
                t.id = o.optLong("id");
                t.subject = o.optString("subject", "");
                t.text = o.optString("text", "");
                t.day = o.optLong("day");
                t.done = o.optBoolean("done");
                tasks.add(t);
            }
        } catch (Exception ignored) { }
    }

    long newId() {
        long id = System.currentTimeMillis();
        if (id <= lastId) id = lastId + 1;
        for (Task t : tasks) if (t.id >= id) id = t.id + 1;
        lastId = id;
        return id;
    }

    void save() {
        try {
            JSONArray a = new JSONArray();
            for (Task t : tasks) {
                JSONObject o = new JSONObject();
                o.put("id", t.id);
                o.put("subject", t.subject);
                o.put("text", t.text);
                o.put("day", t.day);
                o.put("done", t.done);
                a.put(o);
            }
            prefs.edit().putString("tasks", a.toString()).apply();
        } catch (Exception ignored) { }
    }

    /** Zuletzt benutzte Fächer, neueste zuerst. */
    List<String> recentSubjects(int max) {
        List<Task> sorted = new ArrayList<>(tasks);
        sorted.sort((a, b) -> Long.compare(b.id, a.id));
        List<String> out = new ArrayList<>();
        for (Task t : sorted) {
            String s = t.subject.trim();
            if (s.isEmpty()) continue;
            boolean dup = false;
            for (String o : out) if (o.equalsIgnoreCase(s)) { dup = true; break; }
            if (!dup) out.add(s);
            if (out.size() >= max) break;
        }
        return out;
    }

    int getInt(String k, int def) { return prefs.getInt(k, def); }
    boolean getBool(String k, boolean def) { return prefs.getBoolean(k, def); }
    void putInt(String k, int v) { prefs.edit().putInt(k, v).apply(); }
    void putBool(String k, boolean v) { prefs.edit().putBoolean(k, v).apply(); }
}
