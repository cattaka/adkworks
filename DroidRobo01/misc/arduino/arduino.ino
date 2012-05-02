#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>
#include <AndroidAccessoryStream.h>
#include <MemoryMapLib.h>
#include <Servo.h>

//#define LED_PIN 0
#define PIN_EYE_LIGHT 0
#define PIN_MOTOR_L1 1
#define PIN_MOTOR_L2 2
#define PIN_MOTOR_R1 3
#define PIN_MOTOR_R2 4

#define TARGET_PIN   5
#define SERVO_NUM    2
#define POWER_DELAY 10

MemoryMap mMemoryMap;
AndroidAccessoryStream mAndroidAccessoryStream;
AndroidAccessory mAndroidAccessory("CtkLabs",
"DroidRobo01",
"DroidRobo01",
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
//  pinMode (LED_PIN, OUTPUT);
  pinMode (PIN_EYE_LIGHT, OUTPUT);
  pinMode (PIN_MOTOR_L1, OUTPUT);
  pinMode (PIN_MOTOR_L2, OUTPUT);
  pinMode (PIN_MOTOR_R1, OUTPUT);
  pinMode (PIN_MOTOR_R2, OUTPUT);
  digitalWrite(PIN_EYE_LIGHT, HIGH);
  digitalWrite(PIN_MOTOR_L1, LOW);
  digitalWrite(PIN_MOTOR_L2, LOW);
  digitalWrite(PIN_MOTOR_R1, LOW);
  digitalWrite(PIN_MOTOR_R2, LOW);
  
  initMyServo(0, 750, 2250, 0x7FFF, 0xffff);  // ZS
  initMyServo(1, 750, 2250, 0x7FFF, 0xffff);  // ZS

//  digitalWrite (LED_PIN, HIGH);
  mMemoryMap.setStreamInterface(&mAndroidAccessoryStream);
//  digitalWrite (LED_PIN, LOW);

  // register LED Command to Address 0x1b
  for (int i=0;i<SERVO_NUM;i++) {
    mMemoryMap.registerMapAddressJob(TARGET_PIN+i*2,OPERATION_WRITE,&jobReceive);
    mMemoryMap.registerMapAddressJob(TARGET_PIN+i*2+1,OPERATION_WRITE,&jobReceive);
  }
  mMemoryMap.registerMapAddressJob(PIN_EYE_LIGHT,OPERATION_WRITE,&jobOutput);
  mMemoryMap.registerMapAddressJob(PIN_MOTOR_L1,OPERATION_WRITE,&jobOutput);
  mMemoryMap.registerMapAddressJob(PIN_MOTOR_L2,OPERATION_WRITE,&jobOutput);
  mMemoryMap.registerMapAddressJob(PIN_MOTOR_R1,OPERATION_WRITE,&jobOutput);
  mMemoryMap.registerMapAddressJob(PIN_MOTOR_R2,OPERATION_WRITE,&jobOutput);

  delay(1000);
  mAndroidAccessory.powerOn();

  digitalWrite(PIN_EYE_LIGHT, LOW);
}

void loop() {
  if (mAndroidAccessory.isConnected()) {
//  digitalWrite (LED_PIN, HIGH);
    mMemoryMap.poll();
  } else {
//  digitalWrite (LED_PIN, LOW);
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

void jobOutput(unsigned char RWOP,unsigned char addr,unsigned char* value) {
  if (*value) {
    digitalWrite(addr, HIGH);
  } else {
    digitalWrite(addr, LOW);
  }
}
void jobReceive(unsigned char RWOP,unsigned char addr,unsigned char* value) {
  if(RWOP & OPERATION_WRITE) {
    int i = (addr - TARGET_PIN) / 2;
    if ((addr - 0x01) % 2 == 0) {
      myServos[i].value1 = *value;
    } 
    else {
      myServos[i].value2 = *value;
    }
  }
}

