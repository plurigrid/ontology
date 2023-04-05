# ontology
INVITATION: EACH TIME YOU PLAY AND IT MADE YOUR DAY BETTER OR MADE YOU SMILE,
LEAVE THIS BETTER THAN YOU HAD FOUND IT, FOR WHEN WE ALL PLAY AGAIN. 🌳

# Welcome to Plurigrid Protocol! 🚀

This README will guide you through the process of setting up your development environment and getting started with contributing to the Plurigrid Protocol. Our goal is to ensure a smooth onboarding experience, so you can quickly become an active member of our project.

## Table of Contents
- [Getting Started](#getting-started)
- [Installation](#installation)
  - [CLI](#cli)
  - [Obsidian](#obsidian)
- [Plurigrid Playbook](#plurigrid-playbook)
- [Background Theory](#background-theory)
- [Contributing](#contributing)
- [Support and Community](#support-and-community)

## Getting Started

Before diving into Plurigrid Protocol, we recommend familiarizing yourself with the [background theory](#background-theory) and checking out our [playbook](#plurigrid-playbook) to have a better understanding of the project's principles and guidelines.

## Installation
### CLI
Set up your development environment and start contributing.

```bash
curl -L https://nixos.org/nix/install | sh
nix-env -iA just && just play
```

To debug the open game:
```
nix-shell
poetry shell
python scripts/http.py
```

If you want to exit the shell environment and return to your original shell, you can simply type `exit` or press `Ctrl-D`.

### Obsidian
First, follow the CLI steps and make sure `just play` correctly outputs `gm`.
Then `just obsidian`.

## Plurigrid Playbook
Play / Coplay: the Plurigrid Way :rocket:
### step 1: Begin Your Journey (gm -> gn)

Start your day with a "Good morning" message and share your goals. Close the loop before signing off by leaving a "Good night" message with a summary of your day.

### step 2: Generate Proposals & Collaborate

Leverage the OpenAgencies framework for brainstorming, collaborate using platforms operating on interoperable data formats, and any tool that complies with working on the data streams and in compliance with Digital Public Goods framework (for markdown, like Obsidian, GitHub, etc.) work; and most importantly contribute your improvements to the Plurigrid ontology via Pull Requests on an ongoing basis. The feedback loop at Plurigrid is continuous, and the ones rewarded are those playing next to the top.

### step 3: Iterative & Adaptive Development

Monitor and fine-tune your progress, adapting as needed. Embrace a human-in-the-loop approach, learning from other team members and the groundbreaking AI founder concept.

### step 4: Share Knowledge & Learn

Reflect on your achievements, challenges, and learnings each day. Share your daily summaries to contribute to the collective growth of the Plurigrid team.

### step 5: Close The Loop

Review your progress made during the session.
Update the action items based on the session results.
Plan upcoming tasks to maintain momentum.
Integrate any new frameworks or concepts that emerged during the session.
Update the Plurigrid ontology by creating a Pull Request with your changes, ensuring continuity, and improvement throughout your work at Plurigrid Inc.
By following the Play / Coplay framework, you'll efficiently collaborate and contribute to Plurigrid Inc. Fostering a robust, interconnected, and evolving system that can tackle the challenges of decentralization on a multi-planetary scale. Welcome aboard! :milky_way:

## Background Theory
In this concise review, we focus on the concept of autopoietic ergodicity in the context of Plurigrid systems, shedding light on its significance for broader developments.

### Autopoietic Ergodicity
Autopoietic ergodicity encompasses the ability of Plurigrid systems to self-organize, adapt, and evolve in diverse and ever-changing environments. This concept is grounded in two core principles: autopoiesis, referring to the self-maintenance and self-regulation of a system, and ergodicity, which deals with the equivalence between time and ensemble averages in an interoperable system.

In Plurigrid development, autopoietic ergodicity is crucial in capturing and understanding systems' dynamic interactions with their environments, allowing them to achieve long-term stability and maintain relevance as situations change over time. By ensuring that the learning and adaptation processes of Plurigrid systems align with the principles of autopoietic ergodicity, developers can create systems that continuously evolve and foster an environment in which many worlds can not only co-exist but thrive together.

### Open Games and Markov Category
#### Play / Generative Channel
A generative channel, also known as a generative model or stochastic channel, is a mathematical construct that models the process of generating data or outcomes according to some specified underlying probability distribution. It captures the dependencies and relationships between variables, such as input features and output labels in a data-driven system or between players' strategies and outcomes in a game theory setting.

In the context of a Markov category, a generative channel can be represented as a morphism between objects, where objects capture the structure of probability spaces, and morphisms represent stochastic processes or conditional probability distributions. The composition of morphisms in a Markov category embodies the concept of sequential stochastic processes, where the output of one channel serves as the input for the next.

Generative channels are used to model a wide range of systems in various domains, including machine learning, statistics, and game theory. By analyzing the properties of these channels, one can draw inferences about the underlying processes, predict future outcomes, or optimize the design of a system. In the context of game theory, generative channels can be used to model the dependencies between player strategies, game states, and payoffs, allowing for a deeper understanding of the dynamics of strategic interactions in a game.

#### Co-Play / Recognition Channel
A recognition channel, also referred to as a recognition model or inference model, is a mathematical construct used to model the process of inferring or estimating the underlying latent variables or parameters from observed data or outcomes. It captures the probabilistic relationship between the observed variables and the latent variables and serves as the inverse of a generative channel or generative model.

In the context of a Markov category, a recognition channel can be represented as a morphism between objects, where objects capture the structure of probability spaces, and morphisms represent stochastic processes or conditional probability distributions. The composition of morphisms in a Markov category embodies the concept of sequential stochastic processes, where the output of one channel serves as the input for the next.

Recognition channels play a significant role in various fields, including machine learning, statistics, and game theory. In machine learning, recognition channels are often used for variational inference and learning, where the goal is to approximate an intractable posterior distribution of latent variables given observations. In game theory, recognition channels can be employed to model the players' beliefs about other players' strategies based on observed actions, which can be useful in understanding and predicting the behavior of players in strategic interactions.

### Learning
Together with generative channels, recognition channels form an essential part of the learning and inference process. They enable a systematic translation and understanding of the relationships between observable data and hidden variables or parameters that govern the underlying processes in a system.
