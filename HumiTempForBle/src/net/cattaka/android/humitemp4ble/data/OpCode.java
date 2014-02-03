
package net.cattaka.android.humitemp4ble.data;

public enum OpCode {
    REQUEST((byte)1), //
    RESPONSE((byte)2), //
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
