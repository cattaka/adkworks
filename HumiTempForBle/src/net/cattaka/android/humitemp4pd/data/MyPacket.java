
package net.cattaka.android.humitemp4pd.data;

import net.cattaka.libgeppa.data.IPacket;

public class MyPacket implements IPacket {
    private static final long serialVersionUID = 1L;

    private OpCode opCode;

    private int dataLen;

    private byte[] data;

    public MyPacket(OpCode opCode, int dataLen, byte[] data) {
        super();
        this.opCode = opCode;
        this.dataLen = dataLen;
        this.data = data;
    }

    public OpCode getOpCode() {
        return opCode;
    }

    public void setOpCode(OpCode opCode) {
        this.opCode = opCode;
    }

    public int getDataLen() {
        return dataLen;
    }

    public void setDataLen(int dataLen) {
        this.dataLen = dataLen;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
