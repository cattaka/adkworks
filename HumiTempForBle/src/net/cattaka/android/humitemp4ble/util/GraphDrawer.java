
package net.cattaka.android.humitemp4ble.util;

import java.util.List;

import net.cattaka.android.humitemp4ble.util.CtkCanvasUtil.Gravity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class GraphDrawer {
    public static class GraphAreaInfo {
        public float paddingTop;

        public float paddingBottom;

        public float paddingLeft;

        public float paddingRight;

        public float graphWidth;

        public float graphHeight;

        public float minX;

        public float maxX;

        public float minY;

        public float maxY;

        public String unitX;

        public String unitY;

        public float scaleY1 = 1;

        public float scaleY2 = 1;

        public float textSize;
    }

    public static class GridInfo {
        public GridLine gridLine;

        public String label;

        public String label2;

        public float value;

        public int labelColor = Color.BLACK;

        public GridInfo() {
        }

        public GridInfo(GridLine gridLine, String label, String label2, float value, int labelColor) {
            super();
            this.gridLine = gridLine;
            this.label = label;
            this.label2 = label2;
            this.value = value;
            this.labelColor = labelColor;
        }

    }

    public enum GridLine {
        LINE, DASHED, NONE
    };

    public static class GraphValue {
        public float startX;

        public float endX;

        // public float startY;

        public float endY1;

        public float endY2;

        public int count;
    }

    public static class GraphLabels {
        public String title;

        public String appName;

        public String counts;
    }

    public static void createGraph(Canvas canvas, GraphAreaInfo gaInfo, List<GridInfo> gixList,
            List<GridInfo> giyList, List<GraphValue> gvList) {
        Paint gridLinePaint = new Paint();
        Paint gridDashedLinePaint = new Paint();
        Paint barPaint1 = new Paint();
        Paint barPaint2 = new Paint();
        Paint labelPaint = new Paint();
        Paint boldLabelPaint = new Paint();
        gridLinePaint.setColor(Color.GRAY);
        gridDashedLinePaint.setColor(Color.LTGRAY);
        barPaint1.setColor(Color.BLUE);
        barPaint1.setStyle(Paint.Style.FILL);
        barPaint2.setColor(Color.RED);
        barPaint2.setStyle(Paint.Style.FILL);
        labelPaint.setTextSize(gaInfo.textSize);
        labelPaint.setAntiAlias(true);
        boldLabelPaint.setFakeBoldText(true);
        boldLabelPaint.setTextSize(gaInfo.textSize);
        boldLabelPaint.setAntiAlias(true);

        // Y軸のグリッドを書く
        for (GridInfo giy : giyList) {
            float y = gaInfo.paddingTop + gaInfo.graphHeight - gaInfo.graphHeight
                    * (giy.value - gaInfo.minY) / (gaInfo.maxY - gaInfo.minY);
            if (giy.label != null) {
                labelPaint.setColor(giy.labelColor);
                CtkCanvasUtil.drawText(canvas, labelPaint, gaInfo.paddingLeft, y, giy.label,
                        Gravity.GRAVITY_CENTER, Gravity.GRAVITY_RIGHT);
            }
            if (giy.label2 != null) {
                labelPaint.setColor(giy.labelColor);
                CtkCanvasUtil.drawText(canvas, labelPaint, gaInfo.paddingLeft + gaInfo.graphWidth,
                        y, giy.label2, Gravity.GRAVITY_CENTER, Gravity.GRAVITY_LEFT);
            }
            if (giy.gridLine == GridLine.LINE) {
                canvas.drawLine(gaInfo.paddingLeft, y, gaInfo.paddingLeft + gaInfo.graphWidth, y,
                        gridLinePaint);
            }
            if (giy.gridLine == GridLine.DASHED) {
                canvas.drawLine(gaInfo.paddingLeft, y, gaInfo.paddingLeft + gaInfo.graphWidth, y,
                        gridDashedLinePaint);
            }
        }

        // X軸のグリッドを書く
        for (GridInfo gix : gixList) {
            float x = gaInfo.paddingLeft + gaInfo.graphWidth * (gix.value - gaInfo.minX)
                    / (gaInfo.maxX - gaInfo.minX);
            if (gix.label != null) {
                labelPaint.setColor(gix.labelColor);
                CtkCanvasUtil.drawText(canvas, labelPaint, x, gaInfo.paddingTop
                        + gaInfo.graphHeight, gix.label, Gravity.GRAVITY_TOP,
                        Gravity.GRAVITY_CENTER);
            }
            if (gix.gridLine == GridLine.LINE) {
                canvas.drawLine(x, gaInfo.paddingTop, x, gaInfo.paddingTop + gaInfo.graphHeight,
                        gridLinePaint);
            }
            if (gix.gridLine == GridLine.DASHED) {
                canvas.drawLine(x, gaInfo.paddingTop, x, gaInfo.paddingTop + gaInfo.graphHeight,
                        gridDashedLinePaint);
            }
        }

        // 値を描画する
        GraphValue lastGv = gvList.get(0);
        for (int i = 1; i < gvList.size(); i++) {
            GraphValue nextGv = gvList.get(i);
            {
                float sx = gaInfo.paddingLeft
                        + (gaInfo.graphWidth * (lastGv.startX - gaInfo.minX) / (gaInfo.maxX - gaInfo.minX));
                float sy = gaInfo.paddingTop
                        + gaInfo.graphHeight
                        - (gaInfo.graphHeight * (lastGv.endY1 - gaInfo.minY) / (gaInfo.maxY - gaInfo.minY))
                        - 1;
                float ex = gaInfo.paddingLeft
                        + (gaInfo.graphWidth * (nextGv.startX - gaInfo.minX) / (gaInfo.maxX - gaInfo.minX));
                float ey = gaInfo.paddingTop
                        + gaInfo.graphHeight
                        - (gaInfo.graphHeight * (nextGv.endY1 - gaInfo.minY) / (gaInfo.maxY - gaInfo.minY))
                        - 1;
                canvas.drawLine(sx, sy, ex, ey, barPaint1);
            }
            {
                float sx = gaInfo.paddingLeft
                        + (gaInfo.graphWidth * (lastGv.startX - gaInfo.minX) / (gaInfo.maxX - gaInfo.minX));
                float sy = gaInfo.paddingTop
                        + gaInfo.graphHeight
                        - (gaInfo.graphHeight * (lastGv.endY2 - gaInfo.minY) / (gaInfo.maxY - gaInfo.minY))
                        - 1;
                float ex = gaInfo.paddingLeft
                        + (gaInfo.graphWidth * (nextGv.startX - gaInfo.minX) / (gaInfo.maxX - gaInfo.minX));
                float ey = gaInfo.paddingTop
                        + gaInfo.graphHeight
                        - (gaInfo.graphHeight * (nextGv.endY2 - gaInfo.minY) / (gaInfo.maxY - gaInfo.minY))
                        - 1;
                canvas.drawLine(sx, sy, ex, ey, barPaint2);
            }
            lastGv = nextGv;
        }

        // XY軸の単位を描画
        {
            labelPaint.setColor(Color.BLACK);
            CtkCanvasUtil.drawText(canvas, boldLabelPaint, gaInfo.paddingLeft + gaInfo.graphWidth
                    + gaInfo.textSize, gaInfo.paddingTop + gaInfo.graphHeight + gaInfo.textSize,
                    gaInfo.unitX, Gravity.GRAVITY_TOP, Gravity.GRAVITY_RIGHT);
            CtkCanvasUtil.drawText(canvas, boldLabelPaint, gaInfo.paddingLeft - gaInfo.textSize,
                    gaInfo.paddingTop - gaInfo.textSize / 2f, gaInfo.unitY, Gravity.GRAVITY_BOTTOM,
                    Gravity.GRAVITY_LEFT);
        }
    }
}
