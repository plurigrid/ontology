import torch
import torch.nn as nn
import torch.optim as optim

import numpy as np 

from optim_RL import *

class Policy(nn.Module):
    def __init__(self, input_size, output_size):
        super(Policy, self).__init__()
        self.fc = nn.Linear(input_size, output_size)
        self.softmax = nn.Softmax(dim=1)

    def forward(self, x):
        x = self.fc(x)
        x = self.softmax(x)
        return x

class Agent:
    def __init__(self, input_size, output_size, learning_rate = 0.01, gamma = 0.99):
        self.policy = Policy(input_size, output_size)
        self.optimizer = optim.Adam(self.policy.parameters(), lr = learning_rate)
        self.gamma = gamma

    def select_action(self, state):
        state = np.array(state)
        state = torch.from_numpy(state).float().unsqueeze(0)
        probabilities = self.policy(state)
        action_distribution = torch.distributions.Categorical(probabilities)
        action = action_distribution.sample()
        return action.item()

    def update_policy(self, rewards, log_probabilities):
        discounted_rewards = []
        cumulative_reward = 0
        
        for reward in reversed(rewards):
            cumulative_reward = reward + self.gamma * cumulative_reward
            discounted_rewards.insert(0, cumulative_reward)

        discounted_rewards = torch.tensor(discounted_rewards)
        discounted_rewards = (discounted_rewards - discounted_rewards.mean()) / discounted_rewards.std()

        loss = []
        for log_prob, reward in zip(log_probabilities, discounted_rewards):
            loss.append(-log_prob * reward)

        self.optimizer.zero_grad()
        loss = torch.cat(loss).sum()
        loss.backward()
        self.optimizer.step()

if __name__ == "__main__":
    papers = ['Paper1', 'Paper2', 'Paper3', 'Paper4', 'Paper5']
    reviewers = ['Reviewer1', 'Reviewer2', 'Reviewer3', 'Reviewer4']

    env = OptimizationEnvironment(papers, reviewers)
    agent = Agent(len(papers) * len(reviewers), len(papers) * len(reviewers))

    num_episodes = 1000

    for episode in range(num_episodes):
        state = env.reset()
        done = False
        rewards = []
        log_probabilities = []

        while not done:
            action = agent.select_action(state)
            log_probabilities.append(agent.policy(torch.tensor(state).float().unsqueeze(0))[0, action].log())
            state, reward = env.step(action)
            rewards.append(reward)

        agent.update_policy(rewards, log_probabilities)

    
