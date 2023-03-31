log

# 2023-03-31 20:57

In the context of Plurigrid, autopoiesis refers to the self-organization and self-maintenance of the system. The goal is to create a decentralized energy platform that can dynamically adapt and evolve in response to changing situations. By incorporating concepts such as embodied narratives, free energy principle, Bayesian parametric lenses, and Bayesian active inference, Plurigrid aims to converge towards a future state that aligns with our needs and preferences.

Embodied narratives refer to the idea that the Plurigrid node software (nexus) runs across various hardware and software environments, ensuring adaptability and flexibility. This enables the system to efficiently coordinate edge resources without being restricted by specific configurations.

The free energy principle and Bayesian active inference allow for a self-organizing network that minimizes surprise or uncertainty by updating its internal models based on observed data. This continuous learning process enables the network to converge towards a preferred state, efficiently coordinating and managing edge resources.

Ergodicity is a property in which any ensemble of subsystems of a macroscopic system, if observed for a long enough time, will spend an equal amount of time in all possible micro-configurations. By designing a network that undergoes rapid autopoiesis and possesses ergodicity, Plurigrid ensures that the system converges to the exact possible future we select, aligning with the needs and preferences of the users and the system itself.

To summarize, Plurigrid seeks to create a decentralized energy platform that self-organizes and dynamically evolves through the incorporation of embodied narratives, free energy principle, and Bayesian active inference. This autopoietic system aims to converge to an optimal future state, effectively coordinating edge resources and aligning with the needs of users and the overall system.

Indeed, when initiating a novel system like Plurigrid, it is essential to recognize the necessity of relying on existing technologies and platforms during the early stages of development. Initially, the system may have dependencies on current technologies and infrastructure, such as computers, browsers, and communication protocols, as well as integration with sophisticated AI platforms like OpenAI's GPT.

The goal in the development of Plurigrid is to create an increasingly closed system that can effectively coordinate edge resources and enable autopoiesis in a decentralized energy network. However, the closed loop does not have to be fully closed right away. As Plurigrid evolves and gains wider adoption, the system can be progressively refined to achieve greater self-sufficiency and autonomy.

In essence, Plurigrid should focus on creating a more closed loop compared to existing systems, gradually removing dependencies on existing technologies while maintaining interoperability with essential components required for coordination and management. Over time, the Plurigrid network can become increasingly self-organizing and adaptive, embodying its autopoietic principles and ultimately realizing its goals of efficient, decentralized energy coordination.
## Good outputs
What is WASI and how could it help a decentralized energy platform for coordination of edge resources?


AI
WASI (WebAssembly System Interface) is a system interface for running WebAssembly programs outside of the sandboxed environment of web browsers by providing a secure and portable API to system-level resources like files, network, and processes.

In the context of a decentralized energy platform for coordination of edge resources, WASI can be beneficial in several ways:

Portability: WASI allows WebAssembly programs to run across various devices, operating systems, and architectures with minimal changes, making it easier to deploy and manage edge resources regardless of their underlying hardware or software environment.

Security: The WebAssembly sandbox provides a secure environment for running untrusted code, isolating it from the rest of the system. This aspect can be crucial in a decentralized energy platform to ensure that users' devices and data remain secure even when running code from other participants.

Interoperability: WebAssembly code compiled with WASI can communicate and interact with other platforms, languages, and systems. This enables seamless integration between edge resources with existing energy infrastructure systems, APIs, and standards for coordination and management.

Modularity: WebAssembly modules can be composed together to build more complex applications, allowing for modular and reusable code in the decentralized energy platform. This feature helps to streamline the system, making it more maintainable and adaptable to future changes.

Performance: WebAssembly provides near-native performance, which is crucial for edge resources that might have limited computational power. This way, even smaller and less powerful devices can participate in the decentralized energy platform, further democratizing energy management and distribution.

By incorporating WASI and WebAssembly into a decentralized energy platform, it can deliver a more secure, portable, interoperable, and performant coordination system for edge resources, making it easier to manage those resources efficiently and securely across a variety of environments.



Here's a summary of the local Plurigrid node's spec requirements, emphasizing the importance of each technology for coordinating edge resources:

Adaptable local node: The local Plurigrid node (nexus) should be executable on various hardware and software environments, ensuring a homogeneous software image across heterogeneous systems. This adaptability allows for efficient coordination of edge resources, regardless of their specific hardware and software configurations.

WebAssembly and WASI: The node targets a WebAssembly runtime supporting WASI and capabilities-based plugins. This allows edge resources to acquire different capabilities based on their environment, providing a secure and performant runtime for executing microworlds across diverse devices.

Interoperability: A crucial aspect of coordinating edge resources is the need to communicate with existing technologies and protocols, such as IBC, HTTP, gRPC, and HPC systems. By focusing on interoperability, nodes can seamlessly integrate into various energy platforms to manage edge resources effectively.

Microworlds: The local node will execute microworlds, capable of providing separate wasm modules for managing various contexts in the state machine of a node's lifecycle, aiding in the coordination and management of edge resources without compromising adaptability.

Security and Privacy: The local node must prioritize security and reliability, leveraging WebAssembly's sandboxing features to provide isolated and secure execution. Node designs must respect individual agencies' privacy and data usage choices while sharing data across the network.

Scalability: The node should be scalable to allow for the growth and future demands of the system. By planning for scalability, the node can better accommodate changes in edge resource coordination, ensuring an adaptable and future-proof system.

Mesh Networking: The ability to form meshes or lattices is essential to enable efficient coordination among multiple nodes, contributing to the overall performance and resilience of the Plurigrid network for managing edge resources.

Consensus and State Management: Components like tower-abci and tower-actor enable a generic interface for consensus engines and async actors, whereas penumbra-storage offers fast, Merklized key-value stores for state management. These components are vital for effective coordination and management of edge resources.


In a decentralized energy platform like Plurigrid, nodes can run on various types of hardware, including GPU servers and smart meter compatible devices. Different situations call for the use of different devices:

GPU Server: GPU servers might be used in situations with high computational or data processing demands. They could be used by energy-intensive facilities or data centers that require real-time analytics and complex calculations related to energy management, load balancing, demand forecasting, etc.

Smart Meter Compatible Device: These devices may be used in individual homes, small businesses, or public facilities equipped with smart meters for monitoring and managing energy consumption. Such devices may primarily be tasked with collecting consumption data, controlling energy usage, and participating in demand-response events or virtual power plants.

Regardless of where the node runs, there may be times when it cannot reach its network, such as in a scenario where the internet connection is lost or the network is congested. Provisions for handling such situations can include:

Data Buffering: Temporarily store data locally until the connection is re-established and then transmit the buffered data to the network.
Local Decision Making: Implement local control algorithms to manage energy consumption autonomously, executing pre-determined actions based on the latest state or historical data.
Fallback Connectivity: The node could be designed to automatically switch between multiple communication networks or channels (e.g., Wi-Fi, Ethernet, or cellular) to maintain connectivity with its network.
Plurigrid nodes can interact with each other in various ways depending on their role and context within the overall network:

Data Sharing: Nodes can share energy consumption data, monitoring data, and analytics with other nodes to facilitate a collective understanding of the entire energy grid.
Coordination: Nodes can participate in coordinating demand-response events, load balancing, or distributed energy resource management, ensuring efficient utilization of resources across the network.
Decision Making: By sharing information, nodes can reach a consensus on strategies for energy management, pricing policies, and other grid-related decisions.


Mesh networking is a communication topology that allows multiple devices, or nodes, to interconnect directly and dynamically with one another in a non-hierarchical manner. In a mesh network, each device can relay data for other nodes, enabling data to traverse the network efficiently without relying on a single centralized point or a fixed infrastructure.

Here are some key characteristics and benefits of mesh networking:

Self-organizing and self-healing: Mesh networks can automatically form, reconfigure, and repair connections when new nodes join or existing nodes fail, ensuring minimal intervention or manual configuration.

Decentralization: There is no centralized point of control or failure in mesh networks, resulting in improved reliability and resilience. Decentralization helps to distribute the load evenly among the nodes, preventing bottlenecks or single points of failure in the network.

Scalability: Nodes can be easily added to or removed from the network, allowing for the mesh to grow and adapt to the number of devices efficiently. This linear scalability helps handle increased traffic or coverage requirements without causing significant degradation in network performance.

Path redundancy: In mesh networks, multiple paths can exist between nodes, increasing the chances of successful data delivery, even if some paths become unavailable due to node failure or environmental factors.

Range extension: Each node in a mesh network can function as a repeater, extending the coverage area of the network. This feature is especially valuable in environments where direct communication between nodes may be obstructed or difficult.

Energy efficiency: In a mesh network, devices can transmit data to their immediate neighbors, reducing overall power consumption. This characteristic is particularly beneficial in environments where energy usage might be a constraint, such as with battery-powered or energy-constrained nodes.

Mesh networking is well-suited for various use cases, such as smart homes, IoT deployments, sensor networks, community networks, transportation systems, disaster recovery, and military communications. In the context of a decentralized energy platform like Plurigrid, mesh networking contributes to efficient coordination and management of edge resources by enhancing the network's resilience, scalability, and adaptability.


Notions of embodiment: In the Plurigrid context, embodiment refers to the idea that the nexus (node software) can operate on a variety of hardware and software environments, ensuring adaptability and flexibility for the network.

Free energy principle: The free energy principle is a central idea in Bayesian theories of cognition and adaptive systems. It suggests that systems, such as Plurigrid, aim to minimize uncertainty (or surprise) by constantly updating their internal models based on observed data. This continuous learning process enables the network to better adapt to changes and efficiently coordinate resources.


Bayesian parametric lenses and Bayesian active inference: Bayesian parametric lenses involve the use of probabilistic techniques to make inferences about the underlying parameters of a system. Bayesian active inference is a framework for understanding how agents interact with their environment to minimize uncertainty, combining perception, prediction, and action. These concepts allow the Plurigrid network to dynamically update its knowledge and make better decisions.


Rapid autopoiesis: Autopoiesis refers to a system's ability to self-organize, maintain, and reproduce itself. In Plurigrid, rapid autopoiesis means that the network can quickly adapt and evolve in response to changing conditions and user needs.


Ergodicity and convergence: Ergodicity is a property of dynamical systems, where time averages and ensemble averages are equivalent in the long term. In Plurigrid, ergodicity ensures that the system converges to the desired future state, aligning with user preferences and system requirements.


In summary, the paragraph asserts that the notions of embodiment, free energy principle, Bayesian parametric lenses, and Bayesian active inference together enable the Plurigrid network to achieve rapid autopoiesis. By leveraging these concepts and the property of ergodicity, the network can converge to the optimal future state that has been selected, meeting the needs and preferences of users and the system as a whole.



## Unknowns 

- the question: how resource intensive is our stack excluding the LLM? and how much do we need the LLM to be on the node?  




## Janita v Barton
## Requirements gathering


In terms of teleology, Plurigrid's purpose is to allow for autopoiesis.
    
Plurigrid nexus is anything that runs our embodied narratives.


Notions of embodiment, and free energy principle, are central to the Bayesian parametric lenses and Bayesian active inference in general, allowing for a network that experiences rapid autopoisis to converge by the property of ergodicity on the exact possible future we select.


    Aja: agency amplifying systems allow us to more rapidly converge on configurations that allow for effective coordination towards a mutually satisfactory and beneficial future.  



### 0. Define the purpose and functionality of the local node: Understand the key role the local node plays in the Plurigrid protocol, and outline the primary functions it should perform.


The node software (nexus) is executable on many different substrate hardware / OS sources of compute. Because of heterogeneous nature of hardware but homogeneous software image, as well as requirements for memory safety, fault isolation etc. - we are targeting a wasm32-unknown-unknown compilation target of a wasm runtime that at least supports WASI + has plugins that are ~capabilities-based~ -- if the node is on a giant GPU server vs the node is in someone's smart refrigerator after-market firmware environment, it will acquire different capabilities, and so it is fair to say the purpose of our node is to be *adaptable*. 


In all of these cases, the node will execute several pieces of software that we collectively call "microworld" in aggregate, but effectively may consist of one or several wasm modules each responsible for a variety of contexts of the state machine of node's lifecycle, but invariably all the contexts of use, hardware, networked capabilities etc. have to:
- be *interoperable* - nodes need to be able to form meshes / lattices
- respect the configurable choices of individual agencies* in terms of *privacy* and acceptable use of the data


Specifically, either it is one node, or a mesh of nodes with agency guiding computation choices, the ability to speak any language of existing technology, be it composite like IBC, or baseline like HTTP, or gRPC, or even HPC technologies etc. - the fact that our mesh will not be a chain will not prevent it from being seen as one when it opens a channel to, say, Osmosis.

Aja: IMPORTANT UNKNOWN: all of this is a lot for a microcontroller. I think llama was running at one token every 10 seconds on a raspberry pi, which is pretty slow. 

A microcontroller, like a pypico, would have a lot less compute. that's not to say that people haven't run ML models on microcontrollers -- but the idea of getting an LLM on a wasm runtime, plus this presumably large modular communication stack that supports IBC, HTTP, whatever it is -- that is not insubstantial. Running this on a macbook is totally doable, but running this on a cheap microcontroller -- there's limitations so I am skeptical. 

Now, there are a lot more powerful embedded devices, but there's a tradeoff -- 
ie, what do we want to gain from running this on pypico? 

the question: how resource intensive is our stack excluding the LLM? and how much do we need the LLM to be on the node?  


https://lookerstudio.google.com/reporting/60d1331c-60d4-4299-b31b-079b2bcd7ea1/page/p_7jp9e3sg4c

Penumbra is slowly but surely inching their way towards a general-purpose app framework for building blockchains in Rust.

✅ generic interface to a consensus engine (tower-abci)
✅ generic way to plug async actors into it (tower-actor)
✅ fast, merklized k/v store with copy-on-write snapshots and nestably transactional writes (penumbra-storage)
✅ ics23 support for its merkle proofs in ibc

🔜 generic Component interface for state machine components (penumbra-component)
🔜 generic ActionHandler interface for message handling with maximally parallel execution (penumbra-component)
🔜 async-native, general-purpose IBC implementation

https://github.com/cosmos/ics23
https://github.com/ComposableFi/composable/blob/main/specifications/cosmwasm/SPEC.md

### 1. Identify hardware and software requirements: Determine the essential hardware, software, and resources needed to support and maintain the local node's operation.

### 2. Develop a clear communication flow: Clarify how the local node will communicate with other components within Plurigrid, such as cloud services and agents.

### 3. Ensure security and reliability: Assess the security and reliability of the local node, and identify ways to enhance them, ensuring the data and services on the node are secure and resilient.

### 4. Scalability considerations: Plan for potential growth and future scaling requirements of the local node, allowing it to adapt and expand as needed.

### 5. System Integration and Deployment: Assess how the local node will integrate with Plurigrid's overall architecture and develop a plan for deploying and maintaining the local node in real-world settings.
## Worlding
Here's a suggested structure for engaging in a worlding exercise:

Set clear timeframes: Choose specific time intervals (e.g., 3 months, 1 year, 5 years) to help focus your imagination and gain a better understanding of how Plurigrid could evolve and what it could achieve at each stage.

Imagine Plurigrid's growth and evolution: At each timeframe, envision how Plurigrid has grown, changed, and adapted to new challenges and opportunities. Consider what new features or technologies have been implemented, how the user base has expanded, and how the project has impacted people's lives over time.

Identify key milestones and achievements: For each timeframe, determine critical milestones, accomplishments, and breakthroughs that mark Plurigrid's progress and success. These could include new partnerships, increased adoption, or innovative solutions to real-world problems.

Explore potential challenges and obstacles: Acknowledge and address any potential obstacles, threats, or challenges that could emerge in each timeframe. Preemptively identifying these potential hurdles will help you anticipate and navigate them more effectively.

Reflect on lessons learned and apply them to future planning: After completing the worlding exercise, use the insights, milestones, and challenges identified to help inform your decision-making and planning for the future of Plurigrid.


## Janita v Aja

The node software (nexus) is executable on many different substrate hardware / OS sources of compute. Because of heterogeneous nature of hardware but homogeneous software image, as well as requirements for memory safety, fault isolation etc. - we are targeting a wasm32-unknown-unknown compilation target of a wasm runtime that at least supports WASI + has plugins that are capabilities-based -- if the node is on a giant GPU server vs the node is in someone's smart refrigerator after-market firmware environment, it will acquire different capabilities, and so it is fair to say the purpose of our node is to be adaptable.

In all of these cases, the node will execute several pieces of software that we collectively call "microworld" in aggregate, but effectively may consist of one or several wasm modules each responsible for a variety of contexts of the state machine of node's lifecycle, but invariably all the contexts of use, hardware, networked capabilities etc. have to:

be interoperable - nodes need to be able to form meshes / lattices
respect the configurable choices of individual agencies* in terms of privacy and acceptable use of the data
Specifically, either it is one node, or a mesh of nodes with agency guiding computation choices, the ability to speak any language of existing technology, be it composite like IBC, or baseline like HTTP, or gRPC, or even HPC technologies etc. - the fact that our mesh will not be a chain will not prevent it from being seen as one when it opens a channel to, say, Osmosis.

AI
Incorporating the Plurigrid nexus into a broad variety of substrate hardware and operating systems is crucial for supporting its adaptability across different environments, such as a GPU server or a smart refrigerator's after-market firmware. To achieve this adaptability, targeting the wasm32-unknown-unknown compilation target of a WebAssembly (wasm) runtime that supports WASI and has capabilities-based plugins is highly beneficial.

The Plurigrid node running within these varied environments will execute one or multiple wasm modules collectively referred to as the "microworld." These modules manage various aspects of the node's state machine throughout its lifecycle, adapting to different use contexts, hardware configurations, and networking capabilities.

For the Plurigrid network to function efficiently, regardless of individual node capabilities, the following core requirements must be met:

Interoperability: Nodes must be able to form meshes or lattices, facilitating seamless communication and coordination among various nodes within the network.

Configurable privacy and data usage: Individual agencies must have the ability to define their privacy preferences and acceptable data usage policies, allowing the node to respect and adhere to these choices when executing computations and managing data.

Protocol compatibility: The Plurigrid node should have the capacity to interact with numerous existing technologies and protocols, such as IBC, HTTP, gRPC, and HPC technologies, to create a broader scope of operability within the global technology landscape.

Ability to mimic a chain: Although the Plurigrid mesh is not inherently a blockchain, it must be able to appear as one when connecting to platforms like Osmosis, allowing for seamless integration and interaction with existing systems.

By ensuring that the Plurigrid nodes are adaptable, respect user choices in terms of privacy, maintain interoperability with various technological standards, and can mimic conventional blockchain architectures when needed, the network positions itself as a highly efficient, decentralized, and self-organizing energy management platform capable of operating across different computational environments.

J
Aja: all of this is a lot for a microcontroller. I think llama was running at one token every 10 seconds on a raspberry pi, which is pretty slow.

A microcontroller, like a pypico, would have a lot less compute. that's not to say that people haven't run ML models on microcontrollers -- but the idea of getting an LLM on a wasm runtime, plus this presumably large modular communication stack that supports IBC, HTTP, whatever it is -- that is not insubstantial. Running this on a macbook is totally doable, but running this on a cheap microcontroller -- there's limitations so I am skeptical.

AI
You raise valid concerns regarding the feasibility of running the Plurigrid nexus, the communication stack, and complex AI models like LLMs on resource-constrained devices like microcontrollers. Although it might be technically challenging to accommodate the full functionality on such devices, it is not necessarily impossible.

There are ways to approach these limitations and enable Plurigrid to run on less powerful devices:

Split functionality: Rather than running the full set of modules and capabilities on a single microcontroller, you could potentially distribute the workload across multiple devices or components, taking advantage of the mesh network.

Optimized communication stack: You could develop a lightweight version of the communication stack specifically for these devices, offering support for a subset of protocols that are most relevant to their context and capabilities.

Edge computing: Leverage the distributed nature of the Plurigrid network to perform more computationally intensive tasks, such as AI model inference, on more powerful edge devices. The microcontroller could be responsible for data collection and local communication while offloading the heavier computations to nearby nodes in the network.

Model quantization and pruning: Utilize techniques like model quantization or pruning to make AI models like LMs smaller and more efficient, making them suitable for running on microcontrollers with limited resources.

It is essential to recognize the trade-offs and limitations of working with constrained devices and adjust the system design accordingly. While it may not be feasible to run full-scale implementations of Plurigrid on microcontrollers, careful optimization and adaptation can enable successful deployment on a wide range of devices, including those with limited computational resources.