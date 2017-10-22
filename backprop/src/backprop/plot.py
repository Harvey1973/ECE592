import numpy as np
import matplotlib.pyplot as plt

with open('cost.txt') as f:
    lines = f.readlines()
    x = [line.split()[0] for line in lines]
    y = [line.split()[1] for line in lines]

fig = plt.figure()

ax1 = fig.add_subplot(111)

ax1.set_title("Error curve")    
ax1.set_xlabel('number of epochs')
ax1.set_ylabel('training error')

ax1.plot(x,y, c='r', label='error curve')

leg = ax1.legend()

plt.show()