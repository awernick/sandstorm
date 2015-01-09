#include <TinyGPS++.h>
#include <SoftwareSerial.h>
/*
   This sample sketch demonstrates the normal use of a TinyGPS++ (TinyGPSPlus) object.
   It requires the use of SoftwareSerial, and assumes that you have a
   4800-baud serial GPS device hooked up on pins 4(rx) and 3(tx).
*/
static const int cameraPin = 7;

static const int RXPin = 4, TXPin = 3;
static const uint32_t GPSBaud = 9600;

static const uint32_t RAW_NMAE_INTERVAL = 10000;
static const uint32_t HEARTBEAT_INTERVAL = 5000;
static const uint32_t SENSOR_INFO_INTERVAL = 10000;

static const uint32_t RAW_NMAE_STATUS = 0;
static const uint32_t HEARTBEAT_STATUS = 1;
static const uint32_t SENSOR_INFO_STATUS = 2;

TinyGPSPlus gps;
SoftwareSerial softSerial(RXPin, TXPin);

static unsigned long  time = 0;
static int status = RAW_NMAE_STATUS;
static int interval = RAW_NMAE_INTERVAL; 

void setup()
{
  pinMode(cameraPin, OUTPUT);
  Serial.begin(115200);
  softSerial.begin(GPSBaud);
  
  Serial.println(F("SandStorm1 initializing ..."));
  Serial.println();
}

void loop()
{
  
  time = millis();
  
  while( (millis() - time)  <  interval )
  {
    switch( status )
    {
      case RAW_NMAE_STATUS:
        sendNMAEGPSSentence();
        break;
      case HEARTBEAT_STATUS:
        transmitHeartBeatSignal();
        break;
      case SENSOR_INFO_STATUS:
        displaySensorInfo();
        break;
    }
  }
  Serial.println();
  Serial.print("Running Time: ");
  Serial.println(time);
  
  status++;
  
  if(status > SENSOR_INFO_STATUS)
    status = RAW_NMAE_STATUS;
    
   Serial.print("Status: ");
   Serial.println(status);
    
   switch(status)
   {
     case RAW_NMAE_STATUS:
       interval = RAW_NMAE_INTERVAL;
       break;
     case HEARTBEAT_STATUS:
       interval = HEARTBEAT_INTERVAL;
       break;
     case SENSOR_INFO_STATUS:
       interval = SENSOR_INFO_INTERVAL;
       break;
   }
}

void sendNMAEGPSSentence()
{
  if(softSerial.available())
    Serial.write(softSerial.read());
  if(Serial.available())
    softSerial.write(Serial.read());
}

void transmitHeartBeatSignal()
{
  char chars[20];
  char currentChar;
  static int i = 0;
  
  Serial.println("HeartBeat :");
  
  if(Serial.available())
  {
      currentChar = Serial.read();
      chars[i] = currentChar;
      i++;
      chars[i] = '\0';
  }
  
  String string(chars);
  
  if(string.equals("tick"))
  {
    Serial.println("Recieved tick");
    if(Serial.available())
    {
      Serial.println("tock");
    }
    i = 0;
    status++;
  }
}

void displaySensorInfo()
{
  while(!(millis() - time))
  {
    Serial.println("Display Sensor Info");
    Serial.print("Snapshot...");
    digitalWrite(cameraPin, HIGH);
    digitalWrite(cameraPin, LOW);
  }
}
