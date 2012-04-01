
package net.cattaka.robotarm01.math;

import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import net.cattaka.robotarm01.ArmSetting;

public class ArmAngleUtil {
    public static class AngleBundle {
        float[] p1 = CtkMath.createVector3f();

        float[] p2 = CtkMath.createVector3f();

        double cos3;

        double alpha;

        double beta;

        double cos2;

        double sin2;

        double cos5;

        double sin5;

        public double angle1;

        public double angle2;

        public double angle3;

        public double angle4;

        public double angle5;

        public float[] toAnglesFloatArray() {
            return new float[] {
                    (float)angle1, (float)angle2, (float)angle3, (float)angle4, (float)angle5, 0
            };
        }
    };

    public static AngleBundle[] calcAngles(float[] lengths, float[] position, float[] frontVec,
            float[] upVec) {
        float[] p = position;
        float[] a = frontVec;
        float[] b = upVec;
        float l1 = lengths[0];
        float l2 = lengths[1];
        float l3 = lengths[2];
        float l45 = lengths[3];

        AngleBundle[] bd = new AngleBundle[8];
        for (int i = 0; i < bd.length; i++) {
            bd[i] = new AngleBundle();
        }
        { // θ1の算出
            double angle1 = validateAngle(acos(p[0] / sqrt(pow2(p[0]) + pow2(p[1]))));
            if (p[1] < 0) {
                angle1 = -angle1;
            }
            double angle1d = validateAngle(angle1 + Math.PI);
            for (int i = 0; i < bd.length / 2; i++) {
                bd[i].angle1 = angle1;
            }
            for (int i = bd.length / 2; i < bd.length; i++) {
                bd[i].angle1 = angle1d;
            }
        }
        { // p1の算出
            for (int i = 0; i < bd.length; i++) {
                bd[i].p1[0] = p[0] - a[0] * l45;
                bd[i].p1[1] = p[1] - a[1] * l45;
                bd[i].p1[2] = p[2] - a[2] * l45 - l1;
            }
        }
        { // p2の算出
            for (int i = 0; i < bd.length; i++) {
                bd[i].p2[0] = (float)(cos(bd[i].angle1) * bd[i].p1[0] + sin(bd[i].angle1)
                        * bd[i].p1[1]);
                bd[i].p2[1] = 0;
                bd[i].p2[2] = bd[i].p1[2];
            }
        }
        { // cos3,θ3
            for (int i = 0; i < bd.length; i++) {
                bd[i].cos3 = (pow2(bd[i].p2[0]) + pow2(bd[i].p2[2]) - pow2(l2) - pow2(l3))
                        / (2 * l2 * l3);
                bd[i].angle3 = Math.acos(bd[i].cos3);
            }
            bd[2].angle3 = -bd[2].angle3;
            bd[3].angle3 = -bd[3].angle3;
            bd[6].angle3 = -bd[6].angle3;
            bd[7].angle3 = -bd[7].angle3;
        }
        { // α,β,cos2,sin2,θ2
            for (int i = 0; i < bd.length; i++) {
                bd[i].alpha = l3 * sin(bd[i].angle3);
                bd[i].beta = l2 + l3 * cos(bd[i].angle3);
                bd[i].cos2 = (bd[i].alpha * bd[i].p2[0] + bd[i].beta * bd[i].p2[2])
                        / (pow2(bd[i].alpha) + pow2(bd[i].beta));
                bd[i].sin2 = (bd[i].beta * bd[i].p2[0] - bd[i].alpha * bd[i].p2[2])
                        / (pow2(bd[i].alpha) + pow2(bd[i].beta));
                bd[i].angle2 = (bd[i].sin2 >= 0) ? Math.acos(bd[i].cos2) : -Math.acos(bd[i].cos2);
            }
        }
        { // cos5,sin5,θ5
            for (int i = 0; i < bd.length; i++) {
                bd[i].cos5 = (cos(bd[i].angle1) * b[0] + sin(bd[i].angle1) * b[1]) / (-a[2]);
                bd[i].sin5 = (sin(bd[i].angle1) * b[0] - cos(bd[i].angle1) * b[1]);
                bd[i].angle5 = (bd[i].sin5 >= 0) ? Math.acos(bd[i].cos5) : -Math.acos(bd[i].cos5);
            }
            bd[1].angle5 = validateAngle(bd[1].angle5 + Math.PI);
            bd[3].angle5 = validateAngle(bd[3].angle5 + Math.PI);
            bd[5].angle5 = validateAngle(bd[5].angle5 + Math.PI);
            bd[7].angle5 = validateAngle(bd[7].angle5 + Math.PI);
        }
        { // θ4
            for (int i = 0; i < bd.length; i++) {
                double cos234 = a[2];
                double sin234 = a[0] * cos(bd[i].angle1) + a[01] * sin(bd[i].angle1);
                double angle234 = (sin234 >= 0) ? Math.acos(cos234) : -Math.acos(cos234);
                bd[i].angle4 = validateAngle(angle234 - bd[i].angle2 - bd[i].angle3);
            }
        }
        return bd;
    }

    /**
     * 角度を(-π,π)の範囲に変換する。
     * 
     * @param angle
     * @return
     */
    private static double validateAngle(double angle) {
        angle = (angle % (2 * Math.PI));
        if (angle <= -Math.PI) {
            angle = angle + (2 * Math.PI);
        } else if (angle > Math.PI) {
            angle = angle - (2 * Math.PI);
        }
        return angle;
    }

    private static float pow2(float arg) {
        return arg * arg;
    }

    private static double pow2(double arg) {
        return arg * arg;
    }

    public static double digToRad(double arg) {
        return (arg / 180) * Math.PI;
    }

    public static double radToDig(double arg) {
        return (arg / Math.PI) * 180;
    }

    public static int pickAvailableIndex(AngleBundle[] angleBundles, ArmSetting armSetting) {
        for (int i = 0; i < angleBundles.length; i++) {
            if (isAvailableIndex(angleBundles, armSetting, i)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isAvailableIndex(AngleBundle[] angleBundles, ArmSetting armSetting,
            int index) {
        AngleBundle ab = angleBundles[index];
        double angle1 = CtkMath.radToDeg(ab.angle1);
        double angle2 = CtkMath.radToDeg(ab.angle2);
        double angle3 = CtkMath.radToDeg(ab.angle3);
        double angle4 = CtkMath.radToDeg(ab.angle4);
        double angle5 = CtkMath.radToDeg(ab.angle5);
        if (armSetting.getServo1Min() <= angle1 && angle1 <= armSetting.getServo1Max() //
                && armSetting.getServo2Min() <= angle2 && angle2 <= armSetting.getServo2Max() //
                && armSetting.getServo3Min() <= angle3 && angle3 <= armSetting.getServo3Max() //
                && armSetting.getServo4Min() <= angle4 && angle4 <= armSetting.getServo4Max() //
                && armSetting.getServo5Min() <= angle5 && angle5 <= armSetting.getServo5Max() //
        ) {
            return true;
        } else {
            return false;
        }
    }
}
