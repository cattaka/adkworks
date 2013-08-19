#include <Servo.h>

#define BTN_SUP_PIN 8
#define BTN_SDOWN_PIN 9

#define SERVO_PIN 7
#define SERVO_MIN 550
#define SERVO_MAX 2350
#define SERVO_ANGLE_MIN 60
#define SERVO_ANGLE_MAX 120
#define MIN_SEND_INTERVAL 3d0
#define BUTTON_INTERVAL 30

int interval = 100;
unsigned long lastTime = 0;
unsigned long lastSendTime = 0;
unsigned long lastButtonTime = 0;

Servo gServo;
int servoAngle = 0;
int servoDir = false;
boolean btnSupPress = false;
boolean btnSdownPress = false;

void setup() {
  pinMode(BTN_SUP_PIN, INPUT);
  pinMode(BTN_SDOWN_PIN, OUTPUT);
  digitalWrite(BTN_SUP_PIN, HIGH);
  digitalWrite(BTN_SDOWN_PIN, HIGH);

  gServo.attach(SERVO_PIN, SERVO_MIN, SERVO_MAX);
  servoAngle = SERVO_ANGLE_MIN;
  servoDir = true;
  lastTime = millis();
  lastSendTime = lastTime;
  lastButtonTime = lastTime;
}

void loop() {
  unsigned long ct = millis();
  boolean updated = false;
  if (ct - lastTime > interval) {
    updated = true;
    lastTime = ct;
    if (servoDir) {
      servoAngle++;
      if (servoAngle >= SERVO_ANGLE_MAX) {
        servoDir = false;
      }
    } 
    else {
      servoAngle--;
      if (servoAngle <= SERVO_ANGLE_MIN) {
        servoDir = true;
      }
    }
    if (ct - lastSendTime > MIN_SEND_INTERVAL) {
      lastSendTime = ct;
      gServo.write(servoAngle);
    }
  }

  if (ct - lastButtonTime > BUTTON_INTERVAL) {
    lastButtonTime = ct;
    if (digitalRead(BTN_SUP_PIN) == HIGH) {
      btnSupPress = false;
    } 
    else {
      if (!btnSupPress) {
        interval -= 5;
      }
      btnSupPress = true;
    }
    if (digitalRead(BTN_SDOWN_PIN) == HIGH) {
      btnSdownPress = false;
    } 
    else {
      if (!btnSdownPress) {
        interval += 5;
      }
      btnSdownPress = true;
    }
    if (interval <=0) {
      interval = 5;
    } 
    else if (interval >= 1000) {
      interval = 1000;
    }
  }
}


