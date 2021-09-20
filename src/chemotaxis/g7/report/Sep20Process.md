# Project 2: Chemotaxis

## Group 7

### Current strategy (09/20/2021)

#### Controller

Controller chooses to put the chemical in the grid to guide agents from start to target, which means it must prioritize many agents and make each agent follow the shortest path if possible, so we can simplify the problem to 3 questions:

1. How to guide the agents to finish the goal as soon as possible and in the meantime, to save the chemicals as many as possible?
2. How to use three chemicals to guide the agents?
3. How to select one agent and where to put the chemicals?

After 2 meetings, we come up with a BFS search algorithm and a chemical application strategy.



To solve Q1:

We propose the BFS search to **find Shortest Path with the fewest turns**

Why we should choose the shortest path with fewest turns?

For example, if the grid is 5 * 4, the start and the target are the opposite corners of the grid, of course there are several shortest paths, but if we choose to follow the edge of the grid, the agent only needs 1 turn, which will cost 1 chemical to let it turn.



So the algorithm is the following: (maybe I will draw a graph to illustrate this later)

1. initialize the queue, each node contains four elements: currentPosition, the path so far, directionType, the number of turns so far, put the start point in the queue, initialize the visited grid, set the start to true
2.  if the queue is not empty, pop left the queue, get the node
3. compare current position to the target, if equals, compare the length of the path, choose the shortest one, if the length of path is equal, compare the turns, choose the fewest turns
4. if the current position is not target, add its neighbors which are not blocked and has never been visited
5. keep the process 2-4
6. then we get the result that contains the shortest path and the fewest number of turns



To solve Q2:

We use the Green to let the agent turn left, Red to let it right, and Blue to attract, and we don't take chemical diffusion now because it is too difficult to calculate and may lead the wrong way

- The Green or Red chemical will be put directly in the grid where the agent is now, so the agent knows directly that it should turn left or right.
- The Blue chemical will be put next to the agent, so if the agent senses that there is a Blue in the neighborMap, it will go to the Blue direction.



Why do we use three chemicals instead of just Blue?

Image there are many agents close to each other, if we use Blue, it will be blocked soon, because each wants to go to the same position where the Blue is, but if we use Green/Red in the grid of the agent, the chances to block each other are smaller.

For example, agent empty agent, if we want two agents to turn one direction, if we use Blue, they may meet each other, but if we use Green/Red, they both turn.



What situation will we use Blue?

- From the start position, because the agent is stopped, we use Blue to let it go in the right direction.
- If one agent is blocked by another agent, after the blocked agent is gone, we use Blue to move the agent.
- If the agents goes the opposite way, we want it back, we use Blue.



To solve Q3:

**We always choose the agent that needs to turn, we prioritize the closest agent.**

ApplyChemical Strategy is the following:

1. caculate the shortest path from each agent position to the target, by using the BFS algorithm we introduced above, in this way, we know the distance between the agent and the target, and each agent's expected next step, we compare the next step position and the current position, we will get expected DirectionType
2. we always store the previous locations, so if we compare the previous locations with current locations, we will know the previous DirectionType.
3. compare previous DirectionType with expected DirectionType of each agent, if it is not the same, it means the agent needs to turn, and we can know which direction it needs to turn, which is related to the chemical type
4. choose the agent that needs to turn with the shortest distance, apply the chemical



Bug fix:(still has 1 unfixed)

1. ~~the start/target position is from 1 not 0~~
2. ~~if current position is (x, y), the east means (x, y + 1) the south means(x + 1, y), which is different from Chinese culture~~
3. always waste one blue chemical when starting the agent



Remains to do in the future:

1. take chemical diffusion into consideration

---


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

---

### map_generator

The map_generator script can help us parse txt map generated from the website to the required format of our system.

To use the generator:

1. Draw a map on the website:https://campaignwiki.org/gridmapper.svg
2. down load it and save as txt format in a fold with the script file.
3. run the command 

```shell
python map_generate.py --size sizeOfMap --input nameOfOriginalFile --output nameOfMapGenerated
```

Attention Please, when you are drawing a map, use:

- b on the keyboard as start
- t on the keyboard as target
- p on the keyboard as blocks
- f on the keyboard as normal road



