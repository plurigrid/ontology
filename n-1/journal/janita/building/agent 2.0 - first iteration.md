
-   replicate a local environment
-   every day has to be a state machine 
-   gm —> gn loop, everybody engages everyday
-   vector stores 

ConversationSymbolicMemory

each zulipUUID helps establish a particular context for a user’s session 

**what's the stupidest version of this?**

- well, that begs the question: what's the point of this? 
	- the point is to establish a loop which can hold each of our contexts. It can also output a work dependency graph from this. It will help us better synthesize information about A. what plurigrid is, B. what others are working on, and C. how all of these compose together. 
	- But, for the stupidest version: a "gm" command, where you input what you are working on, and a "gn" command, where you summarize what you worked on today. 
		- The agent should save this context in its memory and summarize it later 
		- And next: the agent develops a dependency graph based on what everyone has input. 
		- And next: the agent is able to save the context of what you work on using Chroma (needs investigation) and the superAgent has the context of what everyone is working on 

**current**

- chroma seems pretty easy to use over a document: https://github.com/hwchase17/chroma-langchain
	- but, what about saving a document to chroma? 
- text-to-sql: https://colab.research.google.com/drive/1DWjH0vRzaTUjdhZXb9brkwKZDe2XWnzD?usp=sharing

- composability of indices! https://gpt-index.readthedocs.io/en/latest/how_to/composability.html

- the thread in agency / agents has been most illuminating 

- if using `Chroma` for embedding storage backed by IPFS, can use it / Filecoin to store the actual serialized index of the DuckDB


- using reinforcement learning 