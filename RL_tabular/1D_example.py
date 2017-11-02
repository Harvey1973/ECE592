''' Q learning 1D example '''

import numpy as np 
import pandas as pd
import time

np.random.seed(2)   

N_states = 6   # length of the 1 dimensional world
ACTIONS = ['left','right']   #avaliable ACTIONS
EPSILON = 0.9   #greedy policy
ALPHA = 0.1     # learning rate
LAMBDA = 0.9    # dicount factor
MAX_EPISODES = 15   # maximum iteration 
FRESH_TIME = 0.3    # time needed to make a move for the robot 

def build_q_table(n_states, actions):
    table = pd.DataFrame(
        np.zeros((n_states, len(actions))),     # q_table initial values
        columns=actions,    # actions's name
    )
    # print(table)    # show table
    return table


def choose_action(state,q_table):
    state_actions = q_table.iloc[state,:]   # retrieve the avaliable actions in a given state
    if(np.random.uniform() > EPSILON) or (state_actions.all() == 0) :     # act non-greedy or if the table has just been initialized
        action_name = np.random.choice(ACTIONS) 
    else :   #act greedy 
        action_name = state_actions.argmax()
    return action_name


def get_env_feedback(S,A):
    if A == 'right' : #move right 
        if S == N_states -2 :   # if the current state is one position to the left of terminal state 
            S_ = 'terminal'
            R = 1 
        else :
            S_ = S + 1
            R = 0
    else :   # move left 
        R = 0
        if S == 0:
            S_ = S   # reach the wall ,stay put
        else :
            S_ = S - 1
    return S_,R       # return next state and reward 



def update_env (S, episode, step_counter):
    # This is how environment get updated
    env_list = ['-']*(N_states-1)+['T']     # the environment looks this '------T'
    if S == 'terminal' : 
        interaction = 'Episode %s: total_steps = %s' %(episode+1,step_counter)
        print('\r{}'.format(interaction))
        time.sleep(2)
        print('\r                        ')
    else :
        env_list[S] = 'o'
        interaction = ''.join(env_list)
        print('\r{}'.format(interaction))
        time.sleep(FRESH_TIME)

def rl() :

    print('hello')
    
    #main part of RL loop 
    q_table = build_q_table(N_states, ACTIONS)
    for episode in range(MAX_EPISODES):
        step_counter = 0
        S = 0   # initial state 
        is_terminated = False 
        update_env(S,episode,step_counter)
        while not is_terminated :
            A = choose_action(S,q_table)   # choose action from S using Q table
            S_, R = get_env_feedback(S,A)
            q_predict = q_table.ix[S,A]   # current estimate 
            if S_ !='terminal':
                q_target = R + LAMBDA* q_table.iloc[S_, :].max()  # next state total reward 
            else :
                q_target = R     # stays same  because there is no next state to be in 
                is_terminated = True
            #update q table 
            q_table.ix[S,A] +=  ALPHA * (q_target - q_predict)
            S = S_     # move to next state 
            update_env(S,episode,step_counter+1)
            step_counter += 1
    return q_table

q_table = rl()
print('\r\n Q-table: \n')

print(q_table)

