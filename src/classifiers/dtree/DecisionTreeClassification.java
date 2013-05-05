package classifiers.dtree;

public class DecisionTreeClassification {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length != 4){
			System.out.println("Invalid command line arguments!");
			System.out.println("USAGE: DecisionTreeClassification complete-train-file-path complete-test-file-path" +
					" complete-accuracy-file-path number-attributes");
			System.exit(-1);
		}
		DataModel in = new DataModel(Integer.parseInt(args[3]));
		in.getInput(args[0]);
		DecisionTreeClassifier dtc = new DecisionTreeClassifier(in);
		System.out.println("Starting to train Decision Tree classifier...");
		long st = System.currentTimeMillis();
		dtc.trainDecisionTree();
		System.out.println("Decision Tree trained in " + (System.currentTimeMillis() - st) + " milliseconds...");
		System.out.println("Starting to evaluate trained classifier...");
		System.out.println("Accuracy of classifier:" + dtc.evaluateDecisionTree(in,args[1],args[2]));
		dtc.printDecisionTree();
	}

}
