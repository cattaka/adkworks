#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>
#include <AndroidAccessoryStream.h>
#include <MemoryMapLib.h>
#include <Servo.h>

#define LED_PIN 0

#define TARGET_PIN   2
#define SERVO_NUM    6
#define POWER_DELAY 10

MemoryMap mMemoryMap;
AndroidAccessoryStream mAndroidAccessoryStream;
AndroidAccessory mAndroidAccessory("CtkLabs",
"RobotArm01",
"RobotArm01",
"1.0",
"http://www.cattaka.net/",
"0000000012345678");

struct MyServo {
  Servo servo;
  int pulseMin;
  int pulseMax;
  long currentValue;
  long value1;
  long value2;
  long stepWidth;
};
struct MyServo myServos[SERVO_NUM];

void initMyServo(int idx, int pulseMin, int pulseMax, long initValue, long stepWidth) {
  myServos[idx].pulseMin = pulseMin;
  myServos[idx].pulseMax = pulseMax;
  myServos[idx].currentValue = initValue;
  myServos[idx].value1 = (initValue >> 8) & 0xFF;
  myServos[idx].value2 = initValue & 0xFF;
  myServos[idx].stepWidth = stepWidth;
  myServos[idx].servo.attach(TARGET_PIN + idx);
}

void setup() {
  initMyServo(0, 750, 2250, 0x7FFF, 0xff);  // ZS
  initMyServo(1, 700, 2300, 0x7FFF, 0xff);  // XQ
  initMyServo(2, 853, 2187, 0x7FFF, 0xfff);  // FUTABA
  initMyServo(3, 853, 2187, 0x7FFF, 0xff);  // FUTABA
  initMyServo(4, 750, 2250, 0x7FFF, 0xfff);  // ZS
  initMyServo(5, 500, 1700, 0x7FFF, 0xfff);  // emax(limited of max 2000)

  analogWrite(LED_PIN, 127);
  mMemoryMap.setStreamInterface(&mAndroidAccessoryStream);
  analogWrite(LED_PIN, 0);

  // register LED Command to Address 0x1b
  for (int i=0;i<SERVO_NUM;i++) {
    mMemoryMap.registerMapAddressJob(0x01+i*2,OPERATION_WRITE,&jobReceive);
    mMemoryMap.registerMapAddressJob(0x01+i*2+1,OPERATION_WRITE,&jobReceive);
  }
  analogWrite(LED_PIN,255);
  delay(1000);
}

void loop() {
  if (mAndroidAccessory.isConnected()) {
    analogWrite(LED_PIN,255);
    mMemoryMap.poll();
  } else {
    analogWrite(LED_PIN,0);
    mAndroidAccessoryStream.setInterface(&mAndroidAccessory);
  }

  for (int i=0;i<SERVO_NUM;i++) {
    long targetValue = (myServos[i].value1 << 8) + myServos[i].value2;
    if (myServos[i].currentValue + myServos[i].stepWidth < targetValue) {
      myServos[i].currentValue += myServos[i].stepWidth;
    } 
    else if (myServos[i].currentValue - myServos[i].stepWidth > targetValue) {
      myServos[i].currentValue -= myServos[i].stepWidth;
    } 
    else {
      myServos[i].currentValue = targetValue;
    }

    int val = map(myServos[i].currentValue, 0, 0xFFFF, myServos[i].pulseMin, myServos[i].pulseMax);
    myServos[i].servo.writeMicroseconds(val);
//    if (i==2) {
//      analogWrite(11, (myServos[i].currentValue >> 8) & 0xFF);
//      analogWrite(12, targetValue & 0xFF);
//    }
  }
  delay(POWER_DELAY);
}

void jobReceive(unsigned char RWOP,unsigned char addr,unsigned char* value) {
  if(RWOP & OPERATION_WRITE) {
    int i = (addr - 0x01) / 2;
    if ((addr - 0x01) % 2 == 0) {
      myServos[i].value1 = *value;
    } 
    else {
      myServos[i].value2 = *value;
    }
  }
}


