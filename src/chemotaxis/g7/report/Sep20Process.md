# Project 2: Chemotaxis

## Group 7

### Current strategy (09/20/2021)

#### Controller




#### Agent

Each agent has their own local state consisting of the following 8 bits:
- bits 0-1: previous direction (00: NORTH, 01: EAST, 10: SOUTH, 11: WEST)
- bits 2-6: counter that keeps track of the #rounds the agent hasn't sensed chemicals with concentration 1 --> if counter is 0 then the agent should do a random step
- bit 7: might be used for the counter

Next move strategy (order indicates priority of each case):
- if there is red chemical in the current block then turn right
- if there is green chemical in the current block then turn left
- if there is blue chemical in one of the neighboring cells then move to that direction
- if there is no chemical continue moving to the same direction unless <br>
    1) the next cell is blocked, in which case the agent uses default behavior to circumvent blocks <br>
    2) the agent hasn't sensed any chemical in 31 rounds, in which case the agent should do random steps until they sense a chemical

Random step stategy - randomly chooses the direction the agent should go if he needs to do a random step:
- if the agent can move only to one direction then by default they will move to that direction
- if there are more directions then we exclude the opposite of the previous direciton so that the agent won't make a "circle" and then we use the random number to generate a random idex for the direction the agent should move to

Circumvent block startegy - indicates which direction the agent should go when they meed a blocked cell (order indicates priority of each case):
- if the right cell isn't blocked then the agent moves to the right cell
- if the left cell isn't blocked then the agent moves to the left cell
- if the rear cell isn't blocked then the agent moves to the rear cell
