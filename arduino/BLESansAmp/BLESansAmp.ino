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

int pot1Val = 127;
int pot2Val = 127;
int pot3Val = 127;
int pot4Val = 127;

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

  //Init pots
  digitalPotWrite(pot1CS, pot1Val);
  digitalPotWrite(pot2CS, pot2Val);
  digitalPotWrite(pot3CS, pot3Val);
  digitalPotWrite(pot4CS, pot4Val);

}



void loop() {

  if (bleSerial.available()) {
    //String command  = bleSerial.readString();
    byte command[3];
    bleSerial.readBytesUntil('\n', command, 3);

    //int val = bleSerial.read();
    Serial.write("CMD = ");
    Serial.print(command[0], HEX);
    Serial.print(command[1], HEX);
    Serial.print(command[2], HEX);
    Serial.write("\r\n");

    processCommand(command[0], command[1], command[2]);

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

/**
     | CMD  | act | val |
     |1Byte |1Byte|1Byte|
     | 0,1  | 0-6 |0-255|
*/
void processCommand(int cmd, int act, byte val ) {
  switch (cmd) {
    case 0://GET
      getData(act);
      break;
    case 1://SET
      setData(act, val);
      break;
  }
}

void getData(int act) {
  byte resp[3];
  resp[0] = 2; //Neither GET or SET
  resp[1] = act;

  switch (act) {
    case 0:
      resp[2] = pot1Val;
      break;
    case 1:
      resp[2] = pot2Val;
      break;
    case 2:
      resp[2] = pot3Val;
      break;
    case 3:
      resp[2] = pot4Val;
      break;
    default:
      return;
  }

  bleSerial.write(resp, 3);
}

void setData(int act, byte value) {
  switch (act) {
    case 0:
      pot1Val = value;
      digitalPotWrite(pot1CS, pot1Val);
      break;
    case 1:
      pot2Val = value;
      digitalPotWrite(pot2CS, pot2Val);
      break;
    case 2:
      pot3Val = value;
      digitalPotWrite(pot3CS, pot3Val);
      break;
    case 3:
      pot4Val =  value;
      digitalPotWrite(pot4CS, pot4Val);
      break;
  }
}

