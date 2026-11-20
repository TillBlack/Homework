package de.homework.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/** Selbst gezeichnete Symbole: Plus, Einstellungen, Haken-Kästchen, Auswahlpunkt. */
final class Ico extends View {

    static final int PLUS = 0, SETTINGS = 1, CHECK = 2, RADIO = 3;

    final int kind;
    boolean checked;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF r = new RectF();

    Ico(Context c, int kind) {
        super(c);
        this.kind = kind;
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight(), cx = w / 2f, cy = h / 2f;
        float d = getResources().getDisplayMetrics().density;
        switch (kind) {
            case PLUS: {
                // runder Knopf in Akzentfarbe mit weißem Plus
                p.setStyle(Paint.Style.FILL);
                p.setColor(Pal.accent);
                float rad = Math.min(w, h) / 2f;
                c.drawCircle(cx, cy, rad, p);
                p.setColor(0xFFFFFFFF);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(3f * d);
                float a = rad * 0.36f;
                c.drawLine(cx - a, cy, cx + a, cy, p);
                c.drawLine(cx, cy - a, cx, cy + a, p);
                break;
            }
            case SETTINGS: {
                // drei Schieberegler
                p.setColor(Pal.text);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(2f * d);
                float half = 9f * d;
                float[] ys = {cy - 6f * d, cy, cy + 6f * d};
                float[] knobs = {cx + 4f * d, cx - 4f * d, cx + 1f * d};
                for (int i = 0; i < 3; i++) {
                    c.drawLine(cx - half, ys[i], cx + half, ys[i], p);
                }
                p.setStyle(Paint.Style.FILL);
                for (int i = 0; i < 3; i++) {
                    p.setColor(Pal.card);
                    c.drawCircle(knobs[i], ys[i], 3.2f * d, p);
                }
                p.setStyle(Paint.Style.STROKE);
                p.setColor(Pal.text);
                for (int i = 0; i < 3; i++) c.drawCircle(knobs[i], ys[i], 2.6f * d, p);
                break;
            }
            case CHECK: {
                float s = 22f * d;
                r.set(cx - s / 2, cy - s / 2, cx + s / 2, cy + s / 2);
                if (checked) {
                    p.setStyle(Paint.Style.FILL);
                    p.setColor(Pal.accent);
                    c.drawRoundRect(r, 6f * d, 6f * d, p);
                    p.setStyle(Paint.Style.STROKE);
                    p.setColor(0xFFFFFFFF);
                    p.setStrokeWidth(2.4f * d);
                    path.reset();
                    path.moveTo(cx - 5.5f * d, cy + 0.5f * d);
                    path.lineTo(cx - 1.5f * d, cy + 4.5f * d);
                    path.lineTo(cx + 6f * d, cy - 4f * d);
                    c.drawPath(path, p);
                } else {
                    p.setStyle(Paint.Style.STROKE);
                    p.setColor(Pal.dim);
                    p.setStrokeWidth(2f * d);
                    r.inset(1f * d, 1f * d);
                    c.drawRoundRect(r, 5f * d, 5f * d, p);
                }
                break;
            }
            case RADIO: {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(2f * d);
                p.setColor(checked ? Pal.accent : Pal.dim);
                c.drawCircle(cx, cy, 9f * d, p);
                if (checked) {
                    p.setStyle(Paint.Style.FILL);
                    c.drawCircle(cx, cy, 5f * d, p);
                }
                break;
            }
        }
    }
}
