package classifiers.dtree;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

class DataModel {
	int number_instances;		
	int number_attributes;
	
	ArrayList<int[]> instances;			//store mapped instances
	String[] attributeNames;			
	String[] attributeTypes;			//type of each attribute either string or int
	String className;			
	String classType;					//type of class attribute either string or int
	
	ArrayList<HashMap<String,Integer>> stringAttributeMappings = null;		//used to map string attributes to integers
	ArrayList<HashMap<Integer,Integer>> integerAttributeMappings = null;
	HashMap<Object,Integer> classMappings = null;		//used to map class values to integer
	
	//holds number of unique values per attribute and class
	int[] uniqueAttributeValueSoFar;
	int uniqueClassValueSoFar;
	
	public DataModel(int num_attributes){
		attributeNames = new String[num_attributes];
		attributeTypes = new String[num_attributes];
		number_instances = 0;
		this.number_attributes = num_attributes;
		instances = new ArrayList<int[]>();
	}
	public void getInput(String filename){
		BufferedReader br = null;
		ArrayList<String[]> tempInstances = new ArrayList<String[]>();
		try{
			String line;
			String[] words;
			int i;
			FileReader fr = new FileReader(filename);
			br = new BufferedReader(fr);
			//read attribute names
			line = br.readLine();
			if(line != null){
				words = line.split(",");
				if(words.length != number_attributes + 1){
					System.out.println("Error: Invalid input file format");
					System.exit(-1);
				}				
				for(i = 0;i < this.number_attributes;i++){
					attributeNames[i] = words[i];
				}
				className = words[number_attributes];
			}
			else{
				System.out.println("Error: Invalid input file format");
				System.exit(-1);				
			}
			//read attribute types
			line = br.readLine();
			if(line != null){
				words = line.split(",");
				if(words.length != number_attributes + 1){
					System.out.println("Error: Invalid input file format");
					System.exit(-1);
				}				
				for(i = 0;i < this.number_attributes;i++){
					attributeTypes[i] = words[i];
				}
				classType = words[number_attributes];
			}
			else{
				System.out.println("Error: Invalid input file format");
				System.exit(-1);				
			}
			//read rest of the input data
			while((line = br.readLine()) != null){
				words = line.split(",");
				if(words.length != number_attributes + 1){
					System.out.println("Error: Invalid input file format");
					System.exit(-1);
				}
				//String[] instance = new String[number_attributes + 1];
				tempInstances.add(words);
				this.number_instances++;
			}
			//this.number_classes = classTypes.size();
		}
		catch(IOException e){
			System.out.println("Error: " + e.getMessage());
		}
		finally{
			try {
				br.close();
			} catch (IOException e) {
				System.out.println("Error: " + e.getMessage());
			}
		}
		preProcess(tempInstances);
	}
	public void preProcess(ArrayList<String[]> tempInstances){
		int numberOfStringAttributes = 0;
		int numberOfIntegerAttributes = 0;
		int i,j;
		for(i = 0;i < attributeTypes.length;i++){
			if(attributeTypes[i].equals("string")){
				numberOfStringAttributes++;
			}
			else if(attributeTypes[i].equals("int")){
				numberOfIntegerAttributes++;
			}
			else{
				System.out.println("Error: Unknown attribute type " + "[" + attributeTypes[i] + "]");
				System.exit(-1);
			}
		}
		//ArrayList<HashMap<Integer,Boolean>> integerAttributeRanges = null;
		if(numberOfIntegerAttributes > 0){
			integerAttributeMappings = new ArrayList<HashMap<Integer,Integer>>();
			for(i = 0;i < numberOfIntegerAttributes;i++){
				integerAttributeMappings.add(new HashMap<Integer,Integer>());
			}
		}
		if(numberOfStringAttributes > 0){			
			this.stringAttributeMappings = new ArrayList<HashMap<String,Integer>>();
			for(i = 0;i < numberOfStringAttributes;i++){
				stringAttributeMappings.add(new HashMap<String,Integer>());
			}
		}
		
		if(classType.equals("string") || classType.equals("int")){
			this.classMappings = new HashMap<Object,Integer>();
		}
		else{
			System.out.println("Error: Unknown class type " + "[" + classType + "]");
			System.exit(-1);
		}
		
		this.uniqueAttributeValueSoFar = new int[this.number_attributes];
		this.uniqueClassValueSoFar = 0;
		for(i = 0;i < uniqueAttributeValueSoFar.length;i++){
			uniqueAttributeValueSoFar[i] = 0;
		}
		int strAttributeSoFar;
		int intAttributeSoFar;
		HashMap<String,Integer> hm1;
		HashMap<Integer,Integer> hm2;
		for(i = 0;i < number_instances;i++){
			strAttributeSoFar = 0;
			intAttributeSoFar = 0;
			for(j = 0;j < this.number_attributes;j++){
				if(this.attributeTypes[j].equals("string")){
					hm1 = stringAttributeMappings.get(strAttributeSoFar);
					if(!hm1.containsKey(tempInstances.get(i)[j])){
						hm1.put((String) tempInstances.get(i)[j],uniqueAttributeValueSoFar[j]);
						uniqueAttributeValueSoFar[j]++;
					}
					strAttributeSoFar++;
				}
				else{
					hm2 = integerAttributeMappings.get(intAttributeSoFar);
					if(!hm2.containsKey(Integer.parseInt(tempInstances.get(i)[j]))){
						hm2.put(Integer.parseInt(tempInstances.get(i)[j]),uniqueAttributeValueSoFar[j]);
						uniqueAttributeValueSoFar[j]++;
					}					
					intAttributeSoFar++;
				}
			}
			if(!this.classMappings.containsKey(tempInstances.get(i)[number_attributes])){
				classMappings.put(tempInstances.get(i)[number_attributes], uniqueClassValueSoFar);
				uniqueClassValueSoFar++;
			}
		}
		for(i = 0;i < number_instances;i++){
			strAttributeSoFar = 0;
			intAttributeSoFar = 0;
			int[] instance = new int[number_attributes + 1];
			for(j = 0;j < this.number_attributes;j++){
				if(this.attributeTypes[j].equals("string")){
					instance[j] = this.stringAttributeMappings.get(strAttributeSoFar).get((tempInstances.get(i)[j]));
					strAttributeSoFar++;
				}
				else{
					instance[j] = this.integerAttributeMappings.get(intAttributeSoFar)
							.get((Integer.parseInt(tempInstances.get(i)[j])));
					intAttributeSoFar++;
				}
			}
			instance[number_attributes] = this.classMappings.get(tempInstances.get(i)[number_attributes]);
			this.instances.add(instance);
		} 
	}
}
