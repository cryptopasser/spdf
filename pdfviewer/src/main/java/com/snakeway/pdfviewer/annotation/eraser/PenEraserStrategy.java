package com.snakeway.pdfviewer.annotation.eraser;

import android.graphics.Point;
import android.graphics.Rect;

import com.snakeway.pdflibrary.util.SizeF;
import com.snakeway.pdfviewer.CoordinateUtils;
import com.snakeway.pdfviewer.PDFView;
import com.snakeway.pdfviewer.annotation.PenAnnotation;
import com.snakeway.pdfviewer.annotation.base.BaseAnnotation;

/**
 * @author snakeway
 */
public class PenEraserStrategy implements EraserStrategy {

    @Override
    public boolean erase(PDFView view, BaseAnnotation annotation, Rect rect) {
        if (annotation instanceof PenAnnotation) {
            PenAnnotation a = (PenAnnotation) annotation;
            Point lastPoint = null;
            for (SizeF sizeF : a.data) {
                Point poi = CoordinateUtils.toPdfPointCoordinateDesc(view, a.page, sizeF.getWidth(), sizeF.getHeight());
                if (lastPoint == null) {
                    lastPoint = poi;
                    continue;
                }
                if (lastPoint.x == poi.x && lastPoint.y == poi.y && rect.contains(poi.x, poi.y)) {
                    return true;
                } else if (intersectionRect(rect, lastPoint, poi)) {
                    return true;
                }
                lastPoint = poi;
            }
        }
        return false;
    }

    /**
     * 两线段是否相交
     *
     * @param l1x1 线段1的x1
     * @param l1y1 线段1的y1
     * @param l1x2 线段1的x2
     * @param l1y2 线段1的y2
     * @param l2x1 线段2的x1
     * @param l2y1 线段2的y1
     * @param l2x2 线段2的x2
     * @param l2y2 线段2的y2
     * @return 是否相交
     */
    private boolean intersection(
            double l1x1, double l1y1, double l1x2, double l1y2,
            double l2x1, double l2y1, double l2x2, double l2y2
    ) {
        // 快速排斥实验 首先判断两条线段在 x 以及 y 坐标的投影是否有重合。 有一个为真，则代表两线段必不可交。
        if (Math.max(l1x1, l1x2) < Math.min(l2x1, l2x2)
                || Math.max(l1y1, l1y2) < Math.min(l2y1, l2y2)
                || Math.max(l2x1, l2x2) < Math.min(l1x1, l1x2)
                || Math.max(l2y1, l2y2) < Math.min(l1y1, l1y2)) {
            return false;
        }
        // 跨立实验  如果相交则矢量叉积异号或为零，大于零则不相交
        if ((((l1x1 - l2x1) * (l2y2 - l2y1) - (l1y1 - l2y1) * (l2x2 - l2x1))
                * ((l1x2 - l2x1) * (l2y2 - l2y1) - (l1y2 - l2y1) * (l2x2 - l2x1))) > 0
                || (((l2x1 - l1x1) * (l1y2 - l1y1) - (l2y1 - l1y1) * (l1x2 - l1x1))
                * ((l2x2 - l1x1) * (l1y2 - l1y1) - (l2y2 - l1y1) * (l1x2 - l1x1))) > 0) {
            return false;
        }
        return true;
    }

    /**
     * 判断线段是否于矩形相交
     */
    private boolean intersectionRect(Rect rect, Point p1, Point p2) {
        boolean intersectionLeft = intersection(rect.left, rect.top, rect.left, rect.bottom, p1.x, p1.y, p2.x, p2.y);
        if (intersectionLeft) {
            return intersectionLeft;
        }
        boolean intersectionTop = intersection(rect.left, rect.top, rect.right, rect.top, p1.x, p1.y, p2.x, p2.y);
        if (intersectionTop) {
            return intersectionTop;
        }
        boolean intersectionRight = intersection(rect.right, rect.top, rect.right, rect.bottom, p1.x, p1.y, p2.x, p2.y);
        if (intersectionRight) {
            return intersectionRight;
        }
        boolean intersectionBottom = intersection(rect.left, rect.bottom, rect.right, rect.bottom, p1.x, p1.y, p2.x, p2.y);
        if (intersectionBottom) {
            return intersectionBottom;
        }
        return false;
    }
}
