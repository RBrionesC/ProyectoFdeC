package com.example.dogpedia;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.LineBackgroundSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.HashSet;
import java.util.Set;

public class BorderDecorator implements DayViewDecorator {

    private final Set<CalendarDay> dates;
    private final int borderColor;

    public BorderDecorator(Set<CalendarDay> dates, int borderColor) {
        this.dates = dates;
        this.borderColor = borderColor;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new LineBackgroundSpan() {
            @Override
            public void drawBackground(Canvas canvas, Paint paint,
                                       int left, int right, int top, int baseline, int bottom,
                                       CharSequence charSequence, int start, int end, int lineNum) {

                Paint borderPaint = new Paint();
                borderPaint.setStyle(Paint.Style.STROKE);
                borderPaint.setColor(borderColor);
                borderPaint.setStrokeWidth(4f);
                borderPaint.setAntiAlias(true);

                int margin = 15;
                float cx = (left + right) / 2f;
                float cy = (top + bottom) / 2f;
                float radius = Math.min(right - left, bottom - top) / 2f + margin; // Ajusta según necesites

                canvas.drawCircle(cx, cy, radius, borderPaint);
            }
        });
    }
}
