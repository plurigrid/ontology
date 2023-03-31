**my questions**

- what are we aiming to do here? there are a couple goals: 
	- use agents to help us operate at this company
	- use agents to help us generate microworld simulations 
- what is a useful first step? 
	- agents which can help us generate a work dependency graph that is formatted in JSON
		- each person talks to the agent, and it generates 
- implementation questions
	- how will user data be stored? 
	- 

**the perspective from human in the loop and AI alignment articles, and barton**

LLMs only have a small context window, and there needs to be a way to quickly prune the bad outputs and then retrain the model on the good outputs

**what i'd like to do right now**

- do a bit more reading of the shared materials and see if I can reach my own conclusion about why prompting is insufficient and LLMs are more bot-like  

**formal verification**

- you query the LLM to get a mathematical formulation that represents its output and context thus far
	- you should similarly be able to verify its output 

machine fetishism - paper 

open agency model lesswrong article

"formal specification"

a mathematical formulation which describes a summary of: 
	- the context that the agent has so far
	- the microworld output the agent has generated
	- thus, we can prune which outputs are bad, and use the remaining to retrain the model (?)

example of the previous agent's capabilities: 

https://jessezhang.org/llmdemo

barton's PR: https://github.com/hwchase17/langchain/pull/1300

**papers** / articles

[https://penrose.cs.cmu.edu/docs/ref](https://penrose.cs.cmu.edu/docs/ref)

augmented language models; [https://arxiv.org/abs/2302.07842](https://arxiv.org/abs/2302.07842)

recitation augmented language models: [https://arxiv.org/abs/2210.01296](https://arxiv.org/abs/2210.01296)

compositional exemplars: [https://arxiv.org/abs/2302.05698](https://arxiv.org/abs/2302.05698)

chain of thought reasoning: [https://arxiv.org/abs/2302.00923](https://arxiv.org/abs/2302.00923)

chatGPT general purpose: [https://arxiv.org/abs/2302.06476](https://arxiv.org/abs/2302.06476)

moral self-correction: [https://arxiv.org/abs/2302.07459](https://arxiv.org/abs/2302.07459)

https://finbarr.ca/llms-not-trained-enough/

human in the loop: https://www.alignmentforum.org/posts/bxt7uCiHam4QXrQAA/cyborgism

loom, generate in a tree structure: https://generative.ink/posts/loom-interface-to-the-multiverse/

fully decentralized policies for multi-agent systems: https://arxiv.org/abs/1707.06334v2

**open questions**

- as always, what's something simple we can start with that we can iterate on? 
	- the JSON schema is promising 

![[cyborgs.png]]