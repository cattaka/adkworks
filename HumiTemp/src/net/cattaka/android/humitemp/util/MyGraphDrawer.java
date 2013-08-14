
package net.cattaka.android.humitemp.util;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import net.cattaka.android.humitemp.R;
import net.cattaka.android.humitemp.db.DbHelper;
import net.cattaka.android.humitemp.entity.HumiTempModel;
import net.cattaka.android.humitemp.util.CtkCanvasUtil.Gravity;
import net.cattaka.android.humitemp.util.GraphDrawer.GraphAreaInfo;
import net.cattaka.android.humitemp.util.GraphDrawer.GraphValue;
import net.cattaka.android.humitemp.util.GraphDrawer.GridInfo;
import net.cattaka.android.humitemp.util.GraphDrawer.GridLine;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class MyGraphDrawer {

    public static final int UPDATE_INTERVAL = 10;

    public static class GraphTextLabelInfo {
        public String title;

        public String appName;

        public float textSize = 12;

        public float labelTextSize = 8;

        public String unitXFormat;

        public String unitYFormat;
    }

    private Context context;

    private DateConverterUtil dateConverterUtil;

    private DbHelper dbHelper;

    public MyGraphDrawer(Context context, DbHelper dbHelper) {
        super();
        this.context = context;
        this.dbHelper = dbHelper;
        this.dateConverterUtil = new DateConverterUtil();
    }

    public Bitmap createImageDay10Min(GregorianCalendar src) {
        long startTimeLong;
        long endTimeLong;

        GregorianCalendar today;
        GregorianCalendar yesterday;
        today = new GregorianCalendar(src.get(Calendar.YEAR), src.get(Calendar.MONTH),
                src.get(Calendar.DAY_OF_MONTH));
        yesterday = new GregorianCalendar(src.get(Calendar.YEAR), src.get(Calendar.MONTH),
                src.get(Calendar.DAY_OF_MONTH));
        yesterday.add(Calendar.DATE, 1);
        startTimeLong = today.getTimeInMillis();
        endTimeLong = yesterday.getTimeInMillis();
        int intervalX = 60 * 1000 * UPDATE_INTERVAL;

        // X軸のグリッドとラベルを作成
        List<GridInfo> gixList = new ArrayList<GridInfo>();
        for (int i = 0; i <= 24; i++) {
            // ３時間毎にグリッド、１時間毎に補助グリッド
            GridInfo gix = new GridInfo();
            gix.value = i * 60 * 60 * 1000;
            if (i % 3 == 0) {
                gix.gridLine = GridLine.LINE;
                gix.label = String.valueOf(i);
            } else {
                gix.gridLine = GridLine.DASHED;
            }
            gixList.add(gix);
        }

        Resources resources = context.getResources();
        GraphTextLabelInfo gtlInfo = new GraphTextLabelInfo();
        gtlInfo.appName = resources.getString(R.string.app_name);
        gtlInfo.title = DateFormat.getDateInstance(DateFormat.MEDIUM).format(
                new Date(startTimeLong));
        gtlInfo.unitXFormat = resources.getString(R.string.graph_hour);
        gtlInfo.unitYFormat = resources.getString(R.string.graph_step_min);

        return createImage(gtlInfo, new Date(startTimeLong), new Date(endTimeLong), intervalX,
                gixList);
    }

    public Bitmap createImageDay1Hour(GregorianCalendar src) {
        long startTimeLong;
        long endTimeLong;

        GregorianCalendar today;
        GregorianCalendar yesterday;
        today = new GregorianCalendar(src.get(Calendar.YEAR), src.get(Calendar.MONTH),
                src.get(Calendar.DAY_OF_MONTH));
        yesterday = new GregorianCalendar(src.get(Calendar.YEAR), src.get(Calendar.MONTH),
                src.get(Calendar.DAY_OF_MONTH));
        yesterday.add(Calendar.DATE, 1);
        startTimeLong = today.getTimeInMillis();
        endTimeLong = yesterday.getTimeInMillis();
        int intervalX = 60 * 60 * 1000;

        // X軸のグリッドとラベルを作成
        List<GridInfo> gixList = new ArrayList<GridInfo>();
        for (int i = 0; i <= 24; i++) {
            // ３時間毎にグリッド、１時間毎に補助グリッド
            GridInfo gix = new GridInfo();
            gix.value = i * 60 * 60 * 1000;
            if (i % 3 == 0) {
                gix.gridLine = GridLine.LINE;
                gix.label = String.valueOf(i);
            } else {
                gix.gridLine = GridLine.DASHED;
            }
            gixList.add(gix);
        }

        Resources resources = context.getResources();
        GraphTextLabelInfo gtlInfo = new GraphTextLabelInfo();
        gtlInfo.appName = resources.getString(R.string.app_name);
        gtlInfo.title = DateFormat.getDateInstance(DateFormat.MEDIUM).format(
                new Date(startTimeLong));
        gtlInfo.unitXFormat = resources.getString(R.string.graph_hour);
        gtlInfo.unitYFormat = resources.getString(R.string.graph_step);

        return createImage(gtlInfo, new Date(startTimeLong), new Date(endTimeLong), intervalX,
                gixList);
    }

    public Bitmap createImageWeek(GregorianCalendar src) {
        long startTimeLong;
        long endTimeLong;

        GregorianCalendar weekStart = new GregorianCalendar();
        GregorianCalendar weekEnd = new GregorianCalendar();
        weekStart.clear();
        weekStart.set(Calendar.YEAR, src.get(Calendar.YEAR));
        weekStart.set(Calendar.WEEK_OF_YEAR, src.get(Calendar.WEEK_OF_YEAR));
        weekEnd.clear();
        weekEnd.set(Calendar.YEAR, src.get(Calendar.YEAR));
        weekEnd.set(Calendar.WEEK_OF_YEAR, src.get(Calendar.WEEK_OF_YEAR));
        weekEnd.add(Calendar.WEEK_OF_YEAR, 1);

        startTimeLong = weekStart.getTimeInMillis();
        endTimeLong = weekEnd.getTimeInMillis();
        int intervalX = 24 * 60 * 60 * 1000;

        // X軸のグリッドとラベルを作成
        String[] weekStr = context.getResources().getStringArray(R.array.label_week);
        List<GridInfo> gixList = new ArrayList<GridInfo>();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(startTimeLong);
        while (cal.compareTo(weekEnd) <= 0) {
            // 24時間毎にグリッド、12時間毎に補助グリッド
            int date = cal.get(Calendar.DATE);
            int week = cal.get(Calendar.DAY_OF_WEEK);
            GridInfo gix = new GridInfo();
            gix.value = (float)(cal.getTimeInMillis() - startTimeLong);
            gix.gridLine = GridLine.LINE;
            gixList.add(gix);

            if (cal.compareTo(weekEnd) < 0) {
                GridInfo gix2 = new GridInfo();
                gix2.label = String.format(weekStr[week - 1], date);
                gix2.value = gix.value + intervalX / 2f;
                if (week == Calendar.SUNDAY) {
                    gix2.labelColor = Color.RED;
                } else if (week == Calendar.SATURDAY) {
                    gix2.labelColor = Color.BLUE;
                } else {
                    gix2.labelColor = Color.BLACK;
                }
                gixList.add(gix2);
            }
            cal.add(Calendar.DATE, 1);
        }

        Resources resources = context.getResources();
        GraphTextLabelInfo gtlInfo = new GraphTextLabelInfo();
        gtlInfo.appName = resources.getString(R.string.app_name);
        gtlInfo.title = DateFormat.getDateInstance(DateFormat.MEDIUM).format(
                new Date(startTimeLong))
                + " - "
                + DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(endTimeLong - 1));
        gtlInfo.unitXFormat = resources.getString(R.string.graph_day);
        gtlInfo.unitYFormat = resources.getString(R.string.graph_step);

        return createImage(gtlInfo, new Date(startTimeLong), new Date(endTimeLong), intervalX,
                gixList);
    }

    public Bitmap createImageMonth(GregorianCalendar src) {
        long startTimeLong;
        long endTimeLong;

        GregorianCalendar monthStart = new GregorianCalendar();
        GregorianCalendar monthEnd = new GregorianCalendar();
        monthStart.clear();
        monthStart.set(Calendar.YEAR, src.get(Calendar.YEAR));
        monthStart.set(Calendar.MONTH, src.get(Calendar.MONTH));
        monthEnd.clear();
        monthEnd.set(Calendar.YEAR, src.get(Calendar.YEAR));
        monthEnd.set(Calendar.MONTH, src.get(Calendar.MONTH));
        monthEnd.add(Calendar.MONTH, 1);

        startTimeLong = monthStart.getTimeInMillis();
        endTimeLong = monthEnd.getTimeInMillis();
        long intervalX = 24 * 60 * 60 * 1000;

        // X軸のグリッドとラベルを作成
        List<GridInfo> gixList = new ArrayList<GridInfo>();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(startTimeLong);
        while (cal.compareTo(monthEnd) <= 0) {
            // １週間毎にグリッド、１日毎に補助グリッド
            int date = cal.get(Calendar.DATE);
            int week = cal.get(Calendar.DAY_OF_WEEK);
            GridInfo gix = new GridInfo();
            gix.value = (float)(cal.getTimeInMillis() - startTimeLong);
            if (date == 1 || week == Calendar.SUNDAY) {
                gix.gridLine = GridLine.LINE;
            } else {
                gix.gridLine = GridLine.DASHED;
            }
            if (cal.compareTo(monthEnd) < 0) {
                GridInfo gix2 = new GridInfo();
                gix2.label = String.valueOf(date);
                gix2.value = gix.value + intervalX / 2f;
                if (week == Calendar.SUNDAY) {
                    gix2.labelColor = Color.RED;
                } else if (week == Calendar.SATURDAY) {
                    gix2.labelColor = Color.BLUE;
                } else {
                    gix2.labelColor = Color.BLACK;
                }
                gixList.add(gix2);
            }
            gixList.add(gix);

            cal.add(Calendar.DATE, 1);
        }

        Resources resources = context.getResources();
        GraphTextLabelInfo gtlInfo = new GraphTextLabelInfo();
        gtlInfo.appName = resources.getString(R.string.app_name);
        gtlInfo.title = DateFormat.getDateInstance(DateFormat.MEDIUM).format(
                new Date(startTimeLong))
                + " - "
                + DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(endTimeLong - 1));
        gtlInfo.unitXFormat = resources.getString(R.string.graph_day);
        gtlInfo.unitYFormat = resources.getString(R.string.graph_step);

        return createImage(gtlInfo, new Date(startTimeLong), new Date(endTimeLong), intervalX,
                gixList);
    }

    private Bitmap createImage(GraphTextLabelInfo gtlInfo, Date from, Date to, long intervalX,
            List<GridInfo> gixList) {
        List<HumiTempModel> dtoList = dbHelper.findHumiTempModel(from, to);
        long startTimeLong = from.getTime();
        long endTimeLong = to.getTime();

        // データの入れ物を用意する
        List<GraphValue> gvList = new ArrayList<GraphValue>();
        {
            long rangeX = to.getTime() - from.getTime();
            for (long sx = 0; sx < rangeX; sx += intervalX) {
                GraphValue gv = new GraphValue();
                gv.startX = sx;
                gv.endX = sx + intervalX;
                gvList.add(gv);
            }
            for (HumiTempModel dto : dtoList) {
                long sx = dto.getDate().getTime() - startTimeLong;
                int si = (int)(sx / intervalX);
                if (0 <= si && si < gvList.size()) {
                    GraphValue gv = gvList.get(si);
                    gv.endY1 += (float)dto.getTemperature();
                    gv.endY2 += (float)dto.getHumidity();
                    gv.count++;
                }
            }
            for (GraphValue gv : gvList) {
                if (gv.count > 0) {
                    gv.endY1 /= gv.count;
                    gv.endY2 /= gv.count;
                    gv.endY2 *= 0.4f;
                }
            }
        }

        int maxValueY = 40;

        // Y軸のグリッドとラベルを作成
        List<GridInfo> giyList = new ArrayList<GridInfo>();
        {
            giyList.add(new GridInfo(GridLine.LINE, "40", "100", 40, Color.BLACK));
            giyList.add(new GridInfo(GridLine.LINE, "30", "75", 30, Color.BLACK));
            giyList.add(new GridInfo(GridLine.LINE, "20", "50", 20, Color.BLACK));
            giyList.add(new GridInfo(GridLine.LINE, "10", "25", 10, Color.BLACK));
            giyList.add(new GridInfo(GridLine.LINE, "0", "0", 0, Color.BLACK));
            giyList.add(new GridInfo(GridLine.DASHED, null, null, 35, Color.GRAY));
            giyList.add(new GridInfo(GridLine.DASHED, null, null, 25, Color.GRAY));
            giyList.add(new GridInfo(GridLine.DASHED, null, null, 15, Color.GRAY));
            giyList.add(new GridInfo(GridLine.DASHED, null, null, 5, Color.GRAY));
        }

        GraphAreaInfo gaInfo = new GraphAreaInfo();
        gaInfo.paddingTop = 40;
        gaInfo.paddingBottom = 40;
        gaInfo.paddingLeft = 17;
        gaInfo.paddingRight = 15;
        gaInfo.graphWidth = 1440 / 2;
        gaInfo.graphHeight = 480;
        gaInfo.minX = 0;
        gaInfo.maxX = endTimeLong - startTimeLong;
        gaInfo.minY = 0;
        gaInfo.maxY = maxValueY;
        gaInfo.unitX = gtlInfo.unitXFormat;
        gaInfo.unitY = String.format(gtlInfo.unitYFormat, 0, UPDATE_INTERVAL);
        gaInfo.textSize = gtlInfo.labelTextSize;

        Bitmap bitmap = createGraph(gaInfo, gixList, giyList, gvList, gtlInfo);
        return bitmap;
    }

    public Bitmap createGraph(GraphAreaInfo gaInfo, List<GridInfo> gixList, List<GridInfo> giyList,
            List<GraphValue> gvList, GraphTextLabelInfo gtlInfo) {
        Paint textPaint = new Paint();
        textPaint.setTextSize(gtlInfo.textSize);
        textPaint.setFakeBoldText(true);
        textPaint.setAntiAlias(true);

        int imageWidth = (int)(gaInfo.paddingLeft + gaInfo.graphWidth + gaInfo.paddingRight);
        int imageHeight = (int)(gaInfo.paddingTop + gaInfo.graphHeight + gaInfo.paddingBottom);
        Bitmap bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.RGB_565);
        bitmap.eraseColor(Color.WHITE);
        Canvas canvas = new Canvas(bitmap);
        GraphDrawer.createGraph(canvas, gaInfo, gixList, giyList, gvList);

        float pad = gtlInfo.textSize / 2f;

        if (gtlInfo.title != null) {
            CtkCanvasUtil.drawText(canvas, textPaint, pad, pad, gtlInfo.title, Gravity.GRAVITY_TOP,
                    Gravity.GRAVITY_LEFT);
        }
        if (gtlInfo.appName != null) {
            CtkCanvasUtil.drawText(canvas, textPaint, imageWidth - pad, pad, gtlInfo.appName,
                    Gravity.GRAVITY_TOP, Gravity.GRAVITY_RIGHT);
        }

        return bitmap;
    }

}
