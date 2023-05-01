classDiagram
Layer3-- Layer2:
Layer2-- Layer1:
Layer1-- Layer0:
Layer0-- LayerMinus1:
class LayerMinus1 {
        Embedded Controller
        --Peripheral interface hardware: Controls device - specific functions
--Peripheral firmware instructions: Directly control the hardware
    }
class Layer0 {
        Linux Kernel: Manages system resources
        --Device tree(XML): Reports status of controllers to the kernel
--Kernel module / device driver: Interfaces with hardware components
    }
class Layer1 {
        WASI peripheral modules: Custom modules for WASM programs
        --WASMCloud: Enables apps to use special instructions for hardware
        --Cosmonic: Orchestrates wasm cloud apps in a distributed environment
    }
class Layer2 {
  CRDT: Synchronizes local databases on each device
}
class Layer3 {
  LLM: Local learning models for coordination
        --Formal verification: Power Systems Julia for grid analysis
        --IBC: Blockchain software for consensus and security
    }
