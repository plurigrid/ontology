
**why is it important to have the agent running on the node?**

the model on the node is supposed to have a similar set of weights 
the model runs either on one node, or in a distributed settings on many nodes
the weights will be distributed when there is more compute — so it should be flexible 
— our protocol has to predict that point at which you scale up 
there’s only one node, but there are many possible configurations of the node
optimize and rebalance computation in this mesh computation layer 

**how will the nodes interact with the chain?** 

-   the chain is *emergent* 
-   all the nodes, at any time, can start their own chain for the purpose of a transaction, and then spin down and *dump* to the availability layer
-   for now though, we can work with an existing chain like neutron

**more questions**

- how does the microworld spec --> code or configurations work? how is this envisioned to work in relation to what's running on the node? 
- How does the NFT computation bidding work?
- How many actual unique microworlds do we want deployed?
- What are the considerations of Raspberry Pi vs microcontroller?
- Which language models will actually be able to run on the node w.r.t. resource constraints?
- If a microworld is a set of strategy profiles, what if you need to swap out the microworld that is running on the node? (it might help to walk through several different explicit microworld scenarios)
- how will formal assurances come into play? 
- How many communication standards will a node run?
- How will microworlds be formally verified?
- What software constitutes a microworld? is there more than just an LLM?
- How do the tokenomics work? where is $grid in all of this?
- What exactly does Barton mean when he says state machine or life cycle of the node?
- What are we building once the nodes nexus' are built?
- How do the nodes coordinate?
- How are the nodes updated?
- How does energy trading come into play?
- How do we make money off of all of this?
- “Nodes forming a mesh network” and “self-relaying” -- how will they interact with the consensus layer, and will they form their own local consensus, if so how does that work?
- How do nodes interact with the grid
- How do nodes generate revenue for Plurigrid (or anyone)
- What is the minimum functionality of an offline node
- How are actions taken offline settled once back online
- when does this actually interact with the grid, or transmission lines, or talk to actual hardware?
- how do "sensemaking" and "narratives" fit in to the decentralized energy story
- Smart meters smart devices
- 3 questions re: hardware:
  1. What is the unique firmware we are producing?
  2. Whose hardware will first adopt this?
  3. Why would they want to run the nexus?
- We’ve deviated from lampDAO architecture in previous weeks because of a) network latency concerns and b) wanting a maximally interoperable stack and c) the idea of creating this distributed compute mesh network
- Business questions:
  - Why would a hardware manufacturer implement our thing?
  - How does the CCA angle fit in to this?
  - Or, is it the best angle to be able to establish new microgrids in areas with no electricity? if so, how does that work since setting up transmission infrastructure is difficult? 
- Tell me more about how the microworld specification turns to code.
- "Grid designer UI"? what are the requirements for this?
- When you’re a Plurigrid user, what kinds of possible things would you be conveying to your agent besides your energy consumption preferences?
- Need clarity on the "node spins up a chain to do one transaction" scenario


explain further: 

```

barton ⊛: I already generated a really robust RL model, but really the minimal deployment is a completely different discussion
barton ⊛: because the code
barton ⊛: becomes .wasm file(s)
barton ⊛: that then get sent to a wasm runtime
barton ⊛: that the nodes run
barton ⊛: so as long as you can get me a battery and a raspberry pi with a wasm runtime, of which we can spin up 1000 right now
barton ⊛: [https://wasmedge.org/book/en/quick_start/use_docker.html](https://wasmedge.org/book/en/quick_start/use_docker.html) -- even if we only get 69 actual nodes with actual hardware, "serverless" hosts can do computation, running different regions of the world, until we convert them is acceptable also
barton ⊛: nexus is the same stack
barton ⊛: and virtual power plants we do not discriminate against :smile:
barton ⊛: the idea is to _imagine_ what 1000 worlds working for everyone on earth would be able to do
barton ⊛: If you grant that there are maybe < 200 organizations now who do digital twins at all, and only < 20 who do it well
barton ⊛: unless you count transactive market participants, such as DERs
barton ⊛: most of whom don't have enough resources to do good modeling but have surprisingly good ecosystems like [https://openmod-initiative.org/](https://openmod-initiative.org/)
barton ⊛: so if we were to give these good ecosystems our good models
barton ⊛: we would empower more quickly
barton ⊛: but then it's upon our _product_ to then proceed and generate these automatically and be easy to run etc
barton ⊛: the key is building a bunch of very carefully distilled language models
barton ⊛: and generally graph models
barton ⊛: large graph / geometry models made small but useful enough -- because we have a way of measuring :smile:

```