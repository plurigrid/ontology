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

def visualize(input_data, output_data):
    fig = plt.figure(figsize = (16, 8))

    for i in range(input_data.shape[1]):
        ax = fig.add_subplot(2, 3, i + 1, projection = '3d')
        x = input_data[:, 0].numpy()
        y = input_data[:, 1].numpy()
        z = input_data[:, i].numpy()

        x_grid, y_grid = np.meshgrid(np.linspace(min(x), max(x), 100),
                                     np.linspace(min(y), max(y), 100))
        kde = gaussian_kde([x, y], weights = z)
        z_grid = kde.evaluate([x_grid.ravel(), y_grid.ravel()]).reshape(x_grid.shape)

        ax.plot_surface(x_grid, y_grid, z_grid, cmap = 'Blues', alpha = 0.8)
        ax.set_xlabel("Input Dim 1")
        ax.set_ylabel("Input Dim 2")
        ax.set_zlabel(f"Input Dim {i + 1}")

    for i in range(output.shape[1]):
        ax = fig.add_subplot(2, 3, i + 4, projection = '3d')
        x = input_data[:, 0].numpy()
        y = input_data[:, 1].numpy()
        z = output[:, i].detach().numpy()

        x_grid, y_grid = np.meshgrid(np.linspace(min(x), max(x), 100),
                                     np.linspace(min(y), max(y), 100))
        kde = gaussian_kde([x, y], weights = z)
        z_grid = kde.evaluate([x_grid.ravel(), y_grid.ravel()]).reshape(x_grid.shape)

        ax.plot_surface(x_grid, y_grid, z_grid, cmap = 'Greens', alpha = 0.8)
        ax.set_xlabel("Input Dim 1")
        ax.set_ylabel("Input Dim 2")
        ax.set_zlabel(f"Output Dim {i + 1}")

    plt.tight_layout()
    plt.show()


if __name__ == "__main__":
    transition_matrix = torch.tensor([[0.8, 0.2], [0.4, 0.6], [0.5, 0.3]], dtype = torch.float32)
    markov_kernel = MarkovKernel(transition_matrix)
    bayesian_inverter = BayesianInverter(2, 3)

    input_data = torch.tensor([[0.7, 0.3, 0.5], [0.2, 0.8, 0.9], [0.7, 0.6, 0.1]], dtype = torch.float32)
    output = compose(markov_kernel, bayesian_inverter).apply(input_data)

    print("Input data: ")
    print(input_data)

    print("Composed Kernel Output: ")
    print(output)

    visualize(input_data, output)
