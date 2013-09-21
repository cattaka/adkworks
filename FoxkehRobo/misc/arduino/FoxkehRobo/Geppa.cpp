#include "Geppa.h"

Geppa::Geppa(void (*func)(unsigned char, unsigned char, int, unsigned char*)) {
  state = UNKNOWN;
  handleRecvPacket = func;
}

bool Geppa::feedData(unsigned char c) {
  bool result = false;
    switch(state) {
      case UNKNOWN: {
        if (c == 0x02) {
          state = PACKET_TYPE;
        }
        break;
      }
      case PACKET_TYPE: {
        packetType = c;
        state = OPC;
        break;
      }
      case OPC: {
        opCode = c;
        state = LEN1;
        break;
      }
      case LEN1: {
        len = c;
        state = LEN2;
        break;
      }
      case LEN2: {
        len |= ((int)c) << 8;
        state = CHECKSUM;
        break;
      }
      case CHECKSUM: {
        checksum = c;
        unsigned char t = packetType + opCode + (0xFF & len) + (0xFF & (len<<8));
        if (c == t) {
          if (len > 0) {
            dataLen = 0;
            state = DATA;
          } else {
            state = ETX;
          }
        } else {
          state = UNKNOWN;
        }
        break;
      }
      case DATA: {
        if (dataLen <= MAX_DATA_LEN) {
          data[dataLen] = c;
        }
        dataLen++;
        if (dataLen == len) {
          state = ETX;
        }
        break;
      }
      case ETX: {
        if (c == 0x03) {
          (*handleRecvPacket)(packetType, opCode, dataLen, data);
          result = true;
        }
        state = UNKNOWN;
        break;
      }
    }
    return result;
}

