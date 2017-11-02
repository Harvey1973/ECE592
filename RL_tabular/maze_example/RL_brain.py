import numpy as np
import pandas as pd 



class QlearningTable :
    def __init__ (self, actions, learning_rate = 0.01, discount_factor = 0.9, e_greedy = 0.9):
        self.actions = actions
        self.lr = learning_rate
        self.gamma = discount_factor
        self.epsilon = e_greedy
        self.q_table = pd.DataFrame(columns = self.actions)   # empty data frame , add new states in the table as we go
    
    def choose_action(self, state):
        self.check_state_exist(state)
        #action selection 
        if np.random.uniform()< self.epsilon :
            #choose the best action 
            state_action = self.q_table.ix[state,:]   # retrieve all avaliable actions
            state_action = state_action.reindex(np.random.permutation(state_action.index))  # in case entries on the same row happen to be same, randomized index to break balance
            action = state_action.argmax()
        else :
            action = np.random.choice(self.actions)
        return action 


    def learn(self,state, action, reward, new_state):
        self.check_state_exist(new_state)
        q_predict = self.q_table.ix[state,action]
        if new_state != 'terminal':
            q_target = reward + self.gamma*self.q_table.ix[new_state,:].max()
        else: 
            q_target = reward
        self.q_table.ix[state,action] += self.lr*(q_target-q_predict)    # perform update function 

    
    def check_state_exist(self,state):
        if state not in self.q_table.index :
            self.q_table = self.q_table.append(
                pd.Series(
                    [0]*len(self.actions),      # add a row of zeros 
                    index = self.q_table.columns,
                    name = state,
                )
            )