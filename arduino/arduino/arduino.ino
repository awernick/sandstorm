#include <pt.h>   // include protothread library
#include <SoftwareSerial.h>

#define RXPin 13
#define TXPin 14
#define SENSOR_INFO_INTERVAL 3000
#define BAUDRATE 115200
#define GPS_BAUDRATE 9600

static struct pt gps, heartbeat, sensors; // each protothread needs one of these

SoftwareSerial gpsSerial(RXPin, TXPin);

void setup() {
  Serial.begin(BAUDRATE);
  Serial1.begin(BAUDRATE);
  
  gpsSerial.begin(GPS_BAUDRATE);
  
  PT_INIT(&gps);  // initialise the three // protothread variables
  PT_INIT(&heartbeat);
  PT_INIT(&sensors);  
}

PT_THREAD(verifyHeartbeat(struct pt *pt, int interval))
{
  static char tick[4];
  static int i = 0;
  static String message = "";
  char currentChar;
  
  static unsigned long timestamp = 0;
  
  PT_BEGIN(pt);
  
  Serial.println("Heart!");
  
  while(1)
  {
    
    PT_WAIT_UNTIL(pt, millis() - timestamp > interval);
    
    timestamp = millis();
      
    if(Serial.available() > 0)
    {
      currentChar = Serial.read();
      Serial.println(currentChar);
        
      if( i > 3 ) { i = 0; }
      
      Serial.println(i,DEC);
      tick[i] = currentChar;
      i++;
      tick[i] = '\0';
      message = tick;
    }
    
    if(message.equals("tick"))
    {
      Serial.println("Got tick");
      Serial.println("tock");
      message = "";
      i = 0;
    }
  }
  
  PT_END(pt);
}

/* exactly the same as the protothread1 function */
static int transmitGPS(struct pt *pt, int interval) {
  static unsigned long timestamp = 0;
  PT_BEGIN(pt);
  
  while(1) 
  {
    timestamp = millis();
    
    while(1) {
      PT_WAIT_UNTIL(pt, millis() - timestamp > interval);
      
      if (gpsSerial.available())
        Serial1.write(gpsSerial.read());
      if (Serial1.available())
        gpsSerial.write(Serial1.read());
        
      Serial1.println("GPS");
    }
  }
  
  PT_END(pt);
}

PT_THREAD(updateSensorInfo(struct pt *pt, int interval)) {
  static unsigned long timestamp = 0;
  struct pt heartbeat;
  
  PT_BEGIN(pt);
  Serial.println("UPDATE SENSOR");
  
  while(1) 
  {
    
    PT_WAIT_UNTIL(pt, millis() - timestamp > interval);
    
    Serial.println("Starting");
    
    timestamp = millis();
    
    Serial.println("UPDATE SENSOR");
    
    Serial.print("timestamp: ");
    Serial.println(timestamp);
  
  }
  
  PT_END(pt);
}

void loop() 
{
  //transmitGPS(&gps, 5000); // by calling them infinitely
  updateSensorInfo(&sensors, 3000);
  verifyHeartbeat(&heartbeat, 1000);
}
