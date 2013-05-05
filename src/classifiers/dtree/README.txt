Entropy based decision tree classifier

Input format for train and test files:
comma seperated list of attribute names,class name
comma seperated list of attribute types,class type
comma seperated list of attribute values,class value per line

ex: with 4 attributes

a,b,c,d,isFraud
int,string,string,int,string	(each attribute type should be either int or string. float and double are not supported yet
3,good,non-suspicious,10,no
10,bad,suspicious,5,yes
5,good,suspicious,5,no
...
...


For int attributes, values need not be consecutive.

Usage:

javac classifiers.dtree.DecisionTreeClassification.java
java classifiers.dtree.DecisionTreeClassification ( prints command line arguments)
java classifiers.dtree.DecisionTreeClassification #command line arguments# 

An accuracy file is created which contains actual class and predicted class per test file instance, followed by the accuracy of classification
