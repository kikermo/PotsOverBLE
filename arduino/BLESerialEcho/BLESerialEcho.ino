/*
  Serial comunication with ble module
*/

#include <SoftwareSerial.h>

SoftwareSerial bleSerial(2, 3); // RX, TX

void setup() {
  //initialize serial port for logs
  Serial.begin(9600);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }

  bleSerial.begin(9600);
}



void loop() {

  if (bleSerial.available()) {
    Serial.write(bleSerial.read());
  }
  
  if (Serial.available()) {
    bleSerial.write(Serial.read());
  }

}
