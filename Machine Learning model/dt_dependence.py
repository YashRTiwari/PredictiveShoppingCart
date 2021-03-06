import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
#import math 

# Importing the dataset
dataset = pd.read_csv('dataset.csv')

X = dataset.iloc[:, [ -4, -1]]
y = dataset.iloc[:, [-2]]

from sklearn.model_selection import train_test_split
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size = 0.35)


# Fitting Decision Tree Classification to the Training set
from sklearn.tree import DecisionTreeClassifier
classifier = DecisionTreeClassifier(criterion = 'entropy', random_state = 0)
classifier.fit(X_train, y_train)

# Predicting the Test set results
y_pred = classifier.predict(X_test)


# Making the Confusion Matrix
from sklearn.metrics import confusion_matrix
cm = confusion_matrix(y_test, y_pred)