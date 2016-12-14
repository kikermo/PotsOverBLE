/*
   Analogue guitar effect, digital control over BLE
*/


// inslude the SPI library:
#include <SPI.h>
#include <SoftwareSerial.h>

SoftwareSerial bleSerial(2, 3); // RX, TX

// set pot select pin
const int pot1CS = 10;
const int pot2CS = 9;
const int pot3CS = 8;
const int pot4CS = 7;

const int sw1 = 0;
const int sw2 = 2;
const int sw3 = 4;
const int sw4 = 6;


const int potWriteCmd = B00010011;

void setup() {
  // set output pins:
  pinMode(pot1CS, OUTPUT);
  pinMode(pot2CS, OUTPUT);
  pinMode(pot3CS, OUTPUT);
  pinMode(pot4CS, OUTPUT);


  // initialize SPI:
  SPI.begin();



  //initialize serial port for logs
  Serial.begin(9600);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }

  bleSerial.begin(9600);
 
  digitalPotWrite(pot1CS,0);
  digitalPotWrite(pot2CS,63);
  digitalPotWrite(pot3CS,127);
  digitalPotWrite(pot4CS,255);
  
}



void loop() {

  if (bleSerial.available()) {
    int val = bleSerial.read();
    Serial.write("value = ");
    Serial.print(val, DEC);
    Serial.write("\r\n");

   // digitalPotWrite(pot1CS, val);
  }

  if (Serial.available()) {
    bleSerial.write(Serial.read());
  }

}




void digitalPotWrite(int potCS, int value) {
  // put the CS pin to low in order to select the chip:
  digitalWrite(potCS, LOW);

  SPI.transfer(potWriteCmd);
  SPI.transfer(value);

  // put the CS pin to high for transfering the data
  digitalWrite(potCS, HIGH);
}

void switchWrite(int switchL, int value) {
  analogWrite(switchL, HIGH);
  analogWrite((switchL + 1), HIGH);

}

