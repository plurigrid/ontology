# Compute

## High power
- Apple Silicon Macbook

## Low power
- RPI Compute Module 4
- RPI 4 Model B

## Embedded
- RPI Pico W
Note: RPI is used for accessibility to devs and should not be targeted for production

# Stack

## High power
- LLaMa can run directly on Apple Silicon
- Can be compiled to WASM to run in WASM edge or WASM3 runtime

## Low power
- LLaMa can run directly on Apple Silicon
- Can be compiled to WASM to run in WASM edge or WASM3 runtime
- Similar to MacBook, both devices run on top of an OS

## Embedded
- LLaMa has not yet been run on the RP2040 (or any other MCU to my knowledge)
- Will most likely need to build images off device due to lack of OS
- Does not support WASM edge, supported by WASM3
- This device is a longer-term target and requires the most development

# Networking
- All devices have built-in WiFi networking. Additional network interfaces can be added down the line

# Built in transducers

## High power
- Equipped with a variety of sensors including thermal, brightness, microphone(s), camera, and others
- Audio and display outputs
- WiFi and Bluetooth
- Will need to utilize external peripherals for additional signaling or will need to communicate with another node

## Low power
- Limited built-in sensors, depending on the Raspberry Pi model
- GPIO pins to connect sensors, such as temperature, humidity, light, motion, and others
- Audio output (3.5mm jack or HDMI) and HDMI video output
- WiFi and Bluetooth (available on RPi 4 Model B and Compute Module 4)
- USB ports for connecting peripherals, such as cameras or other devices
- Wide support for external sensor modules and shields, allowing for various types of transducers to be added as needed
- Can be connected to other nodes for extended functionality and communication

## Embedded
- Raspberry Pi Pico W
- No built-in sensors or transducers
- Can connect a range of sensors using the available GPIO pins, such as temperature, humidity, light, motion, and others
- Built-in 2.4GHz 802.11n wireless LAN for wireless communication and networking
- Lacks built-in Bluetooth; external modules can be added for Bluetooth communication, if needed
- No built-in audio or video output; peripherals and modules can be added for specific applications
- Can be used in conjunction with other devices or nodes for extending functionality and communication

# External Transducers
- RPI 4 IO board
- Relay Shield
- Addition circuits will be added or interfaced with. Current deisgn is for light bulb switching
- Macbooks should communicate with other nodes for additional IO functionality

# Development road map
1) Run first gen LLaMa agents on Macbooks
2) Compile for WASM and run agents in WASM runtime on Macbooks
3) Run WASM runtime with agent on RPI 4 devices
4) Build stack for embeded target
5) Run WASM on RPI Pico W
6) Run IBC node On Macbooks
7) Run IBC node on RPI 4 devices
8) Run IBC node on  RPI Pico W
9) Add relay expansion
10) run on chain LampDAO demo

# Looking forward
Once we have sucesfully ported our stack to embeded we will want to look into options to embed into. This will begin the "true" hardware phase where we will condcut research to determin which deivces to build into, for example, smart assitants, smart meters, PLCs, etc. It should noted that LLaMa is curretnly very slow on the Pi4 and will be extremly slow on the Pi Pico. There are other options for the Pico like TinyML but hardware development will need to move instep with breakthroughs in ML. Once we have these rev1 RPI devices we may want to look into more powerfull embeded targets. Late Panda and Arduino look promising!
