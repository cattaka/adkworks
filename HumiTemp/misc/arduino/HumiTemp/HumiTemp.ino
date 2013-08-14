#include <Usb.h>
#include <AndroidAccessory.h>

#include "DHT.h"
#include "Geppa.h"

//===================================================
#define DHTPIN A0     // what pin we're connected to

// Uncomment whatever type you're using!
//#define DHTTYPE DHT11   // DHT 11 
//#define DHTTYPE DHT22   // DHT 22  (AM2302)
#define DHTTYPE DHT21   // DHT 21 (AM2301)

#define RING_BUF_SIZE 10

//===================================================
typedef struct {
  float humi;
  float temp;
} 
MyData;

DHT dht(DHTPIN, DHTTYPE,3);

//===================================================
unsigned long gLastTime;
int gDataIdx = 0;
MyData gData[RING_BUF_SIZE];

//===================================================
AndroidAccessory gAcc("Cattaka Lab",
"HumiTemp Sensor",
"Humidity and temperature sensors",
"1.0",
"https://play.google.com/store/apps/details?id=net.cattaka.android.humitemp",
"0000000012345678");

//===================================================
void handleRecvPacket(unsigned char packetType, unsigned char opCode, int dataLen, unsigned char* data);
Geppa gGeppa(handleRecvPacket);

//===================================================
void setup() {
  Serial.begin(9600); 
  Serial.println("DHTxx test!");

  dht.begin();

  float h;
  float t;
  do {
    gLastTime = millis();
    h = dht.readHumidity();
    t = dht.readTemperature();
  } 
  while(isnan(t) || isnan(h));
  for (int i=0;i<RING_BUF_SIZE;i++) {
    gData[i].humi = h;
    gData[i].temp = t;
  }

  gAcc.powerOn();
}

void loop() {
  if (gAcc.isConnected()) {
    unsigned long t = (1000 - (millis() - gLastTime));
    if (t<= 1000) {
      delay(t);
    }
  } else {
    while(!gAcc.isConnected()) {
      unsigned long t = (1000 - (millis() - gLastTime));
      if (t>= 1000) {
        break;
      }
    }
  }
  {  // Receive packet
    int len;
    unsigned char msg[0x10];
    // Serial.print("Resv:");
    while((len = gAcc.read(msg, sizeof(msg), 1))>0) {
      for (int i=0;i<len;i++) {
        gGeppa.feedData(msg[i]);
        // Serial.print(msg[i], DEC); 
        // Serial.print(' ');
      }
    }
    // Serial.println();
  }
  

  // Reading temperature or humidity takes about 250 milliseconds!
  // Sensor readings may also be up to 2 seconds 'old' (its a very slow sensor)
  gLastTime = millis();
  float h = dht.readHumidity();
  float t = dht.readTemperature();

  // check if returns are valid, if they are NaN (not a number) then something went wrong!
  if (isnan(t) || isnan(h)) {
    Serial.println("Failed to read from DHT");
  } 
  else {
    gData[gDataIdx].humi = h;
    gData[gDataIdx].temp = t;
    gDataIdx = (gDataIdx + 1) % RING_BUF_SIZE;
    Serial.print("Humidity: "); 
    Serial.print(h);
    Serial.print(" %\t");
    Serial.print("Temperature: "); 
    Serial.print(t);
    Serial.println(" *C");
  }
}

void handleRecvPacket(unsigned char packetType, unsigned char opCode, int dataLen, unsigned char* data) {
//  Serial.print("packetType:");
//  Serial.println(packetType, DEC);
//  Serial.print("opCode:");
//  Serial.println(opCode, DEC);
  
  if (packetType == 0x01) {
    if (opCode == 0x01) {
      float h = 0;
      float t = 0;
      for (int i=0;i<RING_BUF_SIZE;i++) {
        h += gData[i].humi;
        t += gData[i].temp;
      }
      h /= RING_BUF_SIZE;
      t /= RING_BUF_SIZE;
      
      uint32_t ph = round(h*100);
      uint32_t pt = round(t*100);

      int len = sizeof(h) + sizeof(t);  // 8
      unsigned char cs = packetType + 0x02 + (0xFF & len) + (0xFF & (len<<8));
      unsigned char buf[] = {
        0x02, // STX
        0x01, // packetType
        0x02, // opCode
        (unsigned char)(0xFF & len), // DataLen1
        (unsigned char)(0xFF & (len<<8)), // DataLen1
        (unsigned char)cs, // Checksum
        (unsigned char)(ph>>24),
        (unsigned char)(ph>>16),
        (unsigned char)(ph>>8),
        (unsigned char)(ph),
        (unsigned char)(pt>>24),
        (unsigned char)(pt>>16),
        (unsigned char)(pt>>8),
        (unsigned char)(pt),
        0x03 // ETX
      };
      gAcc.write(buf, sizeof(buf));

      // Serial.println("Answered");
    }
  }
}
