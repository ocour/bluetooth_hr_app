# bluetooth_hr_app
BLuetooth LE heart rate app

A BLE app which scans for ble devices, after user selects an device to connect to it will do so and chech that the heart rate service is supported,
if not the device will be disconnected from. If connected device does have the hr service notification for it will be enabled. The Hr values will be shown to the user after.

Uses runtime permissions, flows, androids ble libraries, material 3 and more.

Minimal error checking, no unit tests, only tested on Android 11 phone. Should work for others as well but not tested.
