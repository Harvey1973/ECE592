import numpy as np
import matplotlib.pyplot as plt
import sklearn
import sklearn.datasets
import sklearn.linear_model

X = np.array([[-1,-1,1,1],[-1,1,-1,1]])
print('The training examples are ')
print(X[:,3])
print(X[:,0].shape)
Y = np.array([[-1],[1],[1],[-1]])
print(Y[0])
print(Y[0].shape)

n_h = 4
n_y = 1
n_x = 2
W1 = np.random.randn(n_h,n_x)*0.01 -0.5   #-0.5 works
b1 = np.zeros((n_h,1))
W2 = np.random.randn(n_y,n_h)*0.01 -0.5
b2 = np.zeros((n_y,1))



def sigmoid(X):
    return 1/(1+np.exp(-X))
def bi_sigmoid(X):
	return (1-np.exp(-X))/(1+np.exp(-X))
#q = np.array([[4.587e-06,9.99e-01,9.98e-01,8.54656e-06]])
#print(0.5*np.sum(np.square(Y-q)))

for i in range(4000):
    learning_rate = 0.2   #0.1 works
    cost = 0 
    for j in range(4):
        X_new = np.expand_dims(X[:,j],axis=1)
        Y_new = np.expand_dims(Y[j],axis=1)
        Z1 = np.dot(W1,X_new)+b1
        A1 = sigmoid(Z1)
        Z2 = np.dot(W2,A1)+b2
        A2 = bi_sigmoid(Z2)

        
        #m = Y.shape[1]
        cost += np.square(Y_new-A2)

        #dZ2 = np.multiply(A2*(1-A2),(A2-Y))
        dZ2 = np.multiply(0.5*(1+A2)*(1-A2),(A2-Y_new))
        dW2 = np.dot(dZ2,A1.T)
        db2 = np.sum(dZ2,axis=1,keepdims= True)
        dZ1 = np.dot(W2.T,dZ2)*0.5*A1*(1-A1)
        dW1 = np.dot(dZ1,X_new.T)
        db1 = np.sum(dZ1,axis=1,keepdims=True)

        W1 = W1-learning_rate*dW1
        b1 = b1-learning_rate*db1
        W2 = W2-learning_rate*dW2
        b2 = b2-learning_rate*db2
        parameters = {	"W1": W1,
                        "b1": b1,
                        "W2": W2,
    
                        "b2": b2}
    cost = cost*0.5
    if  i % 1000 == 0:
		print ("Cost after iteration %i %f "%(i,cost))
        

def predict(parameters,X):
	W1 = parameters['W1']
	W2 = parameters['W2']
	b1 = parameters['b1']
	b2 = parameters['b2']
	Z1 = np.dot(W1,X)+b1
	A1 = bi_sigmoid(Z1)
	Z2 = np.dot(W2,A1)+b2
	A2 = bi_sigmoid(Z2)
	print (A2)
	predictions = (A2>0.0)
	
	return predictions
prediction = predict(parameters,X)
print(prediction)
