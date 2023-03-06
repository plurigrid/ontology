
**definitions**

-   distributed energy resources (DERs): small scale energy resource such as a solar panel, battery energy storage system, micro turbine 
-   microinverter: converts DC energy, like form a solar panel, into AC energy for consumption 

here are some current questions. they may be able to be answered by further reading or contemplation, but the purpose of asking questions is to assimilate information faster. there are also a lot of resources I could be consuming and I want to know which to focus my attention on..

**in my words, my understanding of the thesis:** 

-   more energy is becoming available in the form of distributed energy resources (small-scale energy generation), we are only able to rely on centralized plants to coordinate this allocation of this energy, and thus our resources are being allocated ineffectively. Plurigrid allows prosumers in both existing and new microgrids to transact directly with one another using incentives and coordination mechanisms to ensure more efficient allocation of energy resources without having to rely on centralized coordinators.

**questions**

-   regarding the simulation phase: 
-   stepping back, I understand why simulation is useful but what will be the overall goal of our simulation phase? What would you describe a “simulation MVP” as? 
-   so we have cadCAD, gridSPICE, PowerSystems.jl — can you explain how all of these pieces will fit together? 
-   The “mechanism design / games” components are also somewhat opaque to me despite seeming to be the meat of things. What are some coordination strategies which might be implemented and employed during simulation? What other incentive levers might there be other than price? What should I read further to have a good understanding of this? 
-   what will be the role of celestia, anoma, wasmedge, and dao-contracts? 
-   The concept paper mentions this: “The software layer includes a local autonomous feedback loop at the microinverter level.” What does this mean? 
- are the simulation tools themselves going to be used by others? 
- what will extending cadCAD allow us to do?
- what will be the function of gridSPICE? 
- need a drill down on the "mechanism design" / "open games" part -- what different strategies

will we want to simulate specifically?  

    - an example of a simulation would be great -- what kind of preferences a prosumer might select, and which 

    coordination strategies would be used

- what's the deal with this University of Denver partnership?  

- how will celestia and anoma network be involved? 

-   how will wasmedge fit in? 
-   The concept paper mentions this: “The software layer includes a local autonomous feedback loop at the microinverter level.” What does this mean? 
-   how does the mutual credit system work? (how you can sell energy to someone far away) 
-   is it 1 chain, 1 grid? if so why? 
-   what will dao-contracts be used for? 
-   so we have all these tools: cadCAD, gridSPICE, anoma, celestia, IBC and cosmos, NREL’s power systems library — how will they all fit together? 

-   what is a “simulation MVP”? 

**in my words, my understanding of the thesis:** 

-   since more energy is becoming available in the form of distributed energy resources (small-scale energy generation), and we are only able to rely on centralized plants to coordinate this energy, the our energy resources are being used ineffectively. Plurigrid allows prosumers to transact directly with one another using incentives and coordination mechanisms to ensure more efficient allocation of energy resources without having to rely on centralized coordinators, as well as allows for bootstrapping new microgrids. 

**(from the concept paper): The central technologies of Plurigrid Protocol address:**

1.  Interoperability of grids and resources through 

1.  IBC inter-blockchain communication between grids and sub-grids

3.  Security, cyberattack-resilient, private node communication, and data-sharing through

1.  Zero-knowledge computation (ZK)
2.  Multi-party computation (MPC)

5.  **P2P (peer-to-peer) transactive energy models**

1.  coordination mechanism implementing Anoma protocol that enables counterparty discovery to ensure decentralized decision-making in redirecting power.

7.  **Simulation tools** 

1.  **GENX models with added incentive modeling and NREL open-source models** 

1.  NREL has demonstrated its grid optimization work in partnership with Holy Cross, a utility in Aspen, CO.

3.  **GridSPICE**

1.  Plurigrid’s GridSPICE modeler can inform when to create a sovereign blockchain. Smart contracts, specifically commons contracts and Decentralized Autonomous Organization (DAO) contracts are used to bootstrap smaller grids and allow for legal public funding of grid resources, and therefore encourage fractal development of sustainable grids on campus and beyond.
2.  Over time, it can discover the value of coordination as well as evolve into sovereign blockspace across any number of chains held together by IBC, public or private, necessary to sustainably fulfill coordination needs

**TO READ NEXT:** 

-  superorgnanism paper  
-  cosmwasm VM
-   barton’s thoughts on testnet from signal thread 
-   wasmedge 
-   tokenSPICE
-   mutual credit article
-  rest of reading list


#crypto 
#economics 