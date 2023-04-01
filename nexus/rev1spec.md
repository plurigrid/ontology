# Compute

## High power
- Apple Silicon Macbook
<<<<<<< Updated upstream
=======
  - The Apple Silicon MacBook provides plenty of computational power to run the Plurigrid node and support a wide range of functionality, making it an ideal choice for testing and development purposes.
>>>>>>> Stashed changes

## Low power
- RPI Compute Module 4
- RPI 4 Model B
<<<<<<< Updated upstream

## Embedded
- RPI Pico W
Note: RPI is used for accessibility to devs and should not be targeted for production
=======
  - Raspberry Pi devices offer a lower-power, cost-effective option to host the node software while providing sufficient resources to manage a functional local node in practical settings.

## Embedded
- RPI Pico W
  - Note: RPI is used for accessibility to devs and should not be targeted for production. RPI Pico W represents an embedded solution that may require further research and optimization due to its limited computational resources.
>>>>>>> Stashed changes

# Stack

## High power
<<<<<<< Updated upstream
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
=======
- LLaMa can run directly on Apple Silicon, leveraging the MacBook's powerful hardware to support the node's extensive set of capabilities.
- Can be compiled to WASM to run in WASM edge or WASM3 runtime, ensuring compatibility and resource-efficient execution of the node software.

## Low power
- LLaMa can run directly on Apple Silicon, indicating compatibility with Raspberry Pi devices, which have similar hardware architecture.
- Can be compiled to WASM to run in WASM edge or WASM3 runtime, maintaining the benefits of platform independence and resource-efficient execution.
- Similar to MacBook, Raspberry Pi devices also run on top of an OS, providing a familiar environment for node deployment and management.

## Embedded
- LLaMa has not yet been run on the RP2040 (or any other MCU to my knowledge), implying that additional research and development may be needed to adapt the node software for use on embedded devices like RPI Pico W.
- Will most likely need to build images off device due to lack of OS, adding an extra step in the deployment process.
- Does not support WASM edge, supported by WASM3, which is critical for ensuring compatibility and resource-efficient execution on low-powered devices.
- This device is a longer-term target and requires the most development, as the current stack has not yet been optimized for such resource-constrained constraints.

# Networking
- All devices have built-in WiFi networking, simplifying connectivity and enabling various network configurations. Additional network interfaces can be added down the line to support specific use cases and enhance connectivity.
>>>>>>> Stashed changes

# Built in transducers

## High power
<<<<<<< Updated upstream
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
=======
- Equipped with a variety of sensors including thermal, brightness, microphone(s), camera, and others, the Apple Silicon MacBook provides a rich set of built-in transducers for a diverse range of applications.
- Audio and display.outputs allow for easy monitoring and control.
- WiFi and Bluetooth enable wireless communication and peripheral connectivity.
- Will need to utilize external peripherals for additional signaling or communicate with another node to extend its capabilities.

## Low power
- Limited built-in sensors, depending on the Raspberry Pi model, but can be easily augmented with external sensor modules.
- GPIO pins to connect sensors, such as temperature, humidity, light, motion, and others, offering access to a wide range of sensing capabilities.
- Audio output (3.5mm jack or HDMI) and HDMI video output, providing simple audiovisual interfaces.
- WiFi and Bluetooth (available on RPi 4 Model B and Compute Module 4) for network connectivity and peripheral support.
- USB ports for connecting peripherals, such as cameras or other devices, allowing for further customization of the local node capabilities.
- Wide support for external sensor modules and shields, allowing for various types of transducers to be added as needed.
- Can be connected to other_nodes for extended functionality and communication, forming a mesh or lattice with other Plurigrid nodes.

## Embedded
- Raspberry Pi Pico W offers a lightweight and cost-effective solution for embedded applications. However, it lacks built-in sensors or transducers.
- Can connect a range of sensors using the available GPIO pins, such as temperature, humidity, light, motion, and others, allowing the node to interact with its environment.
- Built-in 2.4GHz 802.11n wireless LAN for wireless communication and networking; however, it lacks built-in Bluetooth. External modules can be added for Bluetooth communication if needed.
- No built-in audio or video output; peripherals and modules can be added for specific applications.
- Can be used in conjunction with other devices or nodes for extending functionality.and communication, but with the understanding that the Pico W comes with inherent trade-offs in terms of processing power and built-in capabilities.
>>>>>>> Stashed changes

# External Transducers
- RPI 4 IO board
- Relay Shield
<<<<<<< Updated upstream
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
=======
- Additional circuits will be added or interfaced with. The current design is for light bulb switching.
- Apple Silicon Macbooks should communicate with other nodes or devices for additional input/output (IO) functionality, as they are more powerful and better equipped to manage communication across the Plurigrid system.

# Development road map
1. Run first-gen LLaMa agents on Macbooks.
2. Compile for WASM and.run agents in WASM runtime on Macbooks.
3. Run WASM runtime with.agent on RPI 4 devices.
4. Build stack for embedded target.
5. Run WASM on RPI Pico W.
6. Run IBC node On Macbooks.
7. Run IBC.node on RPI 4 devices.
8. Run IBC node on RPI Pico W.
9. Add relay expansion.
10. Run on-chain LampDAO demo.

# Looking forward
Once we have successfully ported our stack to embedded devices, we will want to explore options for embedding the node software into various types of hardware, such as smart assistants, smart meters, PLCs, etc. It should be noted that LLaMa is currently very slow on the Pi4 and will be extremely slow on the Pi Pico. There are other options for the Pico, like TinyML, but hardware development will need to move in step with breakthroughs in ML. Once we have these rev1 RPI devices, we may want to look into more powerful embedded targets. Late Panda and Arduino look promising!
>>>>>>> Stashed changes
