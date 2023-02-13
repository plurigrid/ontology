## Proposed Gameplay 0

**Metagame:** to create a cybernetic loop involving humans,
agents, simulations, and physical configurations, to demonstrate
how intents may be aggregated into physical-reality outcomes.

**Goal of gameplayers:** the winning coalition will gain control of the lamp
first (while being subjected to **the constraints**, defined below)

### 0: **Participants form coalitions**

And they come together to decide what corpus of information to train their agent on.
    TODO: we should define a template for this in which each person submits their
values / the particular philosophies they ascribe to. If
we provide preconfigured options including some outlandish
choices, it might be more fun.  

### 1: **Game Configuration**

Game operators set up zulip channels for each coalition. Game operators run training script given curated training-inputs and set up the agents in each zulip channel. Game operators create one subDAO per agent. The agent is the only member of the subDAO, but it represents the aggregated interests of the coalition.
    - Note: if this can be automated, cool, but manual is fine.

### 2: **Initial gameplay**

Participants must cooperate amongst themselves to find a configuration state for the lamp that meets **the
constraints**. They need to interact with the agent to write a short manifesto which reflects the values of the coalition (present in the context). This manifesto gets translated into a lamp configuration.
(We also include, in each bot corpus, some game operator-defined rules on how to translate manifestos into lamp configurations. These remain opaque to the players.)

**What are the constraints for a 'valid' lamp configuration?**

The lamp, according to a run of the simulation,
must be in a *safe* configuration. The game operators have
artificially created a narrow configuration space
that is considered "safe." (Ie, a certain RGB range and a certain
brightness range, and whatever other lamp characteristics may be configured).

We should make it difficult enough to translate manifesto into a valid lamp configuration
that it requires some back and forth between participants and agent.

### 3: **Continued gameplay**

The game participants will interact with the agent to understand
whether or not the configuration they have generated is valid. This will be done
by having the agent query the simulation results, explaining what is invalid about the simulation,
and having them retry by re-generating the manifesto. It will be opaque to the gameplayers
how their manifesto is being mapped to a lamp configuration, but the agent can tell them what a
valid configuration looks like.

When the first coalition has finally generated a valid lamp configuration, its agent will post it on-chain,
at which point the lamp will be able to read it and configure it self correctly.  

**NOTES:**

- For this proposed gameplay, we actually don't have to use dao-contracts. We could just have a simple contract which just has one execute message to set a single state var with the lamp configuration. But we could also introduce more ways to try to game things if we used the dao-contracts.
