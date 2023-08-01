import torch 
import torch.nn as nn 
from torch import optim

import matplotlib.pyplot as plt 

class BayesianInverter:
    def __init__(self, input_dim, output_dim, hidden_layers = 2, hidden_units = 128, learning_rate = 0.001, dropout_rate = 0.2):
        self.model = nn.Sequential()
        for i in range(hidden_layers):
            self.model.add_module(f"linear_{i}", nn.Linear(input_dim if i == 0 else hidden_units, hidden_units))
            self.model.add_module(f"relu_{i}", nn.ReLU())
            self.model.add_module(f"dropout_{i}", nn.Dropout(p = dropout_rate))
        self.model.add_module("output", nn.Linear(hidden_units, output_dim))
        self.model.add_module("sigmoid", nn.Sigmoid())

        self.loss_function = nn.MSELoss()
        self.optimizer = optim.Adam(self.model.parameters(), lr = learning_rate)

    def invert(self, input_data, target_data, epochs = 1000, batch_size = 32, verbose = True):
        self.model.train()
        dataset = torch.utils.data.TensorDataset(input_data, target_data)
        dataloader = torch.utils.data.DataLoader(dataset, batch_size = batch_size, shuffle = True)

        losses = []
         
        for epoch in range(epochs):
            total_loss = 0.0
            for inputs, targets in dataloader:
                self.optimizer.zero_grad()
                outputs = self.model(inputs)
                loss = self.loss_function(outputs, targets)
                
                loss.backward()
                self.optimizer.step()

                total_loss += loss.item()
                losses.appennd(loss.item())

            if verbose and (epoch % 100 == 0):
                print(f"Epoch {epoch}, Loss: {total_loss / len(dataloader):.4f}")
            return losses

    def predict(self, input_data):
        self.model.eval()
        with torch.no_grad():
            return self.model(input_data)
    
    def visualize(losses):
        plt.plot(losses)

if __name__ == "__main__":
    input_data = torch.tensor([[0.7, 0.3], [0.2, 0.8]], dtype = torch.float32)
    target_data = torch.tensor([[0.3, 0.7], [0.8, 0.2]], dtype = torch.float32)

    bayesian_inverter = BayesianInverter(input_dim = 2, output_dim = 2, hidden_layers = 3, 
                                         hidden_units = 256, learning_rate = 0.001, dropout_rate = 0.3)
    losses = bayesian_inverter.invert(input_data, target_data)

    test_data = torch.tensor([[0.5, 0.5], [0.1, 0.9]], dtype = torch.float32)
    predicted_output = bayesian_inverter.predict(test_data)

    print("Input data:")
    print(test_data)

    print("Predicted output (approximated inverse transformation):")
    print(predicted_output)

    