#define MAX_DATA_LEN 0x100

enum ConState {
  UNKNOWN,
  STX,
  PACKET_TYPE,
  OPC,
  LEN1,
  LEN2,
  CHECKSUM,
  DATA,
  ETX
};

class Geppa {
private:
//  ConState state;
  unsigned char packetType;
  unsigned char opCode;
//  int len;
  unsigned char checksum;
  int dataLen;
  unsigned char data[MAX_DATA_LEN];
  void (*handleRecvPacket)(unsigned char, unsigned char, int, unsigned char*);
public:
  ConState state;
  int len;
  Geppa(void (*func)(unsigned char, unsigned char, int, unsigned char*));
  bool feedData(unsigned char c);
};

