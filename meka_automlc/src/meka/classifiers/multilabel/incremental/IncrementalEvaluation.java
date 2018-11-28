/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package meka.classifiers.multilabel.incremental;

import meka.core.*;
import weka.classifiers.UpdateableClassifier;
import weka.core.*;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.classifiers.multitarget.MultiTargetClassifier;
import meka.classifiers.multilabel.Evaluation;
import weka.core.converters.AbstractFileSaver;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * IncrementalEvaluation.java - For Evaluating Incremental (Updateable) Classifiers.
 * @author 		Jesse Read
 * @version 	September 2015
 */
public class IncrementalEvaluation {

	/**
	 * RunExperiment - Build and evaluate a model with command-line options.
	 * @param	h			a multi-label updateable classifier
	 * @param	args		classifier + dataset options
	 */
	public static void runExperiment(MultiLabelClassifier h, String args[]) {
		try {
			h.setOptions(args);
			Result avg = IncrementalEvaluation.evaluateModel(h,args);
			if (avg != null)
				System.out.println(avg);
		} catch(Exception e) {
			System.err.println("Evaluation exception ("+e+"); failed to run experiment");
			e.printStackTrace();
			printOptions(h.listOptions());
		}
	}

	/**
	 * EvaluateModel - Build and evaluate.
	 * @param	h			a multi-label Updateable classifier
	 * @param	options	dataset options (classifier options should already be set)
	 * @return	The evaluation Result, null if none produced (eg when just outputting predictions)
	 */
	public static Result evaluateModel(MultiLabelClassifier h, String options[]) throws Exception {
		boolean needPrebuiltModel = false;

		// Load data for non-incremental testing
		Instances test = null;
		if (Utils.getOptionPos('T', options) > -1) {
			test = Evaluation.loadDataset(options, 'T');
			MLUtils.prepareData(test);
			needPrebuiltModel = true;
		}

		// Load training data
		Instances train = null;
		if (Utils.getOptionPos('t', options) > -1) {
			train = Evaluation.loadDataset(options, 't');
			MLUtils.prepareData(train);
			needPrebuiltModel = false;  // we can build a model with training data
		}

		// output predictions to file?
		String predictions = Utils.getOption("predictions", options);

		// suppress evaluation?
		boolean doEval = !Utils.getFlag("no-eval", options);

		// Set the number of windows (batches) @todo move below combining options?
		int nWin = OptionUtils.parse(options, 'x', 10);

		// Set the size of the initial training
		int nInit = OptionUtils.parse(options, "split-percentage", 10);

		// Partially labelled ?
		double rLabeled = OptionUtils.parse(options, "supervision", 1.);

		// Get Threshold
		String Top = OptionUtils.parse(options, "threshold", "0.5");

		// Get Verbosity (do we want to see everything?)
		String Vop = OptionUtils.parse(options, "verbosity", "3");

		// Dump for later?
		String dname = null;
		if (Utils.getOptionPos('d',options) >= 0)
			dname = Utils.getOption('d',options);

		// Load from file?
		String lname = null;
		Instances dataHeader = null;
		if (Utils.getOptionPos('l',options) >= 0) {
			lname = Utils.getOption('l',options);
			Object[] data = SerializationHelper.readAll(lname);
			MultiLabelClassifier h2 = (MultiLabelClassifier)data[0];
			if (h.getClass() != h2.getClass())
				throw new IllegalArgumentException("Classifier stored in '" + lname + "' is not a " + h.getClass().getName());
			if (data.length > 1) {
				dataHeader = (Instances) data[1];
				String msg;
				if (train != null) {
					msg = train.equalHeadersMsg(dataHeader);
					if (msg != null)
						throw new IllegalArgumentException("New training data is not compatible with training header stored in '" + lname + "':\n" + msg);
				}
				if (test != null) {
					msg = test.equalHeadersMsg(dataHeader);
					if (msg != null)
						throw new IllegalArgumentException("New test data is not compatible with training header stored in '" + lname + "':\n" + msg);
				}
			}
			h = h2;
			needPrebuiltModel = false;  // successfully loaded prebuilt model
		}

		if (needPrebuiltModel)
			throw new IllegalArgumentException("Options require a prebuilt model, but none available!");

		if (h.getDebug()) {
			if (train != null)
				System.out.println(":- Dataset -: " + MLUtils.getDatasetName(train) + "\tL=" + train.classIndex() + "");
			if (test != null)
				System.out.println(":- Test -: " + MLUtils.getDatasetName(test) + "\tL=" + test.classIndex() + "");
		}

		Utils.checkForRemainingOptions(options);

		Result result = null;
		if (train != null) {
			if (doEval) {
				result = evaluateModelPrequentialBasic(h, train, nWin, rLabeled, Top, Vop);
			}
			else {
				Instances init = new Instances(train, 0, nWin); 	// initial window
				h.buildClassifier(init);
				for (Instance inst: train)
					((UpdateableClassifier) h).updateClassifier(inst);
			}
		}
		if (test != null && doEval) {
			if (h.getDebug())
				System.out.println("Non-incremental evaluation on provided test set");
			result = Evaluation.evaluateModel(h, test, Top, Vop);
		}

		if (dname != null) {
			dataHeader = new Instances(train, 0);
			SerializationHelper.writeAll(dname, new Object[]{h, dataHeader});
		}

		// predictions
		if (!predictions.isEmpty()) {
			if (test == null) {
				System.err.println("No test set provided, cannot make predictions!");
			}
			else {
				Instances predicted = new Instances(test, 0);
				for (int i = 0; i < test.numInstances(); i++) {
					double pred[] = h.distributionForInstance(test.instance(i));
					// Cut off any [no-longer-needed] probabalistic information from MT classifiers.
					if (h instanceof MultiTargetClassifier)
						pred = Arrays.copyOf(pred, test.classIndex());
					Instance predInst = (Instance) test.instance(i).copy();
					for (int j = 0; j < pred.length; j++)
						predInst.setValue(j, pred[j]);
					predicted.add(predInst);
				}
				AbstractFileSaver saver = ConverterUtils.getSaverForFile(predictions);
				if (saver == null) {
					System.err.println("Failed to determine saver for '" + predictions + "', using " + ArffSaver.class.getName());
					saver = new ArffSaver();
				}
				saver.setFile(new File(predictions));
				saver.setInstances(predicted);
				saver.writeBatch();
				System.out.println("Predictions saved to: " + predictions);
			}
		}

		return result;
	}

	private static String measures[] = new String[]{"Accuracy", "Exact match", "Hamming score"};

	/**
	 * EvaluateModel - over 20 windows.
	 */
	public static Result evaluateModel(MultiLabelClassifier h, Instances D) throws Exception {
		return evaluateModelPrequentialBasic(h,D,20,1.0,"PCut1","3");
	}

	/**
	 * EvaluateModelBatchWindow - Evaluate a multi-label data-stream model over windows.
	 * @param	h	Multilabel Classifier
	 * @param 	D	stream
	 * @param	numWindows	number of windows
	 * @param	rLabeled	labelled-ness (1.0 by default)
	 * @param	Top	threshold option
	 * @param	Vop	verbosity option
	 * @return	The Result on the final window (but it contains samples of all the other evaluated windows).
	 * The window is sampled every N/numWindows instances, for a total of numWindows windows.
	 */
	public static Result evaluateModelBatchWindow(MultiLabelClassifier h, Instances D, int numWindows, double rLabeled, String Top, String Vop) throws Exception {

		if (h.getDebug())
			System.out.println(":- Classifier -: "+h.getClass().getName()+": "+Arrays.toString(h.getOptions()));

		int N = D.numInstances();
		int L = D.classIndex();

		// the Result to use
		Result result = null;
		// the samples of all windows
		ArrayList<HashMap<String,Object>> samples = new ArrayList<HashMap<String,Object>>();

		long train_time = 0;
		long test_time = 0;

		int windowSize = (int) Math.floor(D.numInstances() / (double)numWindows);

		if (rLabeled * windowSize < 1.)
			throw new Exception ("[Error] The ratio of labelled instances ("+rLabeled+") is too small given the window size!");

		double nth = 1. / rLabeled; // label every nth example

		Instances D_init = new Instances(D,0,windowSize); 	// initial window

		if (h.getDebug()) {
			System.out.println("Training classifier on initial window ...");
		}
		train_time = System.currentTimeMillis();
		h.buildClassifier(D_init); 										// initial classifier
		train_time = System.currentTimeMillis() - train_time;
		if (h.getDebug()) {
			System.out.println("Done (in "+(train_time/1000.0)+" s)");
		}
		D = new Instances(D,windowSize,D.numInstances()-windowSize); 	// the rest (after the initial window)

		double t[] = new double[L];
		Arrays.fill(t,0.5);

		int V = MLUtils.getIntegerOption(Vop,3);
		if (h.getDebug()) {
			System.out.println("--------------------------------------------------------------------------------");
			System.out.print("#"+Utils.padLeft("w",6)+" "+Utils.padLeft("n",6));
			for (String m : measures) {
				System.out.print(" ");
				System.out.print(Utils.padLeft(m,12));
			}
			System.out.println("");
			System.out.println("--------------------------------------------------------------------------------");
		}

		int i = 0;
		for (int w = 0; w < numWindows-1; w++) {
			// For each evaluation window ...

			result = new Result(L);
			result.setInfo("Supervision",String.valueOf(rLabeled));
			result.setInfo("Type","MLi");

			int n = 0;
			test_time = 0;
			train_time = 0;

			for(int c = 0; i < (w*windowSize)+windowSize; i++) {
				// For each instance in the evaluation window ...

				Instance x = D.instance(i);
				AbstractInstance x_ = (AbstractInstance)((AbstractInstance) x).copy(); 		// copy 
				// (we can't clear the class values because certain classifiers need to know how well they're doing -- just trust that there's no cheating!)
				//for(int j = 0; j < L; j++)  
				//	x_.setValue(j,0.0);

				if ( rLabeled < 0.5 && (i % (int)(1/rLabeled) == 0) || ( rLabeled >= 0.5 && (i % (int)(1./(1.-rLabeled)) != 0 )) ) {
					// LABELLED - Test & record prediction 
					long before_test = System.currentTimeMillis();
					double y[] = h.distributionForInstance(x_);
					long after_test = System.currentTimeMillis();
					test_time += (after_test-before_test); // was +=
					result.addResult(y, x);
					n++;
				}
				else {
					// UNLABELLED
					x = MLUtils.setLabelsMissing(x,L);
				}

				// Update the classifier. (The classifier will have to decide if it wants to deal with unlabelled instances.)
				long before = System.currentTimeMillis();
				((UpdateableClassifier)h).updateClassifier(x);
				long after = System.currentTimeMillis();
				train_time += (after-before); // was +=
			}

			// calculate results
			result.setInfo("Threshold", Arrays.toString(t));
			result.output = Result.getStats(result,Vop, "test");
			result.setMeasurement("Test time", (test_time) / 1000.0);
			result.setMeasurement("Build time",(train_time)/1000.0);
			result.setMeasurement("Total time",(test_time+train_time)/1000.0);
			result.setMeasurement("Threshold",(double)t[0]);
			result.setMeasurement("Instances",(double)i);
			result.setMeasurement("Samples",(double)(samples.size()+1));
			samples.add(result.output);

			// Display results (to CLI)
			if (h.getDebug()) {
				System.out.print("#"+Utils.doubleToString((double)w+1,6,0)+" "+Utils.doubleToString((double)n,6,0));
				n = 0;
				for (String m : measures) {
					System.out.print(" ");
					System.out.print(Utils.doubleToString((Double)result.getMeasurement(m),12,4));
				} System.out.println("");
			}

			// Calibrate threshold for next window
			if (Top.equals("PCutL")) {
				t = ThresholdUtils.calibrateThresholds(result.predictions,MLUtils.labelCardinalities(result.actuals));
			}
			else {
				Arrays.fill(t,ThresholdUtils.calibrateThreshold(result.predictions,MLUtils.labelCardinality(result.allTrueValues())));
			}

		}

		if (h.getDebug()) {
			System.out.println("--------------------------------------------------------------------------------");
		}


		// This is the last Result; prepare it for evaluation output.
		result.setInfo("Classifier", h.getClass().getName());
		result.vals.put("Test time", (test_time) / 1000.0);
		result.vals.put("Build time",(train_time)/1000.0);
		result.vals.put("Total time",(test_time+train_time)/1000.0);
		result.vals.put("Total instances tested",(double)i);
		result.vals.put("Initial instances for training",(double)windowSize);
		result.setInfo("Options", Arrays.toString(h.getOptions()));
		result.setInfo("Additional Info", h.toString());
		result.setInfo("Dataset", MLUtils.getDatasetName(D));
		result.output = Result.getStats(result,Vop, "test");
		result.setMeasurement("Results sampled over time", Result.getResultsAsInstances(samples));

		return result;
	}

	/*
	 * EvaluateModelPrequentialWindow - Evaluate a multi-label data-stream model over a moving window.
	public static Result[] evaluateModelPrequentialWindow(MultilabelClassifier h, Instances D, int windowSize, double rLabeled) throws Exception {

		if (h.getDebug())
			System.out.println(":- Classifier -: "+h.getClass().getName()+": "+Arrays.toString(h.getOptions()));

		int L = D.classIndex();

		Result result = new Result();

		long train_time = 0;
		long test_time = 0;

		double nth = 1. / rLabeled; // label every nth example
		results.setInfo("Supervision",String.valueOf(rLabeled));

		Instances D_init = new Instances(D,0,windowSize); 	// initial window

		if (h.getDebug()) {
			System.out.println("Training classifier on initial window (of size "+windowSize+") ...");
		}

		train_time = System.currentTimeMillis();
		h.buildClassifier(D_init); 										// initial classifir
		train_time = System.currentTimeMillis() - train_time;

		D = new Instances(D,windowSize,D.numInstances()-windowSize); 	// the rest (after the initial window)

		if (h.getDebug()) {
			System.out.println("Proceeding to Test/Label/Update cycle on remaining ("+D.numInstances()+") instances ...");
		}

		for(int i = 0; i < D.numInstances(); i++) {

			test_time = 0;
			train_time = 0;

			Instance x = D.instance(i);
			AbstractInstance x_ = (AbstractInstance)((AbstractInstance) x).copy(); 		// copy 
				
			 * TEST
			long before_test = System.currentTimeMillis();
			double y[] = h.distributionForInstance(x_);
			long after_test = System.currentTimeMillis();
			test_time += (after_test-before_test); 
			result.addResult(y,x);

			 * LABEL BECOMES AVAILABLE ?
			if ( rLabeled >= 0.5 ) {
				x = MLUtils.setLabelsMissing(x,L);
			}

			 * UPDATE
			 * (The classifier will have to decide if it wants to deal with unlabelled instances.)
			long before = System.currentTimeMillis();
			((UpdateableClassifier)h).updateClassifier(x);
			long after = System.currentTimeMillis();
			train_time += (after-before); 

			// calculate results
			result.output = Result.getStats(results[w],Vop);
		}

		result.setInfo("Classifier",h.getClass().getName());
		result.setInfo("Options",Arrays.toString(h.getOptions()));
		result.setInfo("Additional Info",h.toString());
		result.setInfo("Dataset",MLUtils.getDatasetName(D));
		result.setInfo("Type","MLi");
		double t = 0.5;
		try {
			t = Double.parseDouble(Top);
		} catch(Exception e) {
			System.err.println("[WARNING] Only a single threshold can be chosen for this kind of evaluation; Using "+t);
		}
		result.setInfo("Threshold", t);

		result.vals.put("Test time",(test_time)/1000.0);
		result.vals.put("Build time",(train_time)/1000.0);
		result.vals.put("Total time",(test_time+train_time)/1000.0);

		return result;

	}
	*/

	/**
	 * Prequential Evaluation - Accuracy since the start of evaluation.
	 * @param	h	Multilabel Classifier
	 * @param 	D	stream
	 * @param	windowSize	sampling frequency (of evaluation statistics)
	 * @param	rLabeled	labelled-ness (1.0 by default)
	 * @param	Top	threshold option
	 * @param	Vop	verbosity option
	 * The window is sampled every N/numWindows instances, for a total of numWindows windows.
	 */
	public static Result evaluateModelPrequentialBasic(MultiLabelClassifier h, Instances D, int windowSize, double rLabeled, String Top, String Vop) throws Exception {

		if (h.getDebug())
			System.out.println(":- Classifier -: "+h.getClass().getName()+": "+Arrays.toString(h.getOptions()));

		int L = D.classIndex();

		Result result = new Result();

		long train_time = 0;
		long test_time = 0;

		double nth = 1. / rLabeled; // label every nth example
		result.setInfo("Supervision",String.valueOf(rLabeled));

		Instances D_init = new Instances(D,0,windowSize); 	// initial window

		if (h.getDebug()) {
			System.out.println("Training classifier on initial window (of size "+windowSize+") ...");
		}

		train_time = System.currentTimeMillis();
		h.buildClassifier(D_init); 										// initial classifir
		train_time = System.currentTimeMillis() - train_time;

		D = new Instances(D,windowSize,D.numInstances()-windowSize); 	// the rest (after the initial window)

		if (h.getDebug()) {
			System.out.println("Proceeding to Test/Label/Update cycle on remaining ("+D.numInstances()+") instances ...");
		}

		result.setInfo("Classifier",h.getClass().getName());
		result.setInfo("Options",Arrays.toString(h.getOptions()));
		result.setInfo("Additional Info",h.toString());
		result.setInfo("Dataset",MLUtils.getDatasetName(D));
		result.setInfo("Verbosity",Vop);
		if (h instanceof MultiTargetClassifier || Evaluation.isMT(D)) {
			result.setInfo("Type","MT");
		}
		else {
			result.setInfo("Type","ML");
			double t = 0.5;
			try {
				t = Double.parseDouble(Top);
			} catch(Exception e) {
				System.err.println("[WARNING] Only a single threshold can be chosen for this kind of evaluation; Using "+t);
			}
			result.setInfo("Threshold", String.valueOf(t));
		}
		ArrayList<HashMap<String,Object>> samples = new ArrayList<HashMap<String,Object>>();

		for(int i = 0; i < D.numInstances(); i++) {

			Instance x = D.instance(i);
			Instance x_ = (Instance)x.copy(); 		// copy
				
			/*
			 * TEST
			 */
			long before_test = System.currentTimeMillis();
			double y[] = h.distributionForInstance(x_);
			long after_test = System.currentTimeMillis();
			test_time += (after_test-before_test);
			result.addResult(y,x);

			/*
			 * LABEL BECOMES AVAILABLE ?
			 */
			if ( rLabeled >= 0.5 ) {
				x = MLUtils.setLabelsMissing(x,L);
			}

			/*
			 * UPDATE
			 * (The classifier will have to decide if it wants to deal with unlabelled instances.)
			 */
			long before = System.currentTimeMillis();
			((UpdateableClassifier)h).updateClassifier(x);
			long after = System.currentTimeMillis();
			train_time += (after-before); 

			/*
			 * RECORD MEASUREMENT
			 */
			if (i % windowSize == (windowSize-1)) {
				HashMap<String,Object> eval_sample = Result.getStats(result,Vop, "test");
				eval_sample.put("Test time",(test_time)/1000.0);
				eval_sample.put("Build time",(train_time)/1000.0);
				eval_sample.put("Total time",(test_time+train_time)/1000.0);
				eval_sample.put("Instances",(double)i);
				eval_sample.put("Samples",(double)(samples.size()+1));
				samples.add(eval_sample);
				System.out.println("Sample (#"+samples.size()+") of performance at "+i+"/"+D.numInstances()+" instances.");
			}

		}

		result.output = Result.getStats(result,Vop, "test");
		result.setMeasurement("Results sampled over time", Result.getResultsAsInstances(samples));

		result.vals.put("Test time",(test_time)/1000.0);
		result.vals.put("Build time",(train_time)/1000.0);
		result.vals.put("Total time",(test_time+train_time)/1000.0);

		return result;
	}


	public static void printOptions(Enumeration e) {

		// Evaluation Options
		StringBuffer text = new StringBuffer();
		text.append("\n\nEvaluation Options:\n\n");
		text.append("-t\n");
		text.append("\tSpecify the dataset (required)\n");
		text.append("-T <name of test file>\n");
		text.append("\tSets test file (will be used for making predictions).\n");
		text.append("-predictions <name of output file for predictions>\n");
		text.append("\tSets the file to store the predictions in (does not work with cross-validation).\n");
		//text.append("-split-percentage <percentage>\n");
		//text.append("\tSets the percentage of data to use for the initial training, e.g., 10.\n");
		text.append("-x <number of windows>\n");
		text.append("\tSets the number of samples to take (at evenly space intervals); default: 10.\n");
		text.append("-no-eval\n");
		text.append("\tSkips evaluation, e.g., used when test set contains no class labels.\n");
		text.append("-supervision <ratio labelled>\n");
		text.append("\tSets the ratio of labelled instances; default: 1.\n");
		text.append("-threshold <threshold>\n");
		text.append("\tSets the threshold to use.\n");
		text.append("-verbosity <verbosity level>\n");
		text.append("\tSpecify more/less evaluation output.\n");
		// Multilabel Options
		text.append("\n\nClassifier Options:\n\n");
		while (e.hasMoreElements()) {
			Option o = (Option) (e.nextElement());
			text.append("-"+o.name()+'\n');
			text.append(""+o.description()+'\n');
		}
		System.out.println(text);
	}

}
