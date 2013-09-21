
package net.cattaka.android.foxkehrobo.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.cattaka.libgeppa.data.IPacketFactory;

public class FrPacketFactory implements IPacketFactory<FrPacket> {
    public static final byte STX = 0x02;

    public static final byte ETX = 0x03;

    public static final byte PACKET_TYPE_DATA = 0x01;

    public static final int MAX_DATA_LEN = 1 << 20;

    private static byte[] ZERO_BYTES = new byte[0];

    public enum ConState {
        UNKNOWN, STX, PACKET_TYPE, OPC, LEN1, LEN2, CHECKSUM, DATA, ETX
    };

    @Override
    public FrPacket readPacket(InputStream in) throws IOException {
        // データ送信
        ConState state = ConState.UNKNOWN;
        byte packetType = 0;
        byte opCode = 0;
        int len = 0;
        int dataLen = 0;
        byte[] data = null;
        int r;
        outer: while ((r = in.read()) != -1) {
            switch (state) {
                case UNKNOWN: {
                    if (r == 0x02) {
                        state = ConState.PACKET_TYPE;
                    }
                    break;
                }
                case PACKET_TYPE: {
                    packetType = (byte)r;
                    state = ConState.OPC;
                    break;
                }
                case OPC: {
                    opCode = (byte)r;
                    state = ConState.LEN1;
                    break;
                }
                case LEN1: {
                    len = r;
                    state = ConState.LEN2;
                    break;
                }
                case LEN2: {
                    len |= r << 8;
                    state = ConState.CHECKSUM;
                    break;
                }
                case CHECKSUM: {
                    int t = 0xFF & (packetType + opCode + (0xFF & len) + (0xFF & (len >> 8)));
                    if (r == t) {
                        if (len > 0) {
                            dataLen = 0;
                            data = new byte[len];
                            state = ConState.DATA;
                        } else {
                            data = ZERO_BYTES;
                            state = ConState.ETX;
                        }
                    } else {
                        state = ConState.UNKNOWN;
                    }
                    break;
                }
                case DATA: {
                    if (dataLen < MAX_DATA_LEN) {
                        data[dataLen] = (byte)r;
                    }
                    dataLen++;
                    if (dataLen == len) {
                        state = ConState.ETX;
                    }
                    break;
                }
                case ETX: {
                    if (r == 0x03) {
                        break outer;
                    }
                    state = ConState.UNKNOWN;
                    break;
                }
            }
        }
        if (r == -1) {
            throw new IOException();
        }

        return new FrPacket(OpCode.fromValue(opCode), len, data);
    }

    @Override
    public void writePacket(OutputStream out, FrPacket packet) throws IOException {
        byte len1 = (byte)(0xFF & (packet.getDataLen()));
        byte len2 = (byte)(0xFF & (packet.getDataLen() >> 8));
        byte t = (byte)(0xFF & (PACKET_TYPE_DATA + packet.getOpCode().getValue() + len1 + len2));
        out.write(STX);
        out.write(PACKET_TYPE_DATA);
        out.write(packet.getOpCode().getValue());
        out.write(len1);
        out.write(len2);
        out.write(t);
        if (packet.getDataLen() > 0) {
            out.write(packet.getData(), 0, packet.getDataLen());
        }
        out.write(ETX);
    }
}
