import random

class OptimizationEnvironment:
    def __init__(self, papers, reviewers):
        self.papers = papers
        self.reviewers = reviewers
        self.reviewer_paper_affinities = {
            'Reviewer1': ['Paper1', 'Paper2', 'Paper3'],
            'Reviewer2': ['Paper2', 'Paper4'],
            'Reviewer3': ['Paper1', 'Paper3', 'Paper5'],
            'Reviewer4': ['Paper2', 'Paper5']
        }
        self.state = self._get_state()

    def _get_state(self):
        state = []
        
        for paper in self.papers:
            paper_features = [int(paper in self.reviewer_paper_affinities[reviewer]) for reviewer in self.reviewers]
            state.extend(paper_features)
        return state

    def step(self, action):
        reward = self._evaluate_assignment(action)
        self.state = self._get_state()
        return self.state, reward

    def _evaluate_assignment(self, action):
        reviewer = self.reviewers[action // len(self.papers)]
        paper = self.papers[action % len(self.papers)]

        if paper in self.reviewer_paper_affinities[reviewer]:
            return 1  
        else:
            return -1  

    def reset(self):
        self.state = self._get_state()
        return self.state
