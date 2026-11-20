package de.homework.app;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import de.homework.R;

public class MainActivity extends Activity {

    private Store store;
    private FrameLayout root;
    private LinearLayout header, list;
    private ScrollView scroll;
    private TextView titleView, countView;
    private Ico fab;
    private int insetTop, insetBottom;
    private boolean hideDone;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        store = new Store(this);
        T.lang = Math.max(0, Math.min(T.CODES.length - 1, store.getInt("lang", 0)));
        Pal.set(store.getBool("light", false));
        hideDone = store.getBool("hideDone", false);

        Window w = getWindow();
        if (Build.VERSION.SDK_INT >= 30) w.setDecorFitsSystemWindows(false);
        else w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        root = new FrameLayout(this);
        setContentView(root);
        root.setOnApplyWindowInsetsListener((v, in) -> {
            insetTop = in.getSystemWindowInsetTop();
            insetBottom = in.getSystemWindowInsetBottom();
            applyInsets();
            return in;
        });
        buildUi();
    }

    // ------------------------------------------------------------------ Aufbau

    private void buildUi() {
        root.removeAllViews();
        root.setBackgroundColor(Pal.bg);
        getWindow().setBackgroundDrawable(new ColorDrawable(Pal.bg));
        applyBarColors();

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        root.addView(col, new FrameLayout.LayoutParams(-1, -1));

        header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titleView = text(T.title(), 28, Pal.text, true);
        countView = text("", 14, Pal.dim, false);
        titles.addView(titleView);
        titles.addView(countView);
        header.addView(titles, new LinearLayout.LayoutParams(0, -2, 1f));
        Ico settings = new Ico(this, Ico.SETTINGS);
        settings.setBackground(round(Pal.card, Pal.line, 12));
        settings.setOnClickListener(v -> openSettings());
        settings.setContentDescription(T.settings());
        header.addView(settings, new LinearLayout.LayoutParams(dp(44), dp(44)));
        col.addView(header);

        scroll = new ScrollView(this);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(false);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list, new ScrollView.LayoutParams(-1, -2));
        col.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        fab = new Ico(this, Ico.PLUS);
        fab.setElevation(dp(6));
        fab.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override public void getOutline(View v, android.graphics.Outline o) { o.setOval(0, 0, v.getWidth(), v.getHeight()); }
        });
        fab.setContentDescription(T.newTask());
        fab.setOnClickListener(v -> openEditor(null));
        root.addView(fab, new FrameLayout.LayoutParams(dp(62), dp(62), Gravity.BOTTOM | Gravity.RIGHT));

        applyInsets();
        root.requestApplyInsets();
        refresh();
    }

    private void applyInsets() {
        if (header == null) return;
        header.setPadding(dp(20), insetTop + dp(18), dp(16), dp(10));
        list.setPadding(dp(16), dp(4), dp(16), insetBottom + dp(104));
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) fab.getLayoutParams();
        lp.setMargins(0, 0, dp(22), insetBottom + dp(22));
        fab.setLayoutParams(lp);
    }

    private void applyBarColors() {
        Window w = getWindow();
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = w.getInsetsController();
            if (c != null) {
                int mask = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                c.setSystemBarsAppearance(Pal.light ? mask : 0, mask);
            }
        } else {
            int f = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            if (Pal.light) f |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            w.getDecorView().setSystemUiVisibility(f);
        }
    }

    // ------------------------------------------------------------------ Liste

    private void refresh() {
        int scrollY = scroll.getScrollY();
        list.removeAllViews();
        long today = LocalDate.now().toEpochDay();

        int open = 0;
        for (Store.Task t : store.tasks) if (!t.done) open++;
        countView.setText(T.open(open));

        List<Store.Task> overdue = new ArrayList<>(), upcoming = new ArrayList<>(), earlier = new ArrayList<>();
        for (Store.Task t : store.tasks) {
            if (hideDone && t.done) continue;
            if (t.day < today) (t.done ? earlier : overdue).add(t);
            else upcoming.add(t);
        }
        overdue.sort((a, b) -> a.day != b.day ? Long.compare(a.day, b.day) : Long.compare(a.id, b.id));
        upcoming.sort((a, b) -> {
            if (a.day != b.day) return Long.compare(a.day, b.day);
            if (a.done != b.done) return a.done ? 1 : -1;
            return Long.compare(a.id, b.id);
        });
        earlier.sort((a, b) -> a.day != b.day ? Long.compare(b.day, a.day) : Long.compare(b.id, a.id));

        if (overdue.isEmpty() && upcoming.isEmpty() && earlier.isEmpty()) {
            TextView e = text(store.tasks.isEmpty() ? T.empty() : T.allDone(), 16, Pal.dim, false);
            e.setGravity(Gravity.CENTER);
            e.setLineSpacing(0, 1.25f);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.setMargins(0, dp(120), 0, 0);
            list.addView(e, lp);
            return;
        }

        if (!overdue.isEmpty()) {
            sectionHeader(T.overdue(), Pal.red);
            for (Store.Task t : overdue) list.addView(row(t, true));
        }
        long lastDay = Long.MIN_VALUE;
        for (Store.Task t : upcoming) {
            if (t.day != lastDay) {
                sectionHeader(dayLabel(t.day, today), t.day == today ? Pal.accent : Pal.dim);
                lastDay = t.day;
            }
            list.addView(row(t, false));
        }
        if (!earlier.isEmpty()) {
            sectionHeader(T.earlier(), Pal.dim);
            for (Store.Task t : earlier) list.addView(row(t, true));
        }
        scroll.post(() -> scroll.scrollTo(0, scrollY));
    }

    private void sectionHeader(String label, int color) {
        TextView h = text(label, 13, color, true);
        h.setLetterSpacing(0.04f);
        h.setPadding(dp(4), dp(18), dp(4), dp(8));
        list.addView(h);
    }

    private View row(Store.Task t, boolean showDate) {
        LinearLayout r = new LinearLayout(this);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setBackground(round(Pal.card, Pal.line, 12));
        r.setPadding(dp(16), dp(12), dp(6), dp(12));
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, -2);
        rp.setMargins(0, 0, 0, dp(8));
        r.setLayoutParams(rp);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        String subj = t.subject.trim();
        TextView sv = text(subj.isEmpty() ? t.text.trim() : subj, 16, Pal.text, true);
        sv.setMaxLines(2);
        top.addView(sv, new LinearLayout.LayoutParams(0, -2, 1f));
        if (showDate) {
            TextView dv = text(dayLabel(t.day, LocalDate.now().toEpochDay()), 12, t.done ? Pal.dim : Pal.red, false);
            dv.setPadding(dp(8), 0, 0, 0);
            top.addView(dv);
        }
        texts.addView(top);
        TextView tv = null;
        if (!subj.isEmpty() && !t.text.trim().isEmpty()) {
            tv = text(t.text.trim(), 14, Pal.dim, false);
            tv.setMaxLines(4);
            tv.setLineSpacing(0, 1.15f);
            tv.setPadding(0, dp(3), 0, 0);
            texts.addView(tv);
        }
        r.addView(texts, new LinearLayout.LayoutParams(0, -2, 1f));

        Ico check = new Ico(this, Ico.CHECK);
        check.checked = t.done;
        check.setOnClickListener(v -> {
            t.done = !t.done;
            store.save();
            refresh();
        });
        r.addView(check, new LinearLayout.LayoutParams(dp(48), dp(48)));

        if (t.done) {
            texts.setAlpha(0.5f);
            sv.setPaintFlags(sv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            if (tv != null) tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        r.setOnClickListener(v -> openEditor(t));
        r.setOnLongClickListener(v -> {
            confirm(T.deleteQ(), T.delete(), () -> { store.tasks.remove(t); store.save(); refresh(); });
            return true;
        });
        return r;
    }

    private String dayLabel(long day, long today) {
        if (day == today) return T.today();
        if (day == today + 1) return T.tomorrow();
        if (day == today - 1) return T.yesterday();
        String s = LocalDate.ofEpochDay(day).format(DateTimeFormatter.ofPattern(T.datePattern(), T.locale()));
        return s.isEmpty() ? s : s.substring(0, 1).toUpperCase(T.locale()) + s.substring(1);
    }

    private String fullDayLabel(long day) {
        long today = LocalDate.now().toEpochDay();
        String d = LocalDate.ofEpochDay(day).format(DateTimeFormatter.ofPattern(T.datePattern(), T.locale()));
        if (!d.isEmpty()) d = d.substring(0, 1).toUpperCase(T.locale()) + d.substring(1);
        if (day == today || day == today + 1 || day == today - 1) return dayLabel(day, today) + "  ·  " + d;
        return d;
    }

    // ------------------------------------------------------------------ Bearbeiten

    private void openEditor(Store.Task existing) {
        Dialog dlg = dialog();
        LinearLayout box = panel();

        box.addView(text(existing == null ? T.newTask() : T.editTask(), 20, Pal.text, true));

        box.addView(label(T.subject()));
        EditText subject = field(T.subjectHint(), false);
        if (existing != null) subject.setText(existing.subject);
        box.addView(subject, new LinearLayout.LayoutParams(-1, -2));

        List<String> recent = store.recentSubjects(12);
        if (!recent.isEmpty()) {
            HorizontalScrollView hs = new HorizontalScrollView(this);
            hs.setHorizontalScrollBarEnabled(false);
            LinearLayout chips = new LinearLayout(this);
            for (String s : recent) {
                TextView c = chip(s, false);
                c.setOnClickListener(v -> { subject.setText(s); subject.setSelection(s.length()); });
                chips.addView(c);
            }
            hs.addView(chips);
            LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2);
            hp.setMargins(0, dp(8), 0, 0);
            box.addView(hs, hp);
        }

        box.addView(label(T.task()));
        EditText task = field(T.taskHint(), true);
        if (existing != null) task.setText(existing.text);
        box.addView(task, new LinearLayout.LayoutParams(-1, -2));

        box.addView(label(T.due()));
        long today = LocalDate.now().toEpochDay();
        final long[] day = {existing != null ? existing.day : today + 1};
        TextView dateBtn = text("", 16, Pal.text, false);
        dateBtn.setBackground(round(Pal.field, Pal.line, 10));
        dateBtn.setPadding(dp(14), dp(12), dp(14), dp(12));
        box.addView(dateBtn, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout quick = new LinearLayout(this);
        TextView[] qs = {chip(T.today(), false), chip(T.tomorrow(), false), chip(T.inAWeek(), false)};
        long[] qd = {today, today + 1, today + 7};
        Runnable showDay = () -> {
            dateBtn.setText(fullDayLabel(day[0]));
            for (int i = 0; i < qs.length; i++) styleChip(qs[i], day[0] == qd[i]);
        };
        for (int i = 0; i < qs.length; i++) {
            final int k = i;
            qs[i].setOnClickListener(v -> { day[0] = qd[k]; showDay.run(); });
            quick.addView(qs[i]);
        }
        LinearLayout.LayoutParams qp = new LinearLayout.LayoutParams(-1, -2);
        qp.setMargins(0, dp(8), 0, 0);
        box.addView(quick, qp);
        showDay.run();
        dateBtn.setOnClickListener(v -> {
            LocalDate d = LocalDate.ofEpochDay(day[0]);
            DatePickerDialog p = new DatePickerDialog(this, Pal.light ? R.style.PickerLight : R.style.PickerDark,
                    (view, y, m, dd) -> { day[0] = LocalDate.of(y, m + 1, dd).toEpochDay(); showDay.run(); },
                    d.getYear(), d.getMonthValue() - 1, d.getDayOfMonth());
            p.show();
        });

        LinearLayout buttons = new LinearLayout(this);
        buttons.setGravity(Gravity.CENTER_VERTICAL);
        if (existing != null) {
            TextView del = button(T.delete(), false);
            del.setTextColor(Pal.red);
            del.setOnClickListener(v -> confirm(T.deleteQ(), T.delete(), () -> {
                store.tasks.remove(existing);
                store.save();
                dlg.dismiss();
                refresh();
            }));
            buttons.addView(del);
        }
        buttons.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1f));
        TextView cancel = button(T.cancel(), false);
        cancel.setOnClickListener(v -> dlg.dismiss());
        buttons.addView(cancel);
        TextView save = button(T.save(), true);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-2, -2);
        sp.setMargins(dp(8), 0, 0, 0);
        save.setOnClickListener(v -> {
            String s = subject.getText().toString().trim(), x = task.getText().toString().trim();
            if (s.isEmpty() && x.isEmpty()) {
                Toast.makeText(this, T.needText(), Toast.LENGTH_SHORT).show();
                return;
            }
            Store.Task t = existing;
            if (t == null) {
                t = new Store.Task();
                t.id = store.newId();
                store.tasks.add(t);
            }
            t.subject = s;
            t.text = x;
            t.day = day[0];
            store.save();
            dlg.dismiss();
            refresh();
        });
        buttons.addView(save, sp);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, -2);
        bp.setMargins(0, dp(22), 0, 0);
        box.addView(buttons, bp);

        showDialog(dlg, box);
        if (existing == null) {
            subject.requestFocus();
            dlg.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                    | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    // ------------------------------------------------------------------ Einstellungen

    private void openSettings() {
        Dialog dlg = dialog();
        LinearLayout box = panel();
        box.addView(text(T.settings(), 20, Pal.text, true));

        box.addView(label(T.language()));
        for (int i = 0; i < T.NAMES.length; i++) {
            final int k = i;
            box.addView(optionRow(T.NAMES[i], T.lang == i, v -> {
                T.lang = k;
                store.putInt("lang", k);
                dlg.dismiss();
                buildUi();
                openSettings();
            }));
        }

        box.addView(label(T.theme()));
        LinearLayout seg = new LinearLayout(this);
        TextView dark = chip(T.dark(), !Pal.light), light = chip(T.light(), Pal.light);
        dark.setGravity(Gravity.CENTER);
        light.setGravity(Gravity.CENTER);
        View.OnClickListener themeClick = v -> {
            boolean wantLight = v == light;
            if (wantLight == Pal.light) return;
            Pal.set(wantLight);
            store.putBool("light", wantLight);
            dlg.dismiss();
            buildUi();
            openSettings();
        };
        dark.setOnClickListener(themeClick);
        light.setOnClickListener(themeClick);
        seg.addView(dark, new LinearLayout.LayoutParams(0, -2, 1f));
        seg.addView(light, new LinearLayout.LayoutParams(0, -2, 1f));
        box.addView(seg, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout hide = new LinearLayout(this);
        hide.setGravity(Gravity.CENTER_VERTICAL);
        hide.setPadding(0, dp(14), 0, 0);
        hide.addView(text(T.hideDone(), 16, Pal.text, false), new LinearLayout.LayoutParams(0, -2, 1f));
        Ico hc = new Ico(this, Ico.CHECK);
        hc.checked = hideDone;
        hide.addView(hc, new LinearLayout.LayoutParams(dp(48), dp(48)));
        hide.setOnClickListener(v -> {
            hideDone = !hideDone;
            store.putBool("hideDone", hideDone);
            hc.checked = hideDone;
            hc.invalidate();
            refresh();
        });
        hc.setOnClickListener(v -> hide.performClick());
        box.addView(hide);

        TextView clear = button(T.clearDone(), false);
        clear.setTextColor(Pal.red);
        clear.setBackground(round(0, Pal.line, 10));
        clear.setGravity(Gravity.CENTER);
        clear.setOnClickListener(v -> confirm(T.clearQ(), T.delete(), () -> {
            store.tasks.removeIf(t -> t.done);
            store.save();
            refresh();
        }));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(0, dp(10), 0, 0);
        box.addView(clear, cp);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setGravity(Gravity.END);
        TextView close = button(T.close(), true);
        close.setOnClickListener(v -> dlg.dismiss());
        buttons.addView(close);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, -2);
        bp.setMargins(0, dp(18), 0, 0);
        box.addView(buttons, bp);

        ScrollView sc = new ScrollView(this);
        sc.addView(box);
        showDialog(dlg, sc);
    }

    private View optionRow(String name, boolean selected, View.OnClickListener click) {
        LinearLayout r = new LinearLayout(this);
        r.setGravity(Gravity.CENTER_VERTICAL);
        Ico radio = new Ico(this, Ico.RADIO);
        radio.checked = selected;
        r.addView(radio, new LinearLayout.LayoutParams(dp(36), dp(42)));
        r.addView(text(name, 16, Pal.text, selected), new LinearLayout.LayoutParams(0, -2, 1f));
        r.setOnClickListener(click);
        return r;
    }

    private void confirm(String msg, String action, Runnable yes) {
        Dialog dlg = dialog();
        LinearLayout box = panel();
        box.addView(text(msg, 17, Pal.text, false));
        LinearLayout buttons = new LinearLayout(this);
        buttons.setGravity(Gravity.END);
        TextView no = button(T.cancel(), false);
        no.setOnClickListener(v -> dlg.dismiss());
        buttons.addView(no);
        TextView ok = button(action, true);
        ok.setBackground(round(Pal.red, 0, 10));
        ok.setOnClickListener(v -> { dlg.dismiss(); yes.run(); });
        LinearLayout.LayoutParams op = new LinearLayout.LayoutParams(-2, -2);
        op.setMargins(dp(8), 0, 0, 0);
        buttons.addView(ok, op);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, -2);
        bp.setMargins(0, dp(20), 0, 0);
        box.addView(buttons, bp);
        showDialog(dlg, box);
    }

    // ------------------------------------------------------------------ Bausteine

    private Dialog dialog() {
        Dialog d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return d;
    }

    private LinearLayout panel() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackground(round(Pal.card, Pal.line, 16));
        box.setPadding(dp(20), dp(20), dp(20), dp(16));
        return box;
    }

    private void showDialog(Dialog dlg, View content) {
        dlg.setContentView(content);
        Window w = dlg.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = Math.min(getResources().getDisplayMetrics().widthPixels - dp(32), dp(480));
            w.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        dlg.show();
    }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        return t;
    }

    private TextView label(String s) {
        TextView t = text(s, 13, Pal.dim, true);
        t.setPadding(0, dp(16), 0, dp(6));
        return t;
    }

    private EditText field(String hint, boolean multi) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextColor(Pal.text);
        e.setHintTextColor(Pal.dim);
        e.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        e.setBackground(round(Pal.field, Pal.line, 10));
        e.setPadding(dp(14), dp(12), dp(14), dp(12));
        if (multi) {
            e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            e.setMinLines(3);
            e.setMaxLines(8);
            e.setGravity(Gravity.TOP | Gravity.START);
        } else {
            e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            e.setSingleLine(true);
        }
        return e;
    }

    private TextView chip(String s, boolean selected) {
        TextView c = text(s, 14, Pal.text, false);
        c.setPadding(dp(12), dp(8), dp(12), dp(8));
        c.setSingleLine(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMargins(0, 0, dp(8), 0);
        c.setLayoutParams(lp);
        styleChip(c, selected);
        return c;
    }

    private void styleChip(TextView c, boolean selected) {
        c.setBackground(round(selected ? Pal.accent : Pal.field, selected ? 0 : Pal.line, 10));
        c.setTextColor(selected ? 0xFFFFFFFF : Pal.text);
    }

    private TextView button(String s, boolean primary) {
        TextView b = text(s, 15, primary ? 0xFFFFFFFF : Pal.text, true);
        b.setPadding(dp(18), dp(11), dp(18), dp(11));
        b.setGravity(Gravity.CENTER);
        if (primary) b.setBackground(round(Pal.accent, 0, 10));
        return b;
    }

    private GradientDrawable round(int fill, int stroke, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(radiusDp));
        if (stroke != 0) g.setStroke(Math.max(1, dp(1)), stroke);
        return g;
    }

    private int dp(float v) { return Math.round(v * getResources().getDisplayMetrics().density); }
}
