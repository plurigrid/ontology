# nexus
```
Done:
- [x] Release Laura v0 Alpha
- [x] Develop Plurigrid Protocol
- [x] Plan device architecture for first hardware node
- [x] Include Scenario 2 example for the Plurigrid presentation
- [x] Discuss partnership types for Plurigrid Inc.
- [x] Share investor-focused presentation titles and content
- [x] Identify top priorities for Series A
- [x] Review goals for realism and achievability

Next:
- [ ] Prioritize tasks based on available resources, potential impact, and dependencies
- [ ] Establish short-term objectives within each priority area to build momentum
- [ ] Draft a flexible timeline and roadmap that allows for adjustments as needed
- [ ] Organize a meeting with the slide artisan Pim
- [ ] Discuss the Plurigrid Protocol presentation outline, focusing on investor relevance
- [ ] Develop slides for the investor-focused presentation, including configuration slides

Upcoming:
- [ ] Rehearse the Plurigrid Protocol presentation for investors
- [ ] Present the Plurigrid Protocol at investor meetings/events
- [ ] Develop a comprehensive prototype or demo of the Plurigrid product
- [ ] Research and engage with potential partners for various partnership types
- [ ] Support all major and minor languages within the company through multilingual conversational agents
- [ ] Develop 1000+ microworlds and a grid designer UI
- [ ] Deploy the Protocol in 1000+ instances
- [ ] Create an app (mobile/desktop) for user interaction
- [ ] Implement privacy measures for DER/prosumer coalitions
- [ ] Develop and prototype hardware dev kit designs
- [ ] Establish the minimum viable base of operation in global outposts
- [ ] Secure a source of recurring revenue
- [ ] Assemble a set of board candidates for future growth
- [ ] Promote the adoption of conversational agents internally, leveraging low-cost and edge-operated capability
```

Week of April 3rd - April 9th Roadmap:

Task 1: Develop investor-focused presentation (2 people, 20 hours)

Create a visually appealing and comprehensive slide deck
Align the presentation with the needs and interests of investors
Collaborate with slide artisan Pim
Rehearse the presentation, refine, and finalize
Task 2: Prototype development (6 people, 180 hours)

Design the user interface for the app (mobile/desktop)
Implement the backend infrastructure and functionality
Integrate multilingual support
Perform testing and iterate based on feedback
Involve the broader ecosystem of developers for support, bug fixes, and improvements
Task 3: Promote Plurigrid through bounty campaigns (2 people, 16 hours)

Design repl.it bounties for the developer community
Coordinate the submission process and evaluate submissions
Distribute rewards and recognize exceptional contributors
Task 4: Engage with worlding game studio in Prague (3 people, 40 hours)

Participate in brainstorming sessions to generate ideas and concepts
Collaborate with the studio to integrate game-like elements into Plurigrid products
Develop a plan to implement gaming mechanics in the longer term
Task 5: Work closely with the ZK alliance of advisors (2 people, 20 hours)

Schedule meetings to discuss strategy and technical aspects
Implement suggestions and integrate feedback into Plurigrid products
Coordinate with the alliance to align Plurigrid's goals with broader industry developments
Task 6: Manage contractors, interns, and other resources (1 person, 20 hours)

Allocate tasks and responsibilities to team members
Monitor and guide progress and maintain open communication channels
Address roadblocks and provide support as needed
Task 7: Maintain strong relationships with the crypto fund accelerator (1 person, 8 hours)

Regularly report progress and milestones
Request resources and support when needed
Build and maintain a healthy relationship for the benefit of the project
# code
by popular demand, adding [kanban.py](../scripts/kanban.py): takes this interoperable data format (markdown) and converts to GitHub Issues.

[Try it with this GitHub Project: What is to be done?](https://github.com/orgs/plurigrid/projects/3)
# microworlds
Hello, Microworld!
```
using PowerSystems, TimeSeries

# Create the system with a 100.0 MVA base
sys = System(100.0)

# Create buses
bus1 = Bus("bus1", 20.0; bustype = BusTypes.PQ)
bus2 = Bus("bus2", 20.0; bustype = BusTypes.PQ)
bus3 = Bus("bus3", 20.0; bustype = BusTypes.PQ)

# Add the buses to the system
add_component!(sys, bus1)
add_component!(sys, bus2)
add_component!(sys, bus3)

# Define voltage in p.u.
voltage = 1.0

# Load active and reactive power profiles
active_load_profile = TimeArray([i => 1.0 for i in 1:24], 1:24, ["active_load"])
reactive_load_profile = TimeArray([i => 0.8 for i in 1:24], 1:24, ["reactive_load"])

# Create loads
for (i, bus) in enumerate([bus1, bus2, bus3])
    load = ElectricLoad(
        "load_$i",
        bus,
        voltage, # p.u. voltage
        Source(time_series, :active_load),
        Source(time_series, :reactive_load),
    )
    add_component!(sys, load)
end

# Add diesel generator to the bus3
diesel_gen = ThermalStandard(
    "diesel_gen",
    true,
    bus3,
    50.0,
    25.0,
    (up = 0.0, down = 0.0),
    (up = 50.0, down = 50.0),
    ThreePartCost(
        variable_cost = 30.0,
        fixed_cost = 50.0,
        start_up_cost = 100.0,
    )
)
add_component!(sys, diesel_gen)

# Add transmission line between bus1 and bus2
line12 = Line(
    "line12",
    20.0,
    bus1,
    bus2,
    (series = 0.015, shunt = 0.025),
    1.0,
)
add_component!(sys, line12)

# Add transmission line between bus1 and bus3
line13 = transformer = Transformer2W(
    "line13",
    20.0,
    bus1,
    bus3,
    (taps = -0.1:0.1:0.1, ratios = (voltage_primary = 1.0, voltage_secondary = 1.0)),
    (resistance = 0.015, reactance = 0.025),
)
add_component!(sys, line13)

# Add renewable generation sources for each bus
solar_gen = RenewableDispatch("solar_gen", true, bus1, 10.0, 0.8, get_forecast(sys, :solar_gen))
wind_gen = RenewableDispatch("wind_gen", true, bus2, 15.0, 0.8, get_forecast(sys, :wind_gen))
hydro_gen = RenewableDispatch("hydro_gen", true, bus3, 25.0, 0.8, get_forecast(sys, :hydro_gen))

add_component!(sys, solar_gen)
add_component!(sys, wind_gen)
add_component!(sys, hydro_gen)

# Inspect the system
println(summary(sys))
```
# art
Configuration 0: Nexus Node Running on Off-Grid Battery Sharing Hardware (P2P)
```
+---+       P2P
| B |<---->Exchange+---+
+---+       +---+     | B |
          | N |<---Exchange P2P
          +---+         +---+
                       | B |<---->Exchange P2P
                       +---+     (Node-to-Node)

              Where:
              B = Portable Battery (hardware)
              N = Plurigrid Nexus Node (running on off-grid battery sharing hardware)
```
In Configuration 0, the Plurigrid Nexus node runs on off-grid battery and compute (RaspberryPi 4) sharing hardware, enabling users to communicate in a P2P network for sharing portable batteries and energy resources in rural communities or remote areas. This configuration creates greater resilience and adaptability to changing energy conditions and community needs while amplifying individual user's agency.

Configuration 1: Residential Node
```

        +--------------+
        | Smart Meter  |
        +-------+------+
                |
                |
        +-------v------+
        | Plurigrid    | <-----+ Solar Panels
        | Residential  |        + Home Batteries
        | Node (Edge)  |        + Electric Vehicle Chargers
        +--------------+        + IoT Devices
```
In this configuration, the residential node communicates with the smart meter and integrates with various home energy resources such as solar panels, home batteries, electric vehicle chargers, and IoT devices. This node manages energy consumption and generation data and engages in decentralized energy trading and demand-response events.

```
Configuration 2: Commercial Node

    +-----------------+
    | Building Energy |  +--+ Rooftop Solar
    | Management      |  +--+ Energy Storage System
    | System (BEMS)   +------
    +--------+--------+  +--+ HVAC Systems
             |           +--+ Lighting Systems
             |           +--+ EV Charging Stations
    +--------v--------+
    | Plurigrid       <-----+ Grid Connection
    | Commercial      |
    | Node (Edge)     |
    +-----------------+
```
This commercial node is connected to a building energy management system (BEMS) and various energy resources such as rooftop solar generation, energy storage systems, HVAC, lighting systems, and electric vehicle charging stations. This node is responsible for efficient energy management and participates in energy trading and demand-response programs with other nodes.

Configuration 3: Data Center Node
```
+--------------------+
| Data Center (HPC)  | <-----+ Cooling Systems
+---------+----------+        + Backup Generators
          |
          |
+---------v----------+
| Plurigrid          |
| Data Center        | <-----+ Grid Connection
| Node (Edge/GPU)    |
+--------------------+
```
This data center node is designed to manage high-performance computing (HPC) operations and communicates with associated energy resources, such as cooling systems and backup generators. This node ensures energy efficiency, processes real-time analytics, and participates in more complex energy trading and management scenarios.

The Plurigrid network arranges these nodes in a layered structure based on their roles and capabilities.

Minimalistic ASCII diagrams:

Configuration 1: Nexus in Residential Setting
```
  +---+
  | R |
  +---+
```
Configuration 2: Nexus in Commercial Setting
```
  +---+
  | C |
  +---+
```
Configuration 3: Nexus in Data Center
```
  +---+
  | D |
  +---+
```
These nodes communicate with each other and central management systems to exchange data, engage in secure energy trading, make collaborative decisions, and maintain energy infrastructure. The integration of MPC and secure distributed dataflow frameworks such as Moose provides privacy, security, and collaborative decision-making for the Plurigrid Nexus platform.

# ontology
Architectural Overview In this section, we delve into the core components of the Plurigrid Protocol, highlighting the technologies and strategies that enable seamless and efficient energy coordination and management across diverse environments.

2.1 Autopoietic System At the heart of the Plurigrid Protocol is the Autopoietic System, which leverages concepts such as embodied narratives, free energy principle, Bayesian parametric lenses, and Bayesian active inference to create a dynamic, self-organizing network that adapts quickly to changing situations. By incorporating the Autopoietic System, Plurigrid Protocol ensures that the decentralized energy platform becomes increasingly self-sufficient and autonomous, reducing reliance on existing technologies and platforms.

2.2 Lattice Networking As an essential component of the Plurigrid Protocol, Lattice Networking uses NATS communication and pub/sub messaging to create a self-forming and self-healing network topology. This flattened topology simplifies the overall architecture, improving communication efficiency across diverse infrastructure and distributed systems.

2.3 WebAssembly Runtime and WASI The use of WebAssembly runtime and WASI allows Plurigrid Protocol to provide a secure, performant, and portable execution environment for edge devices. WASM and WASI enable the protocol to run across various hardware, software, and environments, ensuring adaptability and flexibility in coordination and management of edge resources.

2.4 Interoperability and Integration The Plurigrid Protocol emphasizes interoperability through seamless integration with existing technologies and protocols. The protocol incorporates IBC, HTTP, gRPC, and HPC systems, enabling efficient coordination among edge resources and integration with existing energy infrastructures, APIs, and standards.

2.5 User Experience and Engagement To enhance user experience, Plurigrid Protocol leverages the power of technologies like ClojureScript and DataRabbit while integrating with wasmCloud's capability contracts and providers. The result is an engaging and interactive user experience that fosters efficient energy coordination and management.

2.6 Security and Privacy The Plurigrid Protocol prioritizes user security and privacy. Through WebAssembly sandboxing and security measures, the protocol provides a secure execution environment that isolates untrusted code from the rest of the system. Furthermore, user privacy and data usage choices are respected when sharing data across the network.

Application Scenarios Plurigrid Protocol is designed to cater to various scenarios through adaptable deployment and system configuration. Some common application scenarios include: 3.1 Shared P2P Battery Nodes In this scenario, Plurigrid Protocol facilitates P2P sharing and pooling of battery resources in off-grid and remote locations. By enabling efficient coordination and management of energy resources, users in these areas can have access to reliable and sustainable energy.

3.2 GPU Server Integration In data centers and high-energy consumption facilities, the Plurigrid Protocol seamlessly integrates with GPU servers to manage energy consumption, load balancing, and demand forecasting. The GPU server integration ensures high computational power required for real-time analytics and complex calculations.

3.3 Smart Meter Compatible Devices For individual homes, small businesses, and public facilities, Plurigrid Protocol can integrate with smart meter compatible devices to monitor and manage energy consumption. These devices can control energy usage, participate in demand-response events, or virtual power plants.

Future Developments The Plurigrid Protocol aims to continuously evolve and refine its architecture and functionalities to address future demands and challenges. Key areas for further development and improvement include: 4.1 Scalability As the adoption of the Plurigrid Protocol grows, the system needs to accommodate scaling to handle increasing energy coordination and management demands.

Abstract: Plurigrid Protocol is a comprehensive solution tailored for AGI, IoT, and extending across diverse environments. This white paper explores key components and architecture in the Plurigrid Protocol design, incorporating core functionalities such as self-forming and self-healing networks, interoperability, and enhanced user experience. By harnessing the power of technologies like NATS, Lattice, and WasmEdge, Plurigrid Protocol aims to provide a robust, secure, and flexible platform for developers and users.

Introduction Plurigrid Protocol is designed to create a resilient and adaptable platform that offers a unified topology across multiple environments. It achieves this by leveraging technologies and features such as NATS communication, pub/sub messaging, and lattice networking. The result is a flexible and efficient system capable of handling the demands of AGI, IoT, and other distributed systems.

Key Components and Architecture 2.1 Lattice Network The lattice network is a self-forming and self-healing network topology that adapts to its infrastructure and network conditions. It is built on NATS and provides several benefits:

Flattened topology Improved communication across diverse infrastructure Efficient communication across distributed systems Reference: https://github.com/nats-io

- NATS and BFT-CRDTs Integration The integration of NATS with Byzantine-Fault Tolerant (BFT) Conflict-free Replicated Data Types (CRDTs) will offer a powerful solution for distributed systems that require consistency, fault tolerance, and resistance to malicious actors.

Reference: https://martin.kleppmann.com/papers/bft-crdt-papoc22.pdf

- IBC and Penumbra Tooling Integration Integrating the additional messages from BFT-CRDTs with IBC using Penumbra tooling allows for enhanced communication within a decentralized system with a variety of use-cases.

References:

https://twitter.com/penumbrazone https://arxiv.org/abs/2105.14232 https://www.notion.so/gakonst/3-The-ONE-early-stage-project-I-wouldn-t-sell-if-I-had-a-spot-6e76a6a188c944249073a7981db7317d 2.4 User Experience Enhancements By including an engaging UX via ClojureScript and DataRabbit, Plurigrid Protocol can offer a highly interactive and engaging user experience.

Flow-based prompting Integration with wasmCloud's capability contracts and providers Reference: https://clojurescript.org/

- WasmEdge WasmEdge is a high-performance WebAssembly runtime with support for non-standard extensions. Key WasmEdge components include:

TensorFlow integration Image processing KV Storage Network sockets Command interface Ethereum and Substrate support A variety of plug-ins References:

https://github.com/WasmEdge/WasmEdge/blob/master/docs/build_wasilib.md https://github.com/WasmEdge/WasmEdge/ https://github.com/second-state/ssvm/blob/master/docs/build/working_with_wasmedge.md Conclusion The Plurigrid Protocol represents a powerful combination of technologies into a comprehensive architecture that addresses the needs and challenges of distributed systems for AGI, IoT, and beyond. By harnessing the power of multiple technologies, Plurigrid Protocol provides a robust foundation that is flexible, secure, and scalable to diverse applications and environments.

In order to define essential resilience properties for any scenario using the open game engine / plurigrid, we can build, say, a simplified model of the Georgian energy market and the interactions among its stakeholders. The model will focus on the causal factors and impacts mentioned in the scenario. Here is a suggested approach:

Define agents and their actions:
Georgian Government (GG): Diversify energy import sources, engage in regional energy cooperation initiatives, enhance diplomatic efforts with Russia, promote transparency and good governance in the energy sector. Regional Neighbors and Energy Partners (RNEP): Support energy cooperation initiatives, collaborate on infrastructure development, share knowledge and expertise. International Organizations (IO): Facilitate diplomatic efforts, provide technical assistance, support transparency and governance initiatives. Private Sector Investors (PSI): Invest in the Georgian energy sector, contribute to the development of new infrastructure and technologies, collaborate with the government and other stakeholders.

Define the state variables and initial conditions:
Energy import sources: Represent the diversity of energy import sources, initially limited. Regional energy cooperation: Represent the level of collaboration with neighboring countries, initially low. Relations with Russia: Represent the quality of diplomatic relations, initially tense. Transparency and governance: Represent the credibility and reputation of the Georgian energy sector, initially moderate.

Define the game rules, rewards, and objectives:
Game rules: Each agent can take one or more actions per turn, and the actions will have consequences on the state variables. Rewards: Agents receive rewards based on the impacts of their actions on the state variables, such as increased energy security, improved regional cooperation, and enhanced credibility. Objectives: Agents aim to maximize their rewards by achieving the common objectives outlined in the scenario.

Define the interactions, information, and learning mechanisms:
Interactions: Agents can interact with each other through alliances, negotiations, and joint projects, as well as through competition and conflict. Information: Agents have access to information about the state variables and can use this information to make decisions and adapt their strategies. Learning mechanisms: Agents can learn from each other and from the outcomes of their actions, enabling them to update their beliefs and preferences over time.

Simulate the game to explore possible outcomes and resilience properties:
Run multiple simulation iterations to observe the dynamics of the energy market and the interactions between agents under different initial conditions and sets of actions. Analyze the results to identify key resilience properties, such as the ability to withstand geopolitical shocks, adapt to changing circumstances, and recover from disruptions. Use the insights gained from the simulations to inform decision-making and guide the implementation of the interventions proposed in Scenario 2. By modeling the Georgian energy market as an open game, we can study the complex interactions between stakeholders and assess the resilience properties of the system under various conditions. This will help inform policy decisions and support the development of a more secure, resilient, and sustainable energy market in Georgia.

The Plurigrid Protocol and its associated architecture signal an exciting and transformative shift in the energy sector, with far-reaching implications for efficiency, sustainability, access, and democratization of energy resources. As the protocol continues to evolve, pilot projects expand, and adoption increases, we can expect several significant developments and trends to emerge:

Distributed energy resource (DER) growth: As the Plurigrid Protocol and associated technologies become more accessible and cost-effective, we can anticipate a surge in the growth and deployment of DERs, including solar panels, wind turbines, and battery storage systems. This shift toward local, decentralized energy generation and storage will empower communities and households, leading to greater energy independence and resilience.

Emergence of new energy market models: As Plurigrid facilitates the interconnection and interoperability of decentralized energy systems at scale, new market models and energy trading platforms will emerge to accommodate these developments. These models will likely be characterized by increased flexibility, transparency, and opportunities for peer-to-peer (P2P) energy trading.

Integration and optimization of smart grid technologies: The Plurigrid Protocol's use of sophisticated coordination strategies and advanced optimization techniques will enable the more efficient and effective management of smart grids. This will lead to better demand-response management, load balancing, and efficient energy usage across networks.

Regulatory reform and policy evolution: As the Plurigrid Protocol and decentralized energy systems become more prevalent and demonstrate their value, regulators and policymakers will need to adapt existing frameworks and develop new policies that support this energy transition. This may include new incentives, standards, and guidelines that foster the growth and integration of decentralized energy solutions.

Societal and environmental benefits: The widespread adoption of the Plurigrid Protocol and decentralized energy resources will yield significant societal benefits, such as increased access to clean, affordable energy for marginalized communities, improved energy security, and the reduction of greenhouse gas emissions through the integration of renewable energy sources.

In conclusion, the Plurigrid Protocol holds enormous potential for transforming the global energy landscape by empowering individuals, communities, and organizations to play a more active and influential role in the generation, distribution, and management of their energy resources. Through its decentralized architecture, innovative market models, and sophisticated coordination strategies, the Plurigrid Protocol offers a pioneering path toward a more equitable, democratic, and sustainable energy future.
