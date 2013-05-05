package classifiers.dtree;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

class BTreeNode{
	int attributeId;
	//boolean isLeaf;
	int classId;
	HashMap<Integer,BTreeNode> children;
	public BTreeNode(){
		attributeId = -1;		//-1 for leaf nodes
		classId = -1;			//-1 for internal nodes
		//isLeaf = false;
		children = null;
	}
}
class DecisionTreeClassifier {
	DataModel in;
	BTreeNode root;
	boolean isTrained;
	public DecisionTreeClassifier(DataModel dm){
		this.in = dm;
		root = null;
		isTrained = false;
	}
	public void trainDecisionTree(){
		int i;
		ArrayList<Integer> examples = new ArrayList<Integer>();
		for(i = 0;i < this.in.number_instances;i++){
			examples.add(i);
		}
		//ArrayList<Integer> parentExamples = null;
		ArrayList<Integer> attributes = new ArrayList<Integer>();
		for(i = 0;i < this.in.number_attributes;i++){
			attributes.add(i);
		}
		root = trainDecisionTree(root,examples,attributes,examples);
		isTrained = true;
	}
	private BTreeNode trainDecisionTree(BTreeNode btn,ArrayList<Integer> ex,ArrayList<Integer> att,ArrayList<Integer> pex){
		if(btn == null){
			btn = new BTreeNode();
		}
		int i,j;
		if(ex.size() == 0){
			//return plurality-value(pex)
			btn.classId = pluralityValue(pex);
			return btn;
		}
		else if(att.size() == 0){
			//return plurality-value(ex)
			btn.classId = pluralityValue(ex);
			return btn;
		}
		else{
			//int sumOfDist = 0;
			//double initial_entropy;
			int[] distribution;
			distribution = getDistribution(ex);			
			//ArrayList<Integer> nnzIndexes = new ArrayList<Integer>();
			i = 0;
			while(i < distribution.length && distribution[i] == 0)
				i++;
			j = i + 1;
			while(j < distribution.length && distribution[j] == 0)
				j++;
						
			if(j == distribution.length){
				//homogenous
				btn.classId = i;
				return btn; 
			}
			else{
				//not homogenous
				int attIdIndex = findImportantAttribute(ex,att);		//index of least entropy attribute in the list att
				int attId = att.get(attIdIndex);						//id of least entropy attribute in the list of all attributes
				btn.attributeId = attId;
				btn.children = new HashMap<Integer,BTreeNode>();
				ArrayList<ArrayList<Integer>> newex = getNewEx(ex,attId);
				att.remove(attIdIndex);
				//for(i = 0;i < in.attributeTypes[attId];i++){
				for(i = 0;i < in.uniqueAttributeValueSoFar[attId];i++){	
					btn.children.put(i, trainDecisionTree(null,newex.get(i),att,ex));
				}
				return btn;
			}
		}
	}
	private int[] getDistribution(ArrayList<Integer> ex){
		//int[] dist = new int[this.in.number_classes];
		int[] dist = new int[in.uniqueClassValueSoFar];
		for(int e : ex){
			//dist[in.actualClass.get(e)]++;
			dist[in.instances.get(e)[this.in.number_attributes]]++;
		}
		return dist;
	}
	private int findImportantAttribute(ArrayList<Integer> ex,ArrayList<Integer> attributes){
		//return index of min entropy attribute
		int i,j;
		double min_entropy = Double.MAX_VALUE;
		int minentIndex = 0;
		double entropy;
		ArrayList<int[]> subsetDist = new ArrayList<int[]>();		//subset distribution
		ArrayList<int[][]> classDist = new ArrayList<int[][]>();	//class distribution within subsets corresponding to each attribute
		//allocate memory for stats
		for(int att : attributes){
			subsetDist.add(new int[in.uniqueAttributeValueSoFar[att]]);
			classDist.add(new int[in.uniqueAttributeValueSoFar[att]][in.uniqueClassValueSoFar]);
		}
		
		for(int e : ex){
			for(i = 0;i < attributes.size();i++){
				subsetDist.get(i)[in.instances.get(e)[attributes.get(i)]]++;
				//classDist.get(i)[in.instances.get(e)[attributes.get(i)]][in.actualClass.get(e)]++;
				classDist.get(i)[in.instances.get(e)[attributes.get(i)]][in.instances.get(e)[in.number_attributes]]++;
			}
		}
		
		//normalize subsetDist values
		//choose best attribute for split
		for(i = 0;i < attributes.size();i++){
			entropy = 0;
			for(j = 0;j < subsetDist.get(i).length;j++){
				entropy += (((float)subsetDist.get(i)[j]) / ex.size()) * getEntropy(classDist.get(i)[j]);
			}
			if(entropy < min_entropy){
				min_entropy = entropy;
				minentIndex = i;
			}
		}
		return minentIndex;
	}
	private double getEntropy(int[] dist){
		int i;
		int sum = 0;
		double entropy = 0;
		double p = 0;
		for(i = 0;i < dist.length;i++)
			sum += dist[i];
		for(i = 0;i < dist.length;i++){
			p = (((double)dist[i]) / sum);
			entropy += -(p * (Math.log(p) / Math.log(2)));
		}
		return entropy;
	}
	private ArrayList<ArrayList<Integer>> getNewEx(ArrayList<Integer> ex,int attId){
		int i;
		ArrayList<ArrayList<Integer>> newex = new ArrayList<ArrayList<Integer>>();
		for(i = 0;i < in.uniqueAttributeValueSoFar[attId];i++){
			newex.add(new ArrayList<Integer>());
		}
		for(int e : ex){
			newex.get(in.instances.get(e)[attId]).add(e);
		}
		return newex;
	}
	private int pluralityValue(ArrayList<Integer> examples){
		int i;
		int[] classStats = new int[in.uniqueClassValueSoFar];
		for(int e : examples){
			classStats[in.instances.get(e)[in.number_attributes]]++;
		}
		int plurality = 0;
		int pluralityIndex = -1;
		for(i = 0;i < in.uniqueClassValueSoFar;i++){
			if(classStats[i] > plurality){
				plurality = classStats[i];
				pluralityIndex = i;
			}
		}
		return pluralityIndex;
	}
	public double evaluateDecisionTree(DataModel in,String testFile,String accuracyFile){
		int i;
		int predicted;
		int numberTestInstances = 0;
		int corrects = 0;
		if(!this.isTrained){
			System.out.println("Can't classify: Decision Tree untrained");
			return 0;
		}
		String[] lineSplits;
		String line;
		try{
			BufferedReader brTest = new BufferedReader(new FileReader(testFile));
			BufferedWriter bwAccuracy = new BufferedWriter(new FileWriter(accuracyFile));
			//read Attribute Names from testFile
			lineSplits = brTest.readLine().split(",");
			for(i = 0;i < lineSplits.length - 1;i++){
				if(!lineSplits[i].equals(in.attributeNames[i])){
					System.out.println("Error: attribute name mismatch in train and test files");
					System.exit(-1);
				}
			}
			if(!in.className.equals(lineSplits[lineSplits.length - 1])){
				System.out.println("Error: class name mismatch in train and test files");
				System.exit(-1);				
			}
			//read attribute types from testFile
			lineSplits = brTest.readLine().split(",");
			for(i = 0;i < lineSplits.length - 1;i++){
				if(!lineSplits[i].equals(in.attributeTypes[i])){
					System.out.println("Error: attribute type mismatch in train and test files");
					System.exit(-1);
				}
			}
			if(!in.classType.equals(lineSplits[lineSplits.length - 1])){
				System.out.println("Error: class type mismatch in train and test files");
				System.exit(-1);				
			}
			//need to invert HashMap corresponding to classType
			HashMap<Integer,Object> invertedClassMappings = new HashMap<Integer,Object>();
			Set<Object> classValues = in.classMappings.keySet();
			for(Object value : classValues){
				invertedClassMappings.put(in.classMappings.get(value),value);
			}
			
			bwAccuracy.write("Actual\tPredicted");
			bwAccuracy.newLine();
			//read testFile and classify
			int strAttributeSoFar;
			int intAttributeSoFar;
			int[] instance = new int[in.number_attributes];
			while((line = brTest.readLine()) != null){
				lineSplits = line.split(",");
				strAttributeSoFar = 0;
				intAttributeSoFar = 0;
				for(i = 0;i < lineSplits.length - 1;i++){
					if(in.attributeTypes[i].equals("string")){
						if(in.stringAttributeMappings.get(strAttributeSoFar).containsKey(lineSplits[i])){
							instance[i] = in.stringAttributeMappings.get(strAttributeSoFar).get(lineSplits[i]);
						}
						else{
							System.out.println("Error: unknown attribute value found in test file");
							System.exit(-1);
						}
						strAttributeSoFar++;
					}
					else{
						if(in.integerAttributeMappings.get(intAttributeSoFar).containsKey(Integer.parseInt(lineSplits[i]))){
							instance[i] = in.integerAttributeMappings.get(intAttributeSoFar).get(Integer.parseInt(lineSplits[i]));	
						}
						else{
							System.out.println("Error: unknown attribute value found in test file");
							System.exit(-1);							
						}
						intAttributeSoFar++;						
					}
				}
				predicted = classifyInstance(instance);
				bwAccuracy.write(lineSplits[lineSplits.length - 1] + "\t" + invertedClassMappings.get(predicted));
				bwAccuracy.newLine();						
				if(in.classType.equals("string")){
					if(lineSplits[lineSplits.length - 1].equals((String) invertedClassMappings.get(predicted))){
						corrects++;
					}
				}
				else{
					if(Integer.parseInt(lineSplits[lineSplits.length - 1]) == Integer.parseInt((String) invertedClassMappings.get(predicted))){
						corrects++;
					}
				}
				numberTestInstances++;
			}
			bwAccuracy.write("Accuracy in %:" + (((double)corrects * 100) / numberTestInstances));
			brTest.close();
			bwAccuracy.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return (((double)corrects * 100) / numberTestInstances);
	}
	private int classifyInstance(int[] instance){
		return classifyInstance(this.root,instance);
	}
	private int classifyInstance(BTreeNode btn,int[] instance){
		if(btn.attributeId == -1)
			return btn.classId;
		else
			return classifyInstance(btn.children.get(instance[btn.attributeId]),instance);
	}
	
	public void printDecisionTree(){
		System.out.println("Recursively printed Decision Tree");
		printDecisionTree(this.root);
	}
	
	private void printDecisionTree(BTreeNode btn){
		int i;
		if(btn == null)
			return;
		else{
			if(btn.attributeId != -1){
				System.out.println("Non leaf node with attribute id:" + btn.attributeId);
				//for(i = 0;i < in.attributeTypes[btn.attributeId];i++){
				for(i = 0;i < in.uniqueAttributeValueSoFar[btn.attributeId];i++){
					printDecisionTree(btn.children.get(i));
				}
			}
			else if(btn.classId != -1)
				System.out.println("Leaf node with class id:" + btn.classId);
		}
	}
}