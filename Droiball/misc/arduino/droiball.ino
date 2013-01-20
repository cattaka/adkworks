#include <SoftwareSerial.h>
#include <SoftwareServo.h>
#include "Geppa.h"

// =============================
// Debug setting
#define DUMP_RAW
#define DUMP_PACKET

// =============================
// variables for RBT-001
#define RBT_RX A4
#define RBT_TX 11
#define BT_CTS          10
SoftwareSerial g_bluetooth(RBT_RX, RBT_TX);
// =============================

// =============================
#define SERVO_NUM 7
#define SERVO_ARM_LEFT   5
#define SERVO_ARM_RIGHT  4
#define SERVO_FOOT_LEFT  3
#define SERVO_FOOT_RIGHT 2
#define SERVO_HEAD      12
#define SERVO_EAR_LEFT   7
#define SERVO_EAR_RIGHT  6
#define LED_EYE_LEFT     9
#define LED_EYE_RIGHT    8

#define PIN_ACCEL_X A1
#define PIN_ACCEL_Y A2
#define PIN_ACCEL_Z A3
#define OFFSET_ACCEL_X 0
#define OFFSET_ACCEL_Y 0
#define OFFSET_ACCEL_Z 0

struct MyServo {
  SoftwareServo servo;
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
  myServos[idx].servo.attach(pin);
  myServos[idx].servo.setMinimumPulse(pulseMin);
  myServos[idx].servo.setMaximumPulse(pulseMax);
}
// =============================

void handleRecvPacket(unsigned char packetType, unsigned char opCode, int dataLen, unsigned char* data);
Geppa g_geppa(handleRecvPacket);

void setup()
{
  // ボーレートを指定して通信開始
  g_bluetooth.begin(9600);
  Serial.begin(9600);
  Serial.println("Serial connected");
  pinMode(BT_CTS, OUTPUT);
  pinMode(LED_EYE_LEFT, OUTPUT);
  pinMode(LED_EYE_RIGHT, OUTPUT);

  { // Cause UART break and change to transparent mode
    digitalWrite(RBT_TX, LOW);
    delay(100);
    digitalWrite(RBT_TX, HIGH);
    delay(100);
    // End UART break mode
    byte data[] = {
        0x02,
        0x52,
        0x11,
        0x01,
        0x00,
        0x64,
        0x01,
        0x03
    };
    int n = sizeof(data)/sizeof(byte);
    for (int i=0;i<n;i++) {
      g_bluetooth.write(data[i]);
    }
  }
  {
    initMyServo(0, SERVO_ARM_LEFT,   500, 2400, 0x7F, 0xFF);
    initMyServo(1, SERVO_ARM_RIGHT,  500, 2400, 0x7F, 0xFF);
    initMyServo(2, SERVO_FOOT_LEFT,  900, 2000, 0x7F, 0xFF);
    initMyServo(3, SERVO_FOOT_RIGHT, 900, 2000, 0x7F, 0xFF);
    initMyServo(4, SERVO_HEAD,       500, 2400, 0x7F, 0xFF);
    initMyServo(5, SERVO_EAR_LEFT,   800, 2200, 0x7F, 0xFF);
    initMyServo(6, SERVO_EAR_RIGHT,  800, 2200, 0x7F, 0xFF);
  }
  {
    pinMode(PIN_ACCEL_X, INPUT);
    pinMode(PIN_ACCEL_Y, INPUT);
    pinMode(PIN_ACCEL_Z, INPUT);
  }
}

void loop()
{
  // データ受信
  digitalWrite(BT_CTS, LOW);
  while(g_bluetooth.available() > 0) {
    unsigned char c = g_bluetooth.read();
#ifdef DUMP_RAW
    Serial.print(' ');
    Serial.print(c, HEX);
#endif
    g_geppa.feedData(c);
  }
  digitalWrite(BT_CTS, HIGH);
  {  // Sending the data received from Serial to Bluetooth for debug.
    if (Serial.available() > 0) {
      while (Serial.available() > 0) {
        unsigned char c = Serial.read();
#ifdef DUMP_RAW
        Serial.print(' ');
        Serial.print(c, HEX);
#endif
        g_bluetooth.write(c);
      }
#ifdef DUMP_RAW
      Serial.print("\nstate=");
      Serial.print(g_geppa.state, DEC);
      Serial.print("\nlen=");
      Serial.print(g_geppa.len, DEC);
      Serial.print("\n");
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
  SoftwareServo::refresh();
  delay(10);
}

void handleRecvPacket(unsigned char packetType, unsigned char opCode, int dataLen, unsigned char* data) {
#ifdef DUMP_PACKETa
  Serial.print('(');
  Serial.print(packetType, HEX);
  Serial.print(',');
  Serial.print(opCode, HEX);
  Serial.print(',');
  Serial.print(dataLen, HEX);
  Serial.print(',');
  for (int i=0;i<dataLen;i++) {
    if (data[i] < 0x10) {
      Serial.print('0');
    }
    Serial.print(data[i], HEX);
  }
  Serial.print(')');
  Serial.print('\n');
#endif
  if (packetType == 0x01) {
    if (opCode == 0) {
      // ECHO
      int len = dataLen;
      unsigned char t = packetType + opCode + (0xFF & len) + (0xFF & (len<<8));
      g_bluetooth.write(0x02);
      g_bluetooth.write(packetType);
      g_bluetooth.write(opCode);
      g_bluetooth.write((uint8_t)(0xFF & len));
      g_bluetooth.write((uint8_t)(0xFF & (len<<8)));
      g_bluetooth.write((uint8_t)t);
      for (int i=0;i<dataLen;i++) {
        g_bluetooth.write(data[i]);
      }
      g_bluetooth.write(0x03);
    } else if (opCode == 1) {
      // SERVO_ANGLE
      if (dataLen != SERVO_NUM) {
        Serial.print("dataLen is not SERVO_NUM.\n");
      } else {
        for (int i=0;i<SERVO_NUM;i++) {
          myServos[i].value = data[i];
          Serial.print("Servo idx=");
          Serial.print(i, DEC);
          Serial.print(", value=");
          Serial.print(myServos[i].value, HEX);
          Serial.print("\n");
        }
      }
    } else if (opCode == 2) {
      // EYE_LED
      unsigned char val = data[0];
      digitalWrite(LED_EYE_LEFT, (val & 1) ? HIGH:LOW);
      digitalWrite(LED_EYE_RIGHT, (val & 2) ? HIGH:LOW);
    } else if (opCode == 3) {
      // POSE
      if (dataLen != SERVO_NUM + 3) {
        Serial.print("dataLen is not (SERVO_NUM + 2 + 1).\n");
      } else {
        int flags = (int)data[0] | (((int)data[1]) << 8);
        int led = data[SERVO_NUM+2];
        for (int i=0;i<SERVO_NUM;i++) {
          if (flags & (1<<i)) {
            myServos[i].value = data[i+2];
            Serial.print("Servo idx=");
            Serial.print(i, DEC);
            Serial.print(", value=");
            Serial.print(myServos[i].value, HEX);
            Serial.print("\n");
          }
        }
        if (flags & (1<<SERVO_NUM)) {
          digitalWrite(LED_EYE_LEFT, (led & 1) ? HIGH:LOW);
          digitalWrite(LED_EYE_RIGHT, (led & 2) ? HIGH:LOW);
        }
      }
    } else if (opCode == 4) {
      Serial.print("OK\n");
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
        g_bluetooth.write(data[i]);
      }
      //Serial.print("(");
      //Serial.print(x, DEC);
      //Serial.print(",");
      //Serial.print(y, DEC);
      //Serial.print(",");
      //Serial.print(z, DEC);
      //Serial.print(")\n");
    }
  } else if (packetType == 0x69) {
    // The message from RBT-001
    if (opCode == 0x11) {
      Serial.print("Transparent mode is started:");
      Serial.print(data[0], HEX);
      Serial.print(":");
      Serial.print(data[1], HEX);
      Serial.print("\n");
    }
    else if (opCode == 0x0C) {
      digitalWrite(LED_EYE_LEFT, HIGH);
      digitalWrite(LED_EYE_RIGHT, HIGH);
      Serial.print("Bluetooth connection is established.");
      Serial.print("\n");
    }
    else if (opCode == 0x0E) {
      digitalWrite(LED_EYE_LEFT, LOW);
      digitalWrite(LED_EYE_RIGHT, LOW);
      Serial.print("SPP Link released.");
      Serial.print("\n");
    } else if (opCode == 0x10) {
            // End UART break mode
      Serial.print("End UART break mode.\n");
      byte data[] = {
        0x02,
        0x52,
        0x11,
        0x01,
        0x00,
        0x64,
        0x01,
        0x03
      };
      int n = sizeof(data)/sizeof(byte);
      for (int i=0;i<n;i++) {
        g_bluetooth.write(data[i]);
      }
    }
  }
}


