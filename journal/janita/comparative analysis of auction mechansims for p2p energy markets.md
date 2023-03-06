- currently things like net metering (subtracting from utility bill for solar generation) and feed-in tariffs (financial incentives) are basically subsidized by the government in order to incentivize green energy adoption, but as adoption increases, these programs are being phased out 
- other research has not assumed prosumers as "price-makers", ie bid-placers
- discriminatory and uniform k-DA are the auction mechanisms this paper selects
- 4 characteristics of an ideal auction: 
	- IR: individual rationality
	- BB: budget balance
	- IC: incentive compatibility
	- EE: economic efficiency 
- buyer and seller bidding strategies can depend on the previous market clearing price, and for the buyer, the cost of buying electricity per hour from the utility company, and for a seller, the cost to generate an additional kwH of energy over the lifetime of their system (takes into account installation cost, etc) 
- the best-offer approach does not take into account market supply and demand, just focuses on optimizing price 
- the market-power approach takes into account historical data of PV generation and total electrical demand of the microgrid 
- key: ** participating in such an auction will have to rely on a software agent that calculates payoffs and chooses the best bidding strategy based on available information**
- the description they gave of how they set up the simulation is very useful 
- bidding with best-offer game-theoretic strategy (using the payoff matrix)  proved to yield the most ideal results in their simulation 



#papers 
#plurigrid 
#economics 