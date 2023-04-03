from github import Github

# Replace "your_token" with your personal access token from GitHub
g = Github("your_token")

# plurigrid: grid mujoco for your agents
org = g.get_organization("plurigrid")

# Get the existing project by its ID (3 in the given URL)
project = org.get_project(3)

# Get the columns for the project
columns = [_ for _ in project.get_columns()]

# Get the columns for project: Available, In Progress, Done
upcoming_col = next(col for col in columns if col.name == "Available")
next_col = next(col for col in columns if col.name == "In Progress")
done_col = next(col for col in columns if col.name == "Done")

# Task lists
tasks_done = [
    "Release Laura v0 Alpha",
    "Develop Plurigrid Protocol",
    "Plan device architecture for first hardware node",
    "Include Scenario 2 example for the Plurigrid presentation",
    "Discuss partnership types for Plurigrid Inc.",
    "Share investor-focused presentation titles and content",
    "Identify top priorities for Series A",
    "Review goals for realism and achievability",
]

tasks_next = [
    "Prioritize tasks based on available resources, potential impact, and dependencies",
    "Establish short-term objectives within each priority area to build momentum",
    "Draft a flexible timeline and roadmap that allows for adjustments as needed",
    "Organize a meeting with the slide artisan Pim",
    "Discuss the Plurigrid Protocol presentation outline, focusing on investor relevance",
    "Develop slides for the investor-focused presentation, including configuration slides",
]

tasks_upcoming = [
    "Rehearse the Plurigrid Protocol presentation for investors",
    "Present the Plurigrid Protocol at investor meetings/events",
    "Develop a comprehensive prototype or demo of the Plurigrid product",
    "Research and engage with potential partners for various partnership types",
    "Support all major and minor languages within the company through multilingual conversational agents",
    "Develop 1000+ microworlds and a grid designer UI",
    "Deploy the Protocol in 1000+ instances",
    "Create an app (mobile/desktop) for user interaction",
    "Implement privacy measures for DER/prosumer coalitions",
    "Develop and prototype hardware dev kit designs",
    "Establish the minimum viable base of operation in global outposts",
    "Secure a source of recurring revenue",
    "Assemble a set of board candidates for future growth",
    "Promote the adoption of conversational agents internally, leveraging low-cost and edge-operated capability",
]

# Populate the columns with the tasks
for task in tasks_done:
    issue = repo.create_issue(title=task)
    issue_card = done_col.create_card(content_id=issue.id, content_type="Issue")

for task in tasks_next:
    issue = repo.create_issue(title=task)
    issue_card = next_col.create_card(content_id=issue.id, content_type="Issue")

for task in tasks_upcoming:
    issue = repo.create_issue(title=task)
    issue_card = upcoming_col.create_card(content_id=issue.id, content_type="Issue")
