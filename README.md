**Features**
Initialize a new repository (init)
Stage files for commits (add)
Unstage files (unstage)
Create commits (commit)
View commit history (log)
Create and switch branches (branch, checkout)
Merge branches (merge)
View diffs between files or branches (diff)
Clone a repository (clone)
ignore files 
help (help)


 Using Docker
If you want to run the application using Docker, follow these steps:

Build the Docker Image


**docker build -t mygit-app:v1 .
**Run the Docker Container


**docker run -it mygit-app:v1
**Run Commands

To run commands inside the container, use:


**docker run -it mygit-app:v1 java MyGit <command> [options]
**Example: Initialize a new repository:


docker run -it mygit-app:v1 java MyGit init

for help or commands insrtuctions run 
**docker run -it mygit-app:v1 java MyGit help
**



