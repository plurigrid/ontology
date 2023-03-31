

  Alright, so, I'm trying to synthesize the idea of Clarity for Myself, and it's like
  every time I get any answer from that, it comes from like the extremely, extremely high level of vision, which I think it's important to understand, to digest, to clarify for myself, and even to have my own idea, because I think he's coming into it with a lot of, you know,
  his own set of rich biases and prides about what's good for the world in regards to
  progress and agency. 
  
  But I think at the very end of the day, there has to be like a simple
  problem that we're trying to solve, and I think the simple problem could be:  we want to decarbonize the grid. 
  
  So, what does decarbonizing the grid mean? It means relying less on fossil
  fuel and more on renewable energy sources. Where could these renewable energy sources come from? They could come from residential solar panels. So, I think what happens is that people generate energy using their solar panels, and the government currently subsidizes them, like when they're selling the energy back into a grid, their energy doesn't go anywhere, the government subsidizes them for not producing that much energy, and I think in order to
  do that, or I think the reason why the energy doesn't go back to the grid is that it might put things out of equilibrium, but there is something inefficient about the way that solar energy is used. And so, if our goal is to increase renewable energy penetration, because they're increasing the use of the solar panels, we would allow people who have solar panels to actually sell their energy back into the grid, and for that energy actually to go back
  into their local grid, and for the equilibrium to be in place. 
  
  So, yeah, as I'm talking through this, I'm realizing, the only thing I need to clarify are, A, what currently happens
  when people sell their energy back into the grid, and what is ineffective or inefficient
  about it? And two, how could our solution help? So, they say our solution is leveraging
  more data. I think that's where it all starts. It's like, all of these devices, or at least
  like a household with like a DER or devices that are consuming electricity, will be emitting
  data about their usage. And if we can use this data to better predict consumption patterns,
  we can actually direct the flow of energy. We can basically, we can control when their
  devices use energy. Like, when their EV gets charged, for example. Like, I don't know,
  what are plants that they could work for? Like, obviously a fridge always comes with
  electricity. Your phone is always hooked up to a charger. But your car, that's a big
  thing. I mean, it could actually just give you inputs on when you should use your devices,
  run the dishwasher, run the laundry, based on better data. But as far as auto-charging,
  I think other than cars, I can't think of any appliances that this would work for.
  So like, when I originally thought about this, I was thinking of it as a research allocation
  problem. Like, we have a bunch of energy somewhere that's getting wasted or not effectively
  utilized, especially for renewable energy sources. But I think because of the opacity of my understanding
  of how the power grid works, like, I don't, like, I don't know what it means to run a
  research allocation problem for a power grid. Like, what is a research allocation problem?
  There is like a certain amount of energy in a grid. There are generators and there are
  consumers of energy. And we need to basically make it such that we are not at any time consuming
  more energy than exists in the grid or generating more energy than exists in the grid. You know,
  I think it would be really useful for me to, like, watch some videos about, like, how power
  grids work. Like, what are all of the components of power grids? And, like, when do they get
  turned on and off? So I think at the very minimum, like, you have these central power
  plants which are trying to, like, predict capacity, predict demand, and generate energy
  accordingly. And so we want to try to be better than that by using richer data to make better
  predictions. But, yeah, I'm just, like, I'm trying to, like, yeah, I think I'm, so, like,
  why did all of these researchers end up at transactive energy, right? Like, why, why did
  this, like, why is this kind of the thing that the first one ended up? Like, I understand
  that, like, given you want to do transactive energy, like, why you would use a block chain,
  but why did transactive energy in the first place? I mean, I think the original context
  for all of this is actually, you know, people with solar panels. Can they sell their energy
  to their neighbors and get, like, make more money out of it and basically also inject
  more renewable energy into the grid? So that's a very clear scenario that I understand, right?
  Use case one, solar energy from, you know, local neighborhood is, like, sold back to
  neighbors by using their local transmitters. And it is, it is done by, like, smart microinverters.
  So microinverters will have software that can communicate to, like, you know, adjacent
  households, which isn't currently possible. So that's better than any distance mission.
  That is a very clear way to better utilize solar energy.
  So now we come to this game theory stuff. So my understanding of it has increased, but,
  like, it's still pretty fuzzy. So, like, in these papers, we have, like, you know, agents
  that are, that have their own local utility function. And this is a software agent of,
  like, let's say it's, like, I represent the household. So what is this utility function
  trying to maximize? I think it's trying to, or I guess it's trying to minimize the cost of
  consuming one kilowatt hour of energy. And there are, like, you know, multiple different
  energy sources, you know, different types of energy that are being generated. And basically,
  like, the goal of the game is to come to the best solution for all of the players.
  All of the players. So, like, one example of a game, a competitive game, is an auction.
  An auction is where you have, like, there are many different types of auctions, but you have
  some sellers and you have some bidders. And let's say in this case,
  you have, like, let's go back to our solar panels example, you have multiple sellers
  of solar energy in your neighborhood, and you have multiple bidders. And all of them
  can indicate the max price at which they're willing to buy a kilowatt hour of energy.
  And you can basically run this double auction algorithm to match sellers and bidders and come
  up with theoretically the most fair allocation of energy for that neighborhood.
  So, I think there are, like, many, many, like, facets of this plurigrid protocol.
  One is, like, really the peer-to-peer energy trading, right? Energy selling. Small-scale
  prosumers in the plurigrid selling energy to their neighbors. And then you have, like,
  coalition formation. So, you can have, like, an aggregate group of energy consumers who can,
  like, form a spot market and buy and sell energy from, like, large-scale power plants, similar to
  how community choice aggregators work. And, you know, in the spot market, you can basically buy
  a large amount of electricity for a lower rate. And so, basically, with our better data and our
  better modeling, we'll be able to better represent what energy the people in the grid want. So,
  that's, like, layer two. Layer three, I see as, like, the pure financial aspect, which is, like,
  energy trading. So, it's, like, the energy that is, like, you know. Like, what do energy
  futures mean? Like, I think they're buying. I actually don't really know. What do you own in
  your own energy future? Like, do you own a prediction of, like, a trade that's going to be
  made at a future date? Yeah, that's something I also don't fully understand. So, yeah. Yeah,
^  we need to put a hand on that one. So, but there is this aspect of, like, you're a trader at a
  distance who's interacting with the protocol and being able to buy and sell assets related to
  energy. Whether that's energy futures or energy derivatives or energy asset certificate or whatever
  Jasmine Energy is doing. So, that is layer three. And what other areas are there?
  I'm going to consult me, that's...
  Okay.
  So, I guess, like, yeah. There's also, like, this whole other
  phenomena of, like, abstraction, which is, like, the mathematical, like, formulation of all of
  this, which is basically compositional open games, which is, like, the sort of more intuitive framing
  of open games, such that, like, local equilibria, games with local equilibria can be composed into
  larger games with a global equilibria. Now, I don't really understand any of the semantics of this
  because it uses a lot of category theory and it kind of goes over my head. And apparently it's still,
  like, an idea that's in development and is not necessarily ready to, like, be taken to an
  application yet, but it's, I suppose, a good way of framing the problem. It's like,
  you have, like, you know, these agents playing games and reaching local equilibria
  and, like, there needs to be a way to compose these into a larger game that is sizeable.
  So, yeah, I mean, it's kind of a lot given that, like, I don't really know much about game theory,
  I don't know anything about power grids, and, you know, I'm doing my best to, like, learn
  learn and, like, digest this idea for myself.
  So, but if I were to formulate to myself what the, like, most immediate target might be,
  it would be decarbonizing the grid, like, starting with the local neighborhood. How can
  a small neighborhood where people are connected via transmission lines, how can they start trading
  energy with one another? And furthermore, if you don't generate energy, is it possible to
  divert that energy to someone else? So, I think it helps to just start with a neighborhood
  and imagine, like, what an agent would look like, what a seller would, you know, what a
  consumer would look like, what a consumer would look like, what their utility functions would be,
  and go from there. Yeah, yeah, I think this is helpful.

**QUESTIONS AND OUTCOMES**

outcome: the first simulation is a "neighborhood" - a microgrid with a bunch of sellers and buyers (or even more simply, one seller and multiple buyers) -- bidding on one unit of solar energy 
	- include the following devices: a thermostat, a solar panel, an EV charger, and a battery
	- the outcome should be directing the thermostat and EV charger to use energy, and when the battery should store energy 

**DER definition**

- I got a richer definition of "DER" from the podcast -- it is any device in the distribution grid which can either generate or store energy and respond to a signal 

**Microgrid definition**

- a small scale grid (like for a campus, neighborhood, or industrial park) that is capable from operating independently

**What problem is transactive energy trying to solve?**

- It is trying to help "decarbonize" the grid, ie reduce fossil fuel usage, by increasing the number of renewable energy sources in the grid. 
- How can it do this? 
	- smarter demand response: with more information and more control about renewable energy generation times, we can direct demand to occur during peak renewable generation times
	- optimizing energy storage: the system can make use of energy storage systems such as batteries, to store renewable energy generated during periods of low demand, and release that energy during periods of high demand
	- increases investment opportunity for renewables because of market incentives 

**How exactly does demand response work today? Do owners of solar panels actually sell energy back to the grid?**

- With current demand response, customers are incentivized to reduce usage during peak times by being paid to do it. With transactive energy, their energy could actually be "sold" back to the grid. I got hung up on "where the electrons go." My answer right now is that they go back to the local grid. This is as opposed to demand response today, where customers are just incentivized to use less. 
- In net metering, however, currently solar energy is sold back to the grid. However, it is sold at a fixed price. With transactive energy, you could have a dynamic market and help solar panel owners make more. 
- Note: I'm still not sure how you might be able to trade energy with someone who is not on your local grid. I will ponder that more later. 

**what is the role of EVs in transactive energy?**

- they can alter charging time as a demand response. 
- they can store renewable energy for use during peak times. 
- they can sell energy back to the grid (vehicle-to-grid)

**another insight**

- the power grid is actually one really good example of a coordination failure -- because information is imperfect, disparate agents arrive at outcomes which are worse for everybody 

**questions for later**

- when you buy an energy future, what do you actually own? 
- what does the financial / energy trading layer look like? how might someone who isn't in the grid participate in the trading of it? 


