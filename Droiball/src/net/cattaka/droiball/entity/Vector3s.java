
package net.cattaka.droiball.entity;

public class Vector3s {
    private short x;

    private short y;

    private short z;

    public Vector3s() {
    }

    public Vector3s(short x, short y, short z) {
        super();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void read16u(byte[] data, int offset) {
        x = (short)((0xFF & data[0 + offset]) | ((0xFF & data[1 + offset]) << 8));
        y = (short)((0xFF & data[2 + offset]) | ((0xFF & data[3 + offset]) << 8));
        z = (short)((0xFF & data[4 + offset]) | ((0xFF & data[5 + offset]) << 8));
    }

    public void set(Vector3s src) {
        x = src.x;
        y = src.y;
        z = src.z;
    }

    public short getX() {
        return x;
    }

    public void setX(short x) {
        this.x = x;
    }

    public short getY() {
        return y;
    }

    public void setY(short y) {
        this.y = y;
    }

    public short getZ() {
        return z;
    }

    public void setZ(short z) {
        this.z = z;
    }
}
