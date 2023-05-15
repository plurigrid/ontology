# v0.4: Amelia
- obtain the arena from github main, ontology/arena.json which will follow the arena.json.schema
- in a new branch, OPEN A DRAFT PULL REQUEST IMMEDIATELY to add `log/yyyy-mm-dd-name-<focus>.md`
- navigate to plurigrid.game OR localhost "just play"
- you will see the Plurigrid UI
- add a new tree - the System prompt will already have the necessary primitives to help you along the way
- as instructed by the textbox, paste the arena.json from GitHub ontology repo and begin going through the tasks assigned to you (you can ask !filter: Name) to use your name only
- as you go through the day, push your arena state intoe your branch (for which you opened a draft PR)
- iterate: in a separate tree on significant tasks - your goal is to fit your entire gm-gn iteration into the same tree, which means staying within the context window
- compose: as your day is unfolding, others will be working as well - they will be pasting their arena into their branch under `log/yyyy-mm-dd-name-<focus>.md`
ask Plurigrid to reconcile your arenas after pasting them one by one in the same prompt
- merge the PR with the COMPOSED arena.json ONLY AFTER THE TEAM APPROVES
# Ontology

INVITATION: EACH TIME YOU PLAY AND IT MADE YOUR DAY BETTER OR MADE YOU SMILE,
LEAVE THIS BETTER THAN YOU HAD FOUND IT, FOR WHEN WE ALL PLAY AGAIN. 🌳

# Playbook 😏

### Step 1: Begin Your Journey (gm -> gn)

Start your loop at the beginning of the day with the agent -- send a "GM" and one of our preset prompts to iterate on an idea whose results you then compose, and share your goals for your work. This should be done in https://plurigrid.game **open game*** to facilitate bidirectional communication.

### Step 2: Iterative & Adaptive Development Loops (Journey, Iterate, Compose)

**Journey Loop (Your Own Sense):** Monitor and fine-tune your progress, adapting as needed. Embrace a human-in-the-loop approach, learning from other team members and the groundbreaking AI founder concept.

**Iterate Loops (Our Work Together):** Collaborate with teammates to contribute and continuously improve Plurigrid's shared components and resources, as well as learn from others' experiences and findings. Use the Iterate Loops for tasks that are unfinished or ongoing, and seek feedback and support from your peers.

**Compose Loops (WAGMI share with your Plurigrid):** As you complete tasks and make progress, integrate and combine your work with that of your teammates in the Compose stage. This fosters collective growth and supports the composition of our ontologies. Compose is for tasks that are essentially done, facilitating the integration of everyone's output.

Reflect on your achievements, challenges, and learnings each day. Share your daily summaries to contribute to the collective growth of the Plurigrid team. By embracing iterative development and adapting as needed, together, we grow stronger and more capable.

#### Step 3: Keep Looping & Collaborate

Leverage the [OpenAgency framework](https://www.alignmentforum.org/posts/5hApNw5f7uG8RXxGS/the-open-agency-model) for brainstorming in your work, collaborate using platforms operating on interoperable data formats, and using any tool that complies with working on the data streams and is in compliance with Digital Public Goods framework. As you progress, contribute your improvements to the Plurigrid ontology on an ongoing basis. The feedback loop at Plurigrid is continuous, and the ones rewarded are those playing next to the top.

#### Step 4: Close The Loop

At the end of the play/coplay session, review your progress made during the session.
Update the action items based on the session results.
Plan upcoming tasks to maintain momentum.
Integrate any new frameworks or concepts that emerged during the session.
Update the Plurigrid ontology with any artifacts or outputs of your work by using the Obsidian git plugin.
By following the Play / Coplay framework, you'll efficiently collaborate and contribute to Plurigrid Inc. Fostering a robust, interconnected, and evolving system that can tackle the challenges of decentralization on a multi-planetary scale. Welcome aboard! :milky_way:

# Introduction

Welcome to the Plurigrid Protocol, where our mission is to create a decentralized energy platform by harnessing the power of collective intelligence. In order to achieve this goal, it is vital that all players in the network follow the play-coplay process. This process not only functions as a blueprint for our collaborative efforts but also mirrors the autopoietic nature of the plurigrid protocol itself.

By engaging in this play-coplay process, we navigate the complex realms of generative channels (the creation of outcomes) and recognition channels (understanding the connections between these outcomes) through a combination of learning, adaptation, and feedback. This ensures that our network evolves in a gradual and embodied manner––all while maintaining a harmonious balance between individual and collective progress.

Drawing inspiration from the principles of autopoietic ergodicity, Markov categories, and game theory, the play-coplay process enables us to effectively coordinate our efforts and make strides towards a more efficient and resilient decentralized energy platform.

Embarking on this journey together, we are not only building a strong foundation for the Plurigrid Protocol but also embodying its core principles in the way we collaborate, learn, and grow. Join us in embracing the play-coplay process, and let's revolutionize the world of decentralized energy systems—one interaction at a time.

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


## Obsidian
To prepare your pluralistic interface in Obsidian, simply use the appropriate template under `.playback`.

Then run `just obsidian`.

See the obsidian installation instructions below for more details.

## Setting Up Obsidian and Plugins

1. **Set up Obsidian**
- Download and install Obsidian: https://obsidian.md/download
- Open Obsidian and open this ontology folder as a vault.

2. **Set up the Smart Connections Plugin**
- Go to Settings (gear icon) > "Community plugins" > "Turn off Restricted Mode" Then, "Browse community plugins".
- Search for "Smart Connections" and install the plugin.
- Activate the Smart Connections plugin in the "Installed plugins" tab.
- Configure the plugin to your preferences and start creating AI-powered note connections by following the plugin's documentation: https://github.com/mgmeyers/obsidian-smartconnections
- Make sure you set your Open AI API key for GPT-4 in the plugin settings.

3. **Set Up Obsidian Git Plugin**
- Install the Obsidian Git plugin following the steps for the previous section, but search for the "Obsidian Git" plugin.
- Plugin documentation can be found here: https://publish.obsidian.md/git-doc/01+Start+here
- Follow the installation and authentication steps from the documentation.
- **Configure Obsidian Git settings**: Go to `Settings` > `Plugins Options` > `Obsidian Git` to configure your settings. For example settings, see `.config/data.json` in this repository and copy it over to `.obsidian/plugins/obsidian-git/data.json`.

### Workflow with Obsidian Git and Smart Connections

- Before you begin, run the "Obsidian Git: Pull" command to make sure your workspace is up to date.
- When you start your journey, create a new branch that is topical to the journey. Use the "Obsidian Git: Create new branch" command.
- With the default settings, your work should automatically be pushed and merged every 60 minutes. Your repository will also be automatically pulled on the same cadence.
- As you contribute to ontology, you can use the "Smart Connections: Smart Chat conversation" command to ask an agent questions over your data. Use the ontology agent as a guide to help your personal loops.

# Background Theory

## Autopoietic Ergodicity: A Foundation for Embodied Gradualism
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
