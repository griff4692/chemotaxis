from glob import glob
import os

import argparse
import pandas as pd
import numpy as np


SIM_CMD = 'make compile && java -cp .:chemotaxis/org.json.jar chemotaxis.sim.Simulator'

# Fixed tournament configurations
AGENT_GOALS = [10, 100, 1000]
BUDGET_FRACS = [0.1, 0.5, 1.5, 10, 100]  # Fraction of agent_goal
MAP2SIZE = {
    'g8/10x10-trap.map': 10,
    'g7/50x50CenterBarricade.map': 50,
    'dummy/spiral.map': 50,
    'g10/staircase.map': 51,
    # final ones
    'g1/map1.map': 30,
    'g4/15x15Symmetric.map': 15,  # newly added
    'g3/50x50_g3_tournament.map': 50,
    'g5/AgentTest.map': 10,
    'g7/g7-1.map': 30,
    'g8/final.map': 50,
    'g9/ye.map': 40,
    'g10/g10_final.map': 20,
    'g11/20x20_Submit.map': 20,
}
SPAWN_FREQ = [1, 2, 5, 10, 20]
TEAMS = [f'g{i}' for i in list(range(1, 12)) if not i == 6]


def calc_turns(spawn_freq, agent_goal, grid_size, multiplier=5):
    return round(spawn_freq * agent_goal + (multiplier * grid_size ** 2))


def get_run_id(budget, spawn_freq, agent_goal, turns, map_path):
    map_clean = '_'.join(map_path.split('/')).replace('.map', '')
    return f'budget-{budget}_spawn-{spawn_freq}_goal-{agent_goal}_turns-{turns}_map-{map_clean}'


def form_run_args(team, budget, spawn_freq, agent_goal, turns, map_path, seed, log_dir, run_id=None):
    if run_id is None:
        run_id = get_run_id(budget, spawn_freq, agent_goal, turns, map_path)
    group_dir = os.path.join(log_dir, team)
    os.makedirs(group_dir, exist_ok=True)
    log_fn = os.path.join(log_dir, team, f'{run_id}.txt')
    arg_str = f'-p -t {team} -b {budget} -r {spawn_freq} -a {agent_goal} -u {turns} -m {map_path} -s {seed} -l {log_fn}'
    return arg_str


if __name__ == '__main__':
    parser = argparse.ArgumentParser('COMS 4444 Tournament script generator')
    parser.add_argument('--log_dir', default=os.path.expanduser('~/coms4444_chemotaxis_results'))
    parser.add_argument('--out_dir', default=os.path.expanduser('~/chemotaxis/src'))
    parser.add_argument('-print', default=False, action='store_true', help=
    'if you just want to print the commands and not save to disc.')
    parser.add_argument('--team', default=None, help='If none, generate for all.  Otherwise, your team.')
    parser.add_argument('-compile', default=False, action='store_true')
    parser.add_argument('-only_new', default=False, action='store_true')

    args = parser.parse_args()
    if not args.print:
        os.makedirs(args.out_dir, exist_ok=True)

    seed_cache = {}
    team_list = TEAMS if args.team is None else [args.team]
    for team in team_list:
        if args.compile:
            results_pattern = os.path.join(args.log_dir, team, '*.txt')
            fns = glob(results_pattern)
            results = []
            for fn in fns:
                row = {'fn': fn, 'team': team}
                with open(fn, 'r') as fd:
                    row.update(dict([tuple(x.strip().split(':')) for x in fd.readlines() if len(x.strip()) > 0]))
                results.append(row)
            if len(results) > 0:
                results = pd.DataFrame(results)
                out_fn = os.path.join(args.log_dir, team, 'aggregate_results.csv')
                print(f'Saving results from {len(results)} runs for team {team} to {out_fn}')
                results.to_csv(out_fn, index=False)
            continue

        team_cmds = []
        for map_path in MAP2SIZE.keys():
            for agent_goal in AGENT_GOALS:
                for spawn_freq in SPAWN_FREQ:
                    for budget_frac in BUDGET_FRACS:
                        turns = calc_turns(spawn_freq, agent_goal, MAP2SIZE.get(map_path))
                        budget = max(1, round(budget_frac * agent_goal))
                        run_id = get_run_id(budget, spawn_freq, agent_goal, turns, map_path)
                        team_out_fn = os.path.join(args.log_dir, team, f'{run_id}.txt')
                        if os.path.exists(team_out_fn) and args.only_new:
                            # print(f'Skipping {team_out_fn}')
                            continue
                        if run_id not in seed_cache:
                            seed_cache[run_id] = str(np.random.randint(0, 1e4))
                        run_args = form_run_args(
                            team, budget, spawn_freq, agent_goal, turns, map_path, seed_cache[run_id],
                            log_dir=args.log_dir, run_id=run_id
                        )
                        cmd = SIM_CMD + ' ' + run_args
                        team_cmds.append((cmd, turns,))

        # Shortest first
        team_cmds = list(map(lambda x: x[0], list(sorted(team_cmds, key=lambda x: x[1]))))

        if args.print:
            print(f'Generated {len(team_cmds)} scripts for team {team}...')
            print('\n'.join(team_cmds))
        else:
            out_fn = os.path.join(args.out_dir, f'{team}.sh')
            print(f'Generated {len(team_cmds)} scripts for team {team} and saving them to {out_fn}')
            print_statements = [f'echo {i}/{len(team_cmds)}' for i in range(len(team_cmds) + 1)]
            out_str = ''
            for cmd, ps in zip(team_cmds, print_statements):
                out_str += cmd
                out_str += '\n'
                out_str += ps
                out_str += '\n'
            with open(out_fn, 'w') as fd:
                fd.write(out_str.strip())
