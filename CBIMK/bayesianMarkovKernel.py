import torch 
import torch.nn as nn 

import seaborn as sns
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D

import numpy as np
from scipy.stats import gaussian_kde

class MarkovKernel:
    def __init__(self, transition_matrix):
        self.transition_matrix = transition_matrix 

    def apply(self, input_data):
        return torch.matmul(input_data, self.transition_matrix)

class BayesianInverter:
    def __init__(self, input_dim, output_dim):
        self.model = nn.Sequential(
            nn.Linear(input_dim, 128),
            nn.ReLU(),
            nn.Linear(128, output_dim),
            nn.Sigmoid()
        )
    def invert(self, input_data):
        return self.model(input_data)

def compose(markov_kernel, bayesian_inverter):
    class ComposedKernel:
        def __init__(self, markov_kernel, bayesian_inverter):
            self.markov_kernel = markov_kernel
            self.bayesian_inverter = bayesian_inverter

        def apply(self, input_data):
            intermediate_output = self.markov_kernel.apply(input_data)
            inverted_output = self.bayesian_inverter.invert(intermediate_output)
            return inverted_output

    composed_kernel = ComposedKernel(markov_kernel, bayesian_inverter)
    return composed_kernel


if __name__ == "__main__":
    transition_matrix = torch.tensor([[0.8, 0.2], [0.4, 0.6]], dtype = torch.float32)
    markov_kernel = MarkovKernel(transition_matrix)
    bayesian_inverter = BayesianInverter(2, 2)

    input_data = torch.tensor([[0.7, 0.3], [0.2, 0.8]], dtype = torch.float32)
    output = compose(markov_kernel, bayesian_inverter).apply(input_data)

    print("Input data: ")
    print(input_data)

    print("Composed Kernel Output: ")
    print(output)
