# BLEproject

This project involves an application that communicates with a BLE Bulb application.
The application is acting as a central while the BLE Bulb is the peripheral. 

The developed application can 
  1. Read/Write to the GATT service to turn on/off the BULB light
  
      0 -> BULB OFF
      1 -> BULB ON
  2. Read/Write to the GATT service to turn on/off the ALARM
  
      0 -> ALARM ON
      1 -> ALARM OFF
  3. Read/Notify to the GATT service to be notified of the change in TEMPERATURE.
