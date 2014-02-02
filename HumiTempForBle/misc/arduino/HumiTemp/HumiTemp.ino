#include <SPI.h>
#include <boards.h>
#include <ble_shield.h>
#include <services.h> 

#include "DHT.h"
#include "Geppa.h"

//#define DEBUG

//===================================================
#define DHTPIN A0     // what pin we're connected to

// Uncomment whatever type you're using!
#define DHTTYPE DHT11   // DHT 11 
//#define DHTTYPE DHT22   // DHT 22  (AM2302)
//#define DHTTYPE DHT21   // DHT 21 (AM2301)

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
void handleRecvPacket(unsigned char packetType, unsigned char opCode, int dataLen, unsigned char* data);
Geppa gGeppa(handleRecvPacket);
void writeSenserValue();

//===================================================
void setup() {
  Serial.begin(57600); 
  Serial.println("Debug begin.");

  ble_begin();

  dht.begin();

  float h;
  float t;
  do {
    Serial.println("test.");
    gLastTime = millis();
    h = dht.readHumidity();
    t = dht.readTemperature();
  } 
  while(isnan(t) || isnan(h) || !(0 <= t && t<=100 && 0 <= h && h<=100));
  for (int i=0;i<RING_BUF_SIZE;i++) {
    gData[i].humi = h;
    gData[i].temp = t;
  }
  Serial.print("Initial Humidity: "); 
  Serial.print(h);
  Serial.print(" %\t");
  Serial.print("Initial Temperature: "); 
  Serial.print(t);
  Serial.println(" *C");
}

void loop() {
  {  // Receive packet
    int len;
    // Serial.print("Resv:");
    while(ble_available()>0) {
      gGeppa.feedData(ble_read());
    }
    // Serial.println();
  }


  // Reading temperature or humidity takes about 250 milliseconds!
  // Sensor readings may also be up togLastTime 2 seconds 'old' (its a very slow sensor)
  float h = dht.readHumidity();
  float t = dht.readTemperature();

  // check if returns are valid, if they are NaN (not a number) then something went wrong!
  if (!isnan(t) && !isnan(h)) {
    if (0 <= t && t<=100 && 0 <= h && h<=100) {
      gData[gDataIdx].humi = h;
      gData[gDataIdx].temp = t;
      gDataIdx = (gDataIdx + 1) % RING_BUF_SIZE;
    }
  }

  if (isnan(t) || isnan(h)) {
    Serial.println("Failed to read from DHT");
  } 
  
  unsigned long currtime = millis();
  if (true || currtime - gLastTime > 1000) {
    gLastTime = currtime;
    if (ble_connected()) {
        writeSenserValue();
    }
    Serial.print("Humidity: "); 
    Serial.print(h);
    Serial.print(" %\t, ");
    Serial.print("Temperature: "); 
    Serial.print(t);
    Serial.print(" *C, ");
    Serial.print("Connected: "); 
    Serial.println(ble_connected());
  }

  ble_do_events();
}

void handleRecvPacket(unsigned char packetType, unsigned char opCode, int dataLen, unsigned char* data) {
  Serial.print("packetType:");
  Serial.println(packetType, DEC);
  Serial.print("opCode:");
  Serial.println(opCode, DEC);

  if (packetType == 0x01) {
    if (opCode == 0x01) {
      writeSenserValue();
    }
  }
}

void writeSenserValue() {
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
  Serial.print("ph: "); 
  Serial.print(ph);
  Serial.print("\t");
  Serial.print("pt: "); 
  Serial.println(pt);

  int len = sizeof(h) + sizeof(t);  // 8
  unsigned char cs = 0x01 + 0x02 + (0xFF & len) + (0xFF & (len<<8));
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
  for (int i = 0; i < sizeof(buf); i++) {
    ble_write(buf[i]);
  }

  // Serial.println("Answered");
}


