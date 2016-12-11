/*
   Digital Potentiometer control over BLE

   MCP4110 digital Pots SPI interface
*/


// inslude the SPI library:
#include <SPI.h>
#include <SoftwareSerial.h>

SoftwareSerial bleSerial(2, 3); // RX, TX

// set pot select pin
const int potSS = 10;

const int potWriteCmd = B00010011;

void setup() {
  // set output pins:
  pinMode(potSS, OUTPUT);

  // initialize SPI:
  SPI.begin();



  //initialize serial port for logs
  Serial.begin(9600);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }

  bleSerial.begin(9600);
//  digitalPotWrite(0);
}



void loop() {
  //  //test
  //  digitalPotWrite(pot1SS,36);
  //  delay(100);
  //    Wire.readByte();

  if (bleSerial.available()) {
    int val = bleSerial.read();
    Serial.write("value = ");
    Serial.print(val,DEC);
    Serial.write("\r\n");

    digitalPotWrite(val);
  }
  
  if (Serial.available()) {
    bleSerial.write(Serial.read());
  }

}




void digitalPotWrite(int value) {
  // put the SS pin to low in order to select the chip:
  digitalWrite(potSS, LOW);

  SPI.transfer(potWriteCmd);
  SPI.transfer(value);

  // put the SS pin to high for transfering the data
  digitalWrite(potSS, HIGH);
}


