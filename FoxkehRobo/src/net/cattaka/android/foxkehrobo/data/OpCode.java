
package net.cattaka.android.foxkehrobo.data;


public enum OpCode {
    ECHO((byte)0), //
    SERVO_ANGLES((byte)1), //
    EYE_LEDS((byte)2), //
    POSE((byte)3), //
    REQ_ACCEL((byte)4), //
    RES_ACCEL((byte)5), //
    UNKNOWN((byte)-1);

    private byte value;

    private OpCode(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static OpCode fromValue(byte value) {
        for (OpCode oc : values()) {
            if (oc.getValue() == value) {
                return oc;
            }
        }
        return UNKNOWN;
    }
}
