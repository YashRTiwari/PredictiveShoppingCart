import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
# Importing the Keras libraries and packages
import keras
from keras.models import Sequential
from keras.layers import Dense
# Making the Confusion Matrix
from sklearn.metrics import confusion_matrix
    
    # Splitting the dataset into the Training set and Test set
from sklearn.model_selection import train_test_split
    
    # Feature Scaling
from sklearn.preprocessing import StandardScaler
#import math 

cm_ = []

# Importing the dataset
dataset = pd.read_csv('dataset.csv')
X = dataset.iloc[:, 0: 3].values
y_ = [-5,-4,-3,-2,-1]

for i in y_:
    y = dataset.iloc[:, [i]].values

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size = 0.35, random_state = 0)
    
    sc = StandardScaler()
    X_train = sc.fit_transform(X_train)
    X_test = sc.transform(X_test)
    
    # Part 2 - Now let's make the ANN!
    
    
    
    # Initialising the ANN
    classifier = Sequential()
    
    # Adding the input layer and the first hidden layer
    classifier.add(Dense(output_dim = 2, init = 'uniform', activation = 'relu', input_dim = 3))
    
    # Adding the second hidden layer
    classifier.add(Dense(output_dim = 1, init = 'uniform', activation = 'relu'))
    
    # Adding the output layer
    classifier.add(Dense(output_dim = 1, init = 'uniform', activation = 'sigmoid'))
    
    # Compiling the ANN
    classifier.compile(optimizer = 'adam', loss = 'binary_crossentropy', metrics = ['accuracy'])
    
    # Fitting the ANN to the Training set
    classifier.fit(X_train, y_train, batch_size = 10, nb_epoch = 100)
    
    # Part 3 - Making the predictions and evaluating the model
    
    # Predicting the Test set results
    y_pred = classifier.predict(X_test)
    y_pred = (y_pred > 0.5)
    
    
    cm = confusion_matrix(y_test, y_pred)
    
    cm_.append(cm)