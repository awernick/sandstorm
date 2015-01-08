#include <TinyGPS++.h>
#include <SoftwareSerial.h>
/*
   This sample sketch demonstrates the normal use of a TinyGPS++ (TinyGPSPlus) object.
   It requires the use of SoftwareSerial, and assumes that you have a
   4800-baud serial GPS device hooked up on pins 4(rx) and 3(tx).
*/
static const int RXPin = 4, TXPin = 3;
static const uint32_t GPSBaud = 9600;

static const uint32_t RAW_NMAE_INTERVAL = 10000;
static const uint32_t HEARTBEAT_INTERVAL = 5000;
static const uint32_t SENSOR_INFO_INTERVAL = 10000;

TinyGPSPlus gps;
SoftwareSerial softSerial(RXPin, TXPin);

static int time = 0;

void setup()
{
  Serial.begin(115200);
  softSerial.begin(GPSBaud);
  
  Serial.println(F("SandStorm1 initializing ..."));
  Serial.println();
}

void loop()
{
  time = millis();
  
  while( (millis() - time)  < RAW_NMAE_INTERVAL)
  {
    if(softSerial.available())
      Serial.write(softSerial.read());
    if(Serial.available())
      softSerial.write(Serial.read());
  }
  
  time = millis();
  
  while( (millis() - time) < HEARTBEAT_INTERVAL)
  {
    Serial.println("Hello");
  }
  
  time = millis();
  
  while( (millis() - time) < SENSOR_INFO_INTERVAL)
  {
    Serial.println("Heartbeat");
  } 
}
