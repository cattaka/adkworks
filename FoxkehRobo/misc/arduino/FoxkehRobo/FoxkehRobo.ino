#include <SoftwareSerial.h>
#include <Servo.h>
#include "Geppa.h"

// =============================
// Debug setting
#define DUMP_RAW
#define DUMP_PACKET

// =============================
// variables for RBT-001
#define RBT_RX 8
#define RBT_TX 7
SoftwareSerial gDebug(RBT_RX, RBT_TX);
// =============================

// =============================
#define SERVO_NUM 10
#define SERVO_ARM_LEFT   4
#define SERVO_ARM_RIGHT  5
#define SERVO_FOOT_LEFT  3
#define SERVO_FOOT_RIGHT 6
#define SERVO_HEAD_YAW   A3
#define SERVO_HEAD_PITCH A4
#define SERVO_TAIL_YAW   A6
#define SERVO_TAIL_PITCH A5
#define SERVO_EAR_LEFT   12
#define SERVO_EAR_RIGHT  11
//#define LED_POWER        9
//#define LED_EYE_LEFT     9
//#define LED_EYE_RIGHT    8

//#define PIN_ACCEL_X A1
//#define PIN_ACCEL_Y A2
//#define PIN_ACCEL_Z A3
//#define OFFSET_ACCEL_X 0
//#define OFFSET_ACCEL_Y 0
//#define OFFSET_ACCEL_Z 0

struct MyServo {
  Servo servo;
  int pulseMin;
  int pulseMax;
  long currentValue;
  long value;
  long stepWidth;
};
struct MyServo myServos[SERVO_NUM];

void initMyServo(int idx, int pin, int pulseMin, int pulseMax, long initValue, long stepWidth) {
  myServos[idx].pulseMin = pulseMin;
  myServos[idx].pulseMax = pulseMax;
  myServos[idx].currentValue = initValue;
  myServos[idx].value = initValue;
  myServos[idx].stepWidth = stepWidth;
  myServos[idx].servo.attach(pin, pulseMin, pulseMax);
}
// =============================

void handleRecvPacket(unsigned char packetType, unsigned char opCode, int dataLen, unsigned char* data);
Geppa g_geppa(handleRecvPacket);

void setup()
{
  // ボーレートを指定して通信開始
  gDebug.begin(9600);
  Serial.begin(9600);
  gDebug.println("Serial connected");
//  pinMode(LED_EYE_LEFT, OUTPUT);
//  pinMode(LED_EYE_RIGHT, OUTPUT);

  {
    initMyServo(0, SERVO_ARM_LEFT,   500, 2400, 0x7F, 0xFF);
    initMyServo(1, SERVO_ARM_RIGHT,  500, 2400, 0x7F, 0xFF);
    initMyServo(2, SERVO_FOOT_LEFT,  900, 2000, 0x7F, 0xFF);
    initMyServo(3, SERVO_FOOT_RIGHT, 900, 2000, 0x7F, 0xFF);
    initMyServo(4, SERVO_HEAD_YAW,   500, 2400, 0x7F, 0xFF);
    initMyServo(5, SERVO_HEAD_PITCH, 500, 2400, 0x7F, 0xFF);
    initMyServo(6, SERVO_TAIL_YAW,   500, 2400, 0x7F, 0xFF);
    initMyServo(7, SERVO_TAIL_PITCH, 500, 2400, 0x7F, 0xFF);
    initMyServo(8, SERVO_EAR_LEFT,   800, 2200, 0x7F, 0xFF);
    initMyServo(9, SERVO_EAR_RIGHT,  800, 2200, 0x7F, 0xFF);
  }
//  {
//    pinMode(PIN_ACCEL_X, INPUT);
//    pinMode(PIN_ACCEL_Y, INPUT);
//    pinMode(PIN_ACCEL_Z, INPUT);
//  }
}

void loop()
{
  // データ受信
  while(Serial.available() > 0) {
    unsigned char c = Serial.read();
#ifdef DUMP_RAW
    gDebug.print(' ');
    gDebug.print(c, HEX);
#endif
    g_geppa.feedData(c);
  }
  {  // Sending the data received from Serial to Bluetooth for debug.
    if (gDebug.available() > 0) {
      while (gDebug.available() > 0) {
        unsigned char c = gDebug.read();
#ifdef DUMP_RAW
        gDebug.print(' ');
        gDebug.print(c, HEX);
#endif
        Serial.write(c);
      }
#ifdef DUMP_RAW
      gDebug.print("\nstate=");
      gDebug.print(g_geppa.state, DEC);
      gDebug.print("\nlen=");
      gDebug.print(g_geppa.len, DEC);
      gDebug.print("\n");
#endif
    }
  }

  {  // Controlling servo angles
    for (int i=0;i<SERVO_NUM;i++) {
      long targetValue = myServos[i].value;
      if (myServos[i].currentValue + myServos[i].stepWidth < targetValue) {
        myServos[i].currentValue += myServos[i].stepWidth;
      }
      else if (myServos[i].currentValue - myServos[i].stepWidth > targetValue) {
        myServos[i].currentValue -= myServos[i].stepWidth;
      }
      else {
        myServos[i].currentValue = targetValue;
      }
      int val = map(myServos[i].currentValue, 0, 0xFF, 0, 180);
      myServos[i].servo.write(val);
    }
  }
  delay(10);
}

void handleRecvPacket(unsigned char packetType, unsigned char opCode, int dataLen, unsigned char* data) {
#ifdef DUMP_PACKETa
  gDebug.print('(');
  gDebug.print(packetType, HEX);
  gDebug.print(',');
  gDebug.print(opCode, HEX);
  gDebug.print(',');
  gDebug.print(dataLen, HEX);
  gDebug.print(',');
  for (int i=0;i<dataLen;i++) {
    if (data[i] < 0x10) {
      gDebug.print('0');
    }
    gDebug.print(data[i], HEX);
  }
  gDebug.print(')');
  gDebug.print('\n');
#endif
  if (packetType == 0x01) {
    if (opCode == 0) {
      // ECHO
      int len = dataLen;
      unsigned char t = packetType + opCode + (0xFF & len) + (0xFF & (len<<8));
      Serial.write(0x02);
      Serial.write(packetType);
      Serial.write(opCode);
      Serial.write((uint8_t)(0xFF & len));
      Serial.write((uint8_t)(0xFF & (len<<8)));
      Serial.write((uint8_t)t);
      for (int i=0;i<dataLen;i++) {
        Serial.write(data[i]);
      }
      Serial.write(0x03);
    } else if (opCode == 1) {
      // SERVO_ANGLE
      if (dataLen != SERVO_NUM) {
        gDebug.print("dataLen is not SERVO_NUM.\n");
      } else {
        for (int i=0;i<SERVO_NUM;i++) {
          myServos[i].value = data[i];
          gDebug.print("Servo idx=");
          gDebug.print(i, DEC);
          gDebug.print(", value=");
          gDebug.print(myServos[i].value, HEX);
          gDebug.print("\n");
        }
      }
//    } else if (opCode == 2) {
//      // EYE_LED
//      unsigned char val = data[0];
//      digitalWrite(LED_EYE_LEFT, (val & 1) ? HIGH:LOW);
//      digitalWrite(LED_EYE_RIGHT, (val & 2) ? HIGH:LOW);
    } else if (opCode == 3) {
      // POSE
      if (dataLen != SERVO_NUM + 3) {
        gDebug.print("dataLen is not (SERVO_NUM + 2 + 1).\n");
      } else {
        int flags = (int)data[0] | (((int)data[1]) << 8);
        int led = data[SERVO_NUM+2];
        for (int i=0;i<SERVO_NUM;i++) {
          if (flags & (1<<i)) {
            myServos[i].value = data[i+2];
            gDebug.print("Servo idx=");
            gDebug.print(i, DEC);
            gDebug.print(", value=");
            gDebug.print(myServos[i].value, HEX);
            gDebug.print("\n");
          }
        }
//        if (flags & (1<<SERVO_NUM)) {
//          digitalWrite(LED_EYE_LEFT, (led & 1) ? HIGH:LOW);
//          digitalWrite(LED_EYE_RIGHT, (led & 2) ? HIGH:LOW);
//        }
      }
    }
/*
    else if (opCode == 4) {
      gDebug.print("OK\n");
      // REQ_ACCEL
      int x = (analogRead(PIN_ACCEL_X) - OFFSET_ACCEL_X);
      int y = (analogRead(PIN_ACCEL_Y) - OFFSET_ACCEL_Y);
      int z = (analogRead(PIN_ACCEL_Z) - OFFSET_ACCEL_Z);
      byte data[] = {
        0x02,
        0x01,
        0x05,
        0x06,
        0x00,
        0x0C,
        (0xFF & (x >> 0)),
        (0xFF & (x >> 8)),
        (0xFF & (y >> 0)),
        (0xFF & (y >> 8)),
        (0xFF & (z >> 0)),
        (0xFF & (z >> 8)),
        0x03
      };
      int n = sizeof(data)/sizeof(byte);
      for (int i=0;i<n;i++) {
        Serial.write(data[i]);
      }
      //Serial.print("(");
      //Serial.print(x, DEC);
      //Serial.print(",");
      //Serial.print(y, DEC);
      //Serial.print(",");
      //Serial.print(z, DEC);
      //Serial.print(")\n");
    }
*/
//  } else if (packetType == 0x69) {
//    // The message from RBT-001
//    if (opCode == 0x11) {
//      gDebug.print("Transparent mode is started:");
//      gDebug.print(data[0], HEX);
//      gDebug.print(":");
//      gDebug.print(data[1], HEX);
//      gDebug.print("\n");
//    }
//    else if (opCode == 0x0C) {
//      digitalWrite(LED_EYE_LEFT, HIGH);
//      digitalWrite(LED_EYE_RIGHT, HIGH);
//      gDebug.print("Bluetooth connection is established.");
//      gDebug.print("\n");
//    }
//    else if (opCode == 0x0E) {
//      digitalWrite(LED_EYE_LEFT, LOW);
//      digitalWrite(LED_EYE_RIGHT, LOW);
//      gDebug.print("SPP Link released.");
//      gDebug.print("\n");
//    }
  }
}


