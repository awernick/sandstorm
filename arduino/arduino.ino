#include <pt.h>   // include protothread library
#include <SoftwareSerial.h>

#define RXPin 52
#define TXPin 53
#define SENSOR_INFO_INTERVAL 3000
#define BAUDRATE 115200
#define GPS_BAUDRATE 9600
#define NMEA_SENTENCE_MAX 300

static struct pt gps, heartbeat, sensors; // each protothread needs one of these
char nmea_sentence[NMEA_SENTENCE_MAX];
int counter = 0;
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
   
   if(Serial.available() > 3)
   {
        i = 0;
   }
   
   for(int j = 0; j < Serial.available(); j++)
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
  char byteGPS;
  
  PT_BEGIN(pt);
  
  while(1) 
  {
    
    while(1) {
      PT_WAIT_UNTIL(pt, millis() - timestamp > interval);
       timestamp = millis();
       
      if (gpsSerial.available())
      {
         byteGPS = gpsSerial.read();
         handle_byte(byteGPS);
      }
         
//      if (Serial.available())
//        gpsSerial.write(Serial.read());
        
      //Serial.println("GPS");
    }
  }
  
  PT_END(pt);
}

int handle_byte(int byteGPS)
{
  if(byteGPS == '$')
  {
    Serial.println(nmea_sentence);
    counter = 0;
  }
  
  if(counter == 300)
    counter = 0;
  
  nmea_sentence[counter] = byteGPS;
  counter++;
}


PT_THREAD(updateSensorInfo(struct pt *pt, int interval)) {
  static unsigned long timestamp = 0;
  struct pt heartbeat;
  
  PT_BEGIN(pt);
  
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
  transmitGPS(&gps, 5); // by calling them infinitely
  updateSensorInfo(&sensors, 3000);
  verifyHeartbeat(&heartbeat, 1000);
}
