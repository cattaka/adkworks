
package net.cattaka.droidrobo01.math;

import android.opengl.Matrix;

public class CtkMath {
    public static double DEG_TO_RAD = Math.PI / 180.0;

    public static double RAD_TO_DEG = 180.0 / Math.PI;

    public static float[] createVector3f() {
        return new float[3];
    }

    public static float[] createVector3f(float[] src) {
        return new float[] {
                src[0], src[1], src[2]
        };
    }

    public static float[] createVector3f(float x, float y, float z) {
        return new float[] {
                x, y, z
        };
    }

    public static float[] createVector4f() {
        return new float[4];
    }

    public static float[] createVector4f(float[] src) {
        return new float[] {
                src[0], src[1], src[2], src[3]
        };
    }

    public static float[] createVector4f(float a, float b, float c, float d) {
        return new float[] {
                a, b, c, d
        };
    }

    public static float[] createMatrix4f() {
        return new float[] {
                1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1
        };
    }

    public static void set(float[] arg, float x, float y, float z) {
        arg[0] = x;
        arg[1] = y;
        arg[2] = z;
    }

    public static void makeIdentityMatrix4f(float[] dst) {
        dst[0] = 1;
        dst[1] = 0;
        dst[2] = 0;
        dst[3] = 0;
        dst[4] = 0;
        dst[5] = 1;
        dst[6] = 0;
        dst[7] = 0;
        dst[8] = 0;
        dst[9] = 0;
        dst[10] = 1;
        dst[11] = 0;
        dst[12] = 0;
        dst[13] = 0;
        dst[14] = 0;
        dst[15] = 1;
    }

    public static float[] createMatrix4f(float[] src) {
        float[] mat = new float[16];
        System.arraycopy(src, 0, mat, 0, 16);
        return mat;
    }

    public static void add3F(float[] dst, float[] arg1, float[] arg2) {
        dst[0] = arg1[0] + arg2[0];
        dst[1] = arg1[1] + arg2[1];
        dst[2] = arg1[2] + arg2[2];
    }

    public static void addEq3F(float[] arg1, float[] arg2) {
        arg1[0] += arg2[0];
        arg1[1] += arg2[1];
        arg1[2] += arg2[2];
    }

    public static void sub3F(float[] dst, float[] arg1, float[] arg2) {
        dst[0] = arg1[0] - arg2[0];
        dst[1] = arg1[1] - arg2[1];
        dst[2] = arg1[2] - arg2[2];
    }

    public static void subEq3F(float[] arg1, float[] arg2) {
        arg1[0] -= arg2[0];
        arg1[1] -= arg2[1];
        arg1[2] -= arg2[2];
    }

    public static boolean isEqualDir(float[] arg1, float[] arg2) {
        return (arg1[1] * arg2[2] - arg1[2] * arg2[1] == 0)
                && (arg1[2] * arg2[0] - arg1[0] * arg2[2] == 0)
                && (arg1[0] * arg2[1] - arg1[1] * arg2[0] == 0);
    }

    public static float dot3F(float[] arg1, float[] arg2) {
        return arg1[0] * arg2[0] + arg1[1] * arg2[1] + arg1[2] * arg2[2];
    }

    public static void cross3F(float[] dst, float[] arg1, float[] arg2) {
        dst[0] = arg1[1] * arg2[2] - arg1[2] * arg2[1];
        dst[1] = arg1[2] * arg2[0] - arg1[0] * arg2[2];
        dst[2] = arg1[0] * arg2[1] - arg1[1] * arg2[0];
    }

    public static float angle3F(float[] arg1, float[] arg2) {
        float[] a1 = createVector3f();
        float[] a2 = createVector3f();
        float[] cross = createVector3f();
        normalizeEq3F(a1);
        normalizeEq3F(a2);
        float rad = (float)Math.acos(dot3F(a1, a2));
        cross3F(cross, a1, a2);
        if (length3F(cross) >= 0) {
            return rad;
        } else {
            return -rad;
        }
    }

    public static void scale3F(float[] dst, float[] arg, float scale) {
        dst[0] = arg[0] * scale;
        dst[1] = arg[1] * scale;
        dst[2] = arg[2] * scale;
    }

    public static void scaleEq3F(float[] arg, float scale) {
        arg[0] *= scale;
        arg[1] *= scale;
        arg[2] *= scale;
    }

    public static void scaleAdd3F(float[] dst, float[] arg1, float scale, float[] arg2) {
        dst[0] = arg1[0] + scale * arg2[0];
        dst[1] = arg1[1] + scale * arg2[1];
        dst[2] = arg1[2] + scale * arg2[2];
    }

    public static void scaleAddEq3F(float[] arg1, float scale, float[] arg2) {
        arg1[0] = arg1[0] + scale * arg2[0];
        arg1[1] = arg1[1] + scale * arg2[1];
        arg1[2] = arg1[2] + scale * arg2[2];
    }

    public static void copy3F(float[] dst, float[] arg) {
        dst[0] = arg[0];
        dst[1] = arg[1];
        dst[2] = arg[2];
    }

    public static void copyMatrix4f(float[] dst, float[] src) {
        System.arraycopy(src, 0, dst, 0, 16);
    }

    public static float length3F(float[] arg) {
        return (float)Math.sqrt(arg[0] * arg[0] + arg[1] * arg[1] + arg[2] * arg[2]);
    }

    public static float distance(float[] arg1, float[] arg2) {
        float x = arg1[0] - arg2[0];
        float y = arg1[1] - arg2[1];
        float z = arg1[2] - arg2[2];
        return (float)Math.sqrt(x * x + y * y + z * z);
    }

    public static float distanceSquare(float[] arg1, float[] arg2) {
        float x = arg1[0] - arg2[0];
        float y = arg1[1] - arg2[1];
        float z = arg1[2] - arg2[2];
        return x * x + y * y + z * z;
    }

    public static void normalize3F(float[] dst, float[] arg) {
        float l = (float)Math.sqrt(arg[0] * arg[0] + arg[1] * arg[1] + arg[2] * arg[2]);
        dst[0] = arg[0] / l;
        dst[1] = arg[1] / l;
        dst[2] = arg[2] / l;
    }

    public static void normalizeEq3F(float[] arg) {
        float l = (float)Math.sqrt(arg[0] * arg[0] + arg[1] * arg[1] + arg[2] * arg[2]);
        arg[0] = arg[0] / l;
        arg[1] = arg[1] / l;
        arg[2] = arg[2] / l;
    }

    public static boolean isZero3F(float[] arg) {
        return arg[0] == 0 && arg[1] == 0 && arg[2] == 0;
    }

    public static void transpose3F(float[] dst, float[] src, float[] mat) {
        dst[0] = src[0] * mat[0] + src[1] * mat[1] + src[2] * mat[2] + mat[3];
        dst[1] = src[0] * mat[4] + src[1] * mat[5] + src[2] * mat[6] + mat[7];
        dst[2] = src[0] * mat[8] + src[1] * mat[9] + src[2] * mat[10] + mat[11];
    }

    public static void transposeEq3F(float[] arg, float[] mat) {
        float t0 = arg[0] * mat[0] + arg[1] * mat[4] + arg[2] * mat[8] + mat[12];
        float t1 = arg[0] * mat[1] + arg[1] * mat[5] + arg[2] * mat[9] + mat[13];
        float t2 = arg[0] * mat[2] + arg[1] * mat[6] + arg[2] * mat[10] + mat[14];
        arg[0] = t0;
        arg[1] = t1;
        arg[2] = t2;
    }

    public static void rotateM(float[] dstMat, float[] arg1, float[] arg2) {
        float[] a1 = createVector3f();
        float[] a2 = createVector3f();
        float[] n = createVector3f();
        normalize3F(a1, arg1);
        normalize3F(a2, arg2);
        cross3F(n, a1, a2);
        float cosR = dot3F(a1, a2);
        float sinR = length3F(n);

        dstMat[0] = n[0] * n[0] + cosR * (1 - n[0] * n[0]);
        dstMat[1] = n[0] * n[1] * (1 - cosR) + n[2] * sinR;
        dstMat[2] = n[2] * n[0] * (1 - cosR) - n[1] * sinR;
        dstMat[3] = 0;

        dstMat[4] = n[0] * n[1] * (1 - cosR) - n[2] * sinR;
        dstMat[5] = n[1] * n[1] + cosR * (1 - n[1] * n[1]);
        dstMat[6] = n[1] * n[2] * (1 - cosR) + n[0] * sinR;
        dstMat[7] = 0;

        dstMat[8] = n[2] * n[0] * (1 - cosR) + n[1] * sinR;
        dstMat[9] = n[1] * n[2] * (1 - cosR) - n[0] * sinR;
        dstMat[10] = n[2] * n[2] + cosR * (1 - n[2] * n[2]);
        dstMat[11] = 0;

        dstMat[12] = 0;
        dstMat[13] = 0;
        dstMat[14] = 0;
        dstMat[15] = 1;
    }

    public static void lookAtM(float[] rm, float[] eye, float[] center, float[] up) {
        int rmOffset = 0;
        float eyeX = eye[0];
        float eyeY = eye[1];
        float eyeZ = eye[2];
        float centerX = center[0];
        float centerY = center[1];
        float centerZ = center[2];
        float upX = up[0];
        float upY = up[1];
        float upZ = up[2];

        lookAtM(rm, rmOffset, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
    }

    public static void lookAtM(float[] rm, int rmOffset, float eyeX, float eyeY, float eyeZ,
            float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {

        // See the OpenGL GLUT documentation for gluLookAt for a description
        // of the algorithm. We implement it in a straightforward way:

        float fx = centerX - eyeX;
        float fy = centerY - eyeY;
        float fz = centerZ - eyeZ;

        // Normalize f
        float rlf = 1.0f / Matrix.length(fx, fy, fz);
        fx *= rlf;
        fy *= rlf;
        fz *= rlf;

        // compute s = f x up (x means "cross product")
        float sx = fy * upZ - fz * upY;
        float sy = fz * upX - fx * upZ;
        float sz = fx * upY - fy * upX;

        // and normalize s
        float rls = 1.0f / Matrix.length(sx, sy, sz);
        sx *= rls;
        sy *= rls;
        sz *= rls;

        // compute u = s x f
        float ux = sy * fz - sz * fy;
        float uy = sz * fx - sx * fz;
        float uz = sx * fy - sy * fx;

        rm[rmOffset + 0] = sx;
        rm[rmOffset + 1] = ux;
        rm[rmOffset + 2] = -fx;
        rm[rmOffset + 3] = 0.0f;

        rm[rmOffset + 4] = sy;
        rm[rmOffset + 5] = uy;
        rm[rmOffset + 6] = -fy;
        rm[rmOffset + 7] = 0.0f;

        rm[rmOffset + 8] = sz;
        rm[rmOffset + 9] = uz;
        rm[rmOffset + 10] = -fz;
        rm[rmOffset + 11] = 0.0f;

        rm[rmOffset + 12] = 0.0f;
        rm[rmOffset + 13] = 0.0f;
        rm[rmOffset + 14] = 0.0f;
        rm[rmOffset + 15] = 1.0f;

        Matrix.translateM(rm, rmOffset, -eyeX, -eyeY, -eyeZ);
    }

    public static void lookAtDirectionM(float[] rm, float[] eye, float[] dir, float[] up) {
        lookAtDirectionM(rm, eye[0], eye[1], eye[2], dir[0], dir[1], dir[2], up[0], up[1], up[2]);
    }

    public static void lookAtDirectionM(float[] rm, float eyeX, float eyeY, float eyeZ, float dirX,
            float dirY, float dirZ, float upX, float upY, float upZ) {

        // See the OpenGL GLUT documentation for gluLookAt for a description
        // of the algorithm. We implement it in a straightforward way:

        // Normalize f
        float rlf = (float)(1.0 / Math.sqrt((dirX * dirX + dirY * dirY + dirZ * dirZ)));
        dirX *= rlf;
        dirY *= rlf;
        dirZ *= rlf;

        // compute s = f x up (x means "cross product")
        float sx = dirY * upZ - dirZ * upY;
        float sy = dirZ * upX - dirX * upZ;
        float sz = dirX * upY - dirY * upX;

        // and normalize s
        float rls = (float)(1.0 / Math.sqrt(sx * sx + sy * sy + sz * sz));
        sx *= rls;
        sy *= rls;
        sz *= rls;

        // compute u = s x f
        float ux = sy * dirZ - sz * dirY;
        float uy = sz * dirX - sx * dirZ;
        float uz = sx * dirY - sy * dirX;

        rm[0] = sx;
        rm[1] = ux;
        rm[2] = -dirX;
        rm[3] = 0.0f;

        rm[4] = sy;
        rm[5] = uy;
        rm[6] = -dirY;
        rm[7] = 0.0f;

        rm[8] = sz;
        rm[9] = uz;
        rm[10] = -dirZ;
        rm[11] = 0.0f;

        rm[12] = 0.0f;
        rm[13] = 0.0f;
        rm[14] = 0.0f;
        rm[15] = 1.0f;

        Matrix.translateM(rm, 0, -eyeX, -eyeY, -eyeZ);
    }

    public static boolean pickPoint(float[] dst3f, float[] start3f, float[] dir3f, float[] plain4f) {
        float t = -(plain4f[0] * start3f[0] + plain4f[1] * start3f[1] + plain4f[2] * start3f[2] + plain4f[3])
                / (plain4f[0] * dir3f[0] + plain4f[1] * dir3f[1] + plain4f[2] * dir3f[2]);
        if (Float.isInfinite(t) || Float.isNaN(t)) {
            return false;
        }
        scaleAdd3F(dst3f, start3f, t, dir3f);
        return true;
    }

    public static float calcDistance(float[] pos, float[] dir, float[] target) {
        float[] vec = CtkMath.createMatrix4f();
        sub3F(vec, target, pos);
        float l1 = length3F(dir);
        float d = dot3F(dir, vec) / (l1 * l1);

        scaleAddEq3F(vec, -d, dir);
        return length3F(vec);
    }

    public static float determinant4f(float[] m) {
        return m[12] * m[9] * m[6] * m[3] - m[8] * m[13] * m[6] * m[3] - m[12] * m[5] * m[10]
                * m[3] + m[4] * m[13] * m[10] * m[3] + m[8] * m[5] * m[14] * m[3] - m[4] * m[9]
                * m[14] * m[3] - m[12] * m[9] * m[2] * m[7] + m[8] * m[13] * m[2] * m[7] + m[12]
                * m[1] * m[10] * m[7] - m[0] * m[13] * m[10] * m[7] - m[8] * m[1] * m[14] * m[7]
                + m[0] * m[9] * m[14] * m[7] + m[12] * m[5] * m[2] * m[11] - m[4] * m[13] * m[2]
                * m[11] - m[12] * m[1] * m[6] * m[11] + m[0] * m[13] * m[6] * m[11] + m[4] * m[1]
                * m[14] * m[11] - m[0] * m[5] * m[14] * m[11] - m[8] * m[5] * m[2] * m[15] + m[4]
                * m[9] * m[2] * m[15] + m[8] * m[1] * m[6] * m[15] - m[0] * m[9] * m[6] * m[15]
                - m[4] * m[1] * m[10] * m[15] + m[0] * m[5] * m[10] * m[15];
    }

    public static float determinant4fLight(float[] m) {
        float m0_5 = m[0] * m[5];
        float m0_9 = m[0] * m[9];
        float m0_13 = m[0] * m[13];
        float m2_7 = m[2] * m[7];
        float m2_11 = m[2] * m[11];
        float m2_15 = m[2] * m[15];
        float m4_1 = m[4] * m[1];
        float m4_9 = m[4] * m[9];
        float m4_13 = m[4] * m[13];
        float m6_3 = m[6] * m[3];
        float m6_11 = m[6] * m[11];
        float m6_15 = m[6] * m[15];
        float m8_1 = m[8] * m[1];
        float m8_5 = m[8] * m[5];
        float m8_13 = m[8] * m[13];
        float m10_3 = m[10] * m[3];
        float m10_7 = m[10] * m[7];
        float m10_15 = m[10] * m[15];
        float m12_1 = m[12] * m[1];
        float m12_5 = m[12] * m[5];
        float m12_9 = m[12] * m[9];
        float m14_3 = m[14] * m[3];
        float m14_7 = m[14] * m[7];
        float m14_11 = m[14] * m[11];
        return m12_9 * m6_3 - m8_13 * m6_3 - m12_5 * m10_3 + m4_13 * m10_3 + m8_5 * m14_3 - m4_9
                * m14_3 - m12_9 * m2_7 + m8_13 * m2_7 + m12_1 * m10_7 - m0_13 * m10_7 - m8_1
                * m14_7 + m0_9 * m14_7 + m12_5 * m2_11 - m4_13 * m2_11 - m12_1 * m6_11 + m0_13
                * m6_11 + m4_1 * m14_11 - m0_5 * m14_11 - m8_5 * m2_15 + m4_9 * m2_15 + m8_1
                * m6_15 - m0_9 * m6_15 - m4_1 * m10_15 + m0_5 * m10_15;
    }

    public static boolean inverseMatrix4f(float[] out, float[] m) {
        float x = determinant4f(m);
        if (x == 0) {
            return false;
        }

        out[0] = (-m[13] * m[10] * m[7] + m[9] * m[14] * m[7] + m[13] * m[6] * m[11] - m[5] * m[14]
                * m[11] - m[9] * m[6] * m[15] + m[5] * m[10] * m[15])
                / x;
        out[4] = (m[12] * m[10] * m[7] - m[8] * m[14] * m[7] - m[12] * m[6] * m[11] + m[4] * m[14]
                * m[11] + m[8] * m[6] * m[15] - m[4] * m[10] * m[15])
                / x;
        out[8] = (-m[12] * m[9] * m[7] + m[8] * m[13] * m[7] + m[12] * m[5] * m[11] - m[4] * m[13]
                * m[11] - m[8] * m[5] * m[15] + m[4] * m[9] * m[15])
                / x;
        out[12] = (m[12] * m[9] * m[6] - m[8] * m[13] * m[6] - m[12] * m[5] * m[10] + m[4] * m[13]
                * m[10] + m[8] * m[5] * m[14] - m[4] * m[9] * m[14])
                / x;
        out[1] = (m[13] * m[10] * m[3] - m[9] * m[14] * m[3] - m[13] * m[2] * m[11] + m[1] * m[14]
                * m[11] + m[9] * m[2] * m[15] - m[1] * m[10] * m[15])
                / x;
        out[5] = (-m[12] * m[10] * m[3] + m[8] * m[14] * m[3] + m[12] * m[2] * m[11] - m[0] * m[14]
                * m[11] - m[8] * m[2] * m[15] + m[0] * m[10] * m[15])
                / x;
        out[9] = (m[12] * m[9] * m[3] - m[8] * m[13] * m[3] - m[12] * m[1] * m[11] + m[0] * m[13]
                * m[11] + m[8] * m[1] * m[15] - m[0] * m[9] * m[15])
                / x;
        out[13] = (-m[12] * m[9] * m[2] + m[8] * m[13] * m[2] + m[12] * m[1] * m[10] - m[0] * m[13]
                * m[10] - m[8] * m[1] * m[14] + m[0] * m[9] * m[14])
                / x;
        out[2] = (-m[13] * m[6] * m[3] + m[5] * m[14] * m[3] + m[13] * m[2] * m[7] - m[1] * m[14]
                * m[7] - m[5] * m[2] * m[15] + m[1] * m[6] * m[15])
                / x;
        out[6] = (m[12] * m[6] * m[3] - m[4] * m[14] * m[3] - m[12] * m[2] * m[7] + m[0] * m[14]
                * m[7] + m[4] * m[2] * m[15] - m[0] * m[6] * m[15])
                / x;
        out[10] = (-m[12] * m[5] * m[3] + m[4] * m[13] * m[3] + m[12] * m[1] * m[7] - m[0] * m[13]
                * m[7] - m[4] * m[1] * m[15] + m[0] * m[5] * m[15])
                / x;
        out[14] = (m[12] * m[5] * m[2] - m[4] * m[13] * m[2] - m[12] * m[1] * m[6] + m[0] * m[13]
                * m[6] + m[4] * m[1] * m[14] - m[0] * m[5] * m[14])
                / x;
        out[3] = (m[9] * m[6] * m[3] - m[5] * m[10] * m[3] - m[9] * m[2] * m[7] + m[1] * m[10]
                * m[7] + m[5] * m[2] * m[11] - m[1] * m[6] * m[11])
                / x;
        out[7] = (-m[8] * m[6] * m[3] + m[4] * m[10] * m[3] + m[8] * m[2] * m[7] - m[0] * m[10]
                * m[7] - m[4] * m[2] * m[11] + m[0] * m[6] * m[11])
                / x;
        out[11] = (m[8] * m[5] * m[3] - m[4] * m[9] * m[3] - m[8] * m[1] * m[7] + m[0] * m[9]
                * m[7] + m[4] * m[1] * m[11] - m[0] * m[5] * m[11])
                / x;
        out[15] = (-m[8] * m[5] * m[2] + m[4] * m[9] * m[2] + m[8] * m[1] * m[6] - m[0] * m[9]
                * m[6] - m[4] * m[1] * m[10] + m[0] * m[5] * m[10])
                / x;

        return true;
    }

    public static boolean inverseMatrix4fLight(float[] out, float[] m) {
        float m0_5 = m[0] * m[5];
        float m0_9 = m[0] * m[9];
        float m0_13 = m[0] * m[13];
        float m2_7 = m[2] * m[7];
        float m2_11 = m[2] * m[11];
        float m2_15 = m[2] * m[15];
        float m4_1 = m[4] * m[1];
        float m4_9 = m[4] * m[9];
        float m4_13 = m[4] * m[13];
        float m6_3 = m[6] * m[3];
        float m6_11 = m[6] * m[11];
        float m6_15 = m[6] * m[15];
        float m8_1 = m[8] * m[1];
        float m8_5 = m[8] * m[5];
        float m8_13 = m[8] * m[13];
        float m10_3 = m[10] * m[3];
        float m10_7 = m[10] * m[7];
        float m10_15 = m[10] * m[15];
        float m12_1 = m[12] * m[1];
        float m12_5 = m[12] * m[5];
        float m12_9 = m[12] * m[9];
        float m14_3 = m[14] * m[3];
        float m14_7 = m[14] * m[7];
        float m14_11 = m[14] * m[11];
        float x = m12_9 * m6_3 - m8_13 * m6_3 - m12_5 * m10_3 + m4_13 * m10_3 + m8_5 * m14_3 - m4_9
                * m14_3 - m12_9 * m2_7 + m8_13 * m2_7 + m12_1 * m10_7 - m0_13 * m10_7 - m8_1
                * m14_7 + m0_9 * m14_7 + m12_5 * m2_11 - m4_13 * m2_11 - m12_1 * m6_11 + m0_13
                * m6_11 + m4_1 * m14_11 - m0_5 * m14_11 - m8_5 * m2_15 + m4_9 * m2_15 + m8_1
                * m6_15 - m0_9 * m6_15 - m4_1 * m10_15 + m0_5 * m10_15;
        if (x == 0) {
            return false;
        }

        out[0] = (-m[13] * m10_7 + m[9] * m14_7 + m[13] * m6_11 - m[5] * m14_11 - m[9] * m6_15 + m[5]
                * m10_15)
                / x;
        out[4] = (m[12] * m10_7 - m[8] * m14_7 - m[12] * m6_11 + m[4] * m14_11 + m[8] * m6_15 - m[4]
                * m10_15)
                / x;
        out[8] = (-m12_9 * m[7] + m8_13 * m[7] + m12_5 * m[11] - m4_13 * m[11] - m8_5 * m[15] + m4_9
                * m[15])
                / x;
        out[12] = (m12_9 * m[6] - m8_13 * m[6] - m12_5 * m[10] + m4_13 * m[10] + m8_5 * m[14] - m4_9
                * m[14])
                / x;
        out[1] = (m[13] * m10_3 - m[9] * m14_3 - m[13] * m2_11 + m[1] * m14_11 + m[9] * m2_15 - m[1]
                * m10_15)
                / x;
        out[5] = (-m[12] * m10_3 + m[8] * m14_3 + m[12] * m2_11 - m[0] * m14_11 - m[8] * m2_15 + m[0]
                * m10_15)
                / x;
        out[9] = (m12_9 * m[3] - m8_13 * m[3] - m12_1 * m[11] + m0_13 * m[11] + m8_1 * m[15] - m0_9
                * m[15])
                / x;
        out[13] = (-m12_9 * m[2] + m8_13 * m[2] + m12_1 * m[10] - m0_13 * m[10] - m8_1 * m[14] + m0_9
                * m[14])
                / x;
        out[2] = (-m[13] * m6_3 + m[5] * m14_3 + m[13] * m2_7 - m[1] * m14_7 - m[5] * m2_15 + m[1]
                * m6_15)
                / x;
        out[6] = (m[12] * m6_3 - m[4] * m14_3 - m[12] * m2_7 + m[0] * m14_7 + m[4] * m2_15 - m[0]
                * m6_15)
                / x;
        out[10] = (-m12_5 * m[3] + m4_13 * m[3] + m12_1 * m[7] - m0_13 * m[7] - m4_1 * m[15] + m0_5
                * m[15])
                / x;
        out[14] = (m12_5 * m[2] - m4_13 * m[2] - m12_1 * m[6] + m0_13 * m[6] + m4_1 * m[14] - m0_5
                * m[14])
                / x;
        out[3] = (m[9] * m6_3 - m[5] * m10_3 - m[9] * m2_7 + m[1] * m10_7 + m[5] * m2_11 - m[1]
                * m6_11)
                / x;
        out[7] = (-m[8] * m6_3 + m[4] * m10_3 + m[8] * m2_7 - m[0] * m10_7 - m[4] * m2_11 + m[0]
                * m6_11)
                / x;
        out[11] = (m8_5 * m[3] - m4_9 * m[3] - m8_1 * m[7] + m0_9 * m[7] + m4_1 * m[11] - m0_5
                * m[11])
                / x;
        out[15] = (-m8_5 * m[2] + m4_9 * m[2] + m8_1 * m[6] - m0_9 * m[6] - m4_1 * m[10] + m0_5
                * m[10])
                / x;

        return true;
    }

    public static double degToRad(double arg) {
        return arg * DEG_TO_RAD;
    }

    public static double radToDeg(double arg) {
        return arg * RAD_TO_DEG;
    }
}
