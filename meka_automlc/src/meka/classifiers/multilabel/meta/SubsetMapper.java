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

package meka.classifiers.multilabel.meta;

import meka.classifiers.multilabel.BR;
import meka.classifiers.multilabel.ProblemTransformationMethod;
import meka.core.MLUtils;
import weka.core.*;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

import java.util.*;
import meka.core.OptionUtils;

/**
 * Maps the output of a multi-label classifier to a known label combination using the hamming distance.
 * described in	<i>Improved Boosting Algorithms Using Confidence-rated Predictions</i> by Schapire, Robert E. and Singer, Yoram
 *
 * @author 	Jesse Read (jmr30@cs.waikato.ac.nz)
 */
public class SubsetMapper extends ProblemTransformationMethod
  implements TechnicalInformationHandler {

	/** for serialization. */
	private static final long serialVersionUID = -6587406787943635084L;
        
        protected long m_seed;

	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return 
				"Maps the output of a multi-label classifier to a known label combination using the hamming distance."
				+ "For more information see:\n"
				+ getTechnicalInformation().toString();
	}

	public SubsetMapper() {
		// default classifier for GUI
		this.m_Classifier = new BR();
	}

	@Override
	protected String defaultClassifierString() {

		// default classifier for CLI
		return "meka.classifiers.multilabel.BR";
	}

        public void setSeed(long m_seed) {
            this.m_seed = m_seed;
        }

        public long getSeed() {
            return m_seed;
        }
        

	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation	result;
		
		result = new TechnicalInformation(Type.ARTICLE);
		result.setValue(Field.AUTHOR, "Robert E. Schapire, Yoram Singer ");
		result.setValue(Field.TITLE, "Improved Boosting Algorithms Using Confidence-rated Predictions");
		result.setValue(Field.JOURNAL, "Machine Learning Journal");
		result.setValue(Field.YEAR, "1999");
		result.setValue(Field.VOLUME, "37");
		result.setValue(Field.NUMBER, "3");
		result.setValue(Field.PAGES, "297-336");
		
		return result;
	}

	protected HashMap<String,Integer> m_Count = new HashMap<String,Integer>();

    protected double[] nearestSubset(double d[]) throws Exception {   

		String comb = MLUtils.toBitString(doubles2ints(d));

		// If combination exists
		if (m_Count.get(comb) != null) {
			return MLUtils.fromBitString(comb);
		}

        int closest_count = 0;
        int min_distance = Integer.MAX_VALUE;
		String nearest = comb;

        for(String current : shuffle(m_Count.keySet())) {   
            int distance = hammingDistance(current,comb);
            if (distance == min_distance) {   
                int count = m_Count.get(current);
                if (count > closest_count) {   
                    nearest = current;
                    closest_count = count;
                }
            }
            if (distance < min_distance) {   
                min_distance = distance;
                nearest = current;
                closest_count = m_Count.get(nearest);
            }
        }
		return MLUtils.fromBitString(nearest);
    }

	private Collection<String> shuffle(Set<String> labelSubsets)
    {
    	
    	Vector<String> result = new Vector<String>(labelSubsets.size());
    	result.addAll(labelSubsets);
    	Collections.shuffle(result, new Random(m_seed));
    	return result;
    }

	@Override
	public void buildClassifier(Instances D) throws Exception {
	  	testCapabilities(D);
	  	
		for (int i = 0; i < D.numInstances(); i++) {
			m_Count.put(MLUtils.toBitString(D.instance(i),D.classIndex()),0);
		}

		m_Classifier.buildClassifier(D);

	}

	@Override
	public double[] distributionForInstance(Instance TestInstance) throws Exception {

		double r[] = ((ProblemTransformationMethod)m_Classifier).distributionForInstance(TestInstance);

		return nearestSubset(r);
	}

	private static final int[] doubles2ints(double d[]) {
		int b[] = new int[d.length];
		for(int i = 0; i < d.length; i++) {
			b[i] = (int)Math.round(d[i]);
		}
		return b;
	}

	private static final int hammingDistance(String s1, String s2) {
		int dist = 0;
		for(int i = 0; i < Math.min(s1.length(),s2.length()); i++) {
			dist += Math.abs(MLUtils.char2int(s1.charAt(i)) - MLUtils.char2int(s2.charAt(i)));
		}
		return dist;
	}
        
            /**
     * Returns an enumeration of the options.  
     *
     * @return Enumeration of the options.
     */
    public Enumeration listOptions() {
	Vector newVector = new Vector();

	 newVector.addElement(new Option("\tSets the seed\n\tdefault: 0", "S", 1, "-S <value>"));

	OptionUtils.add(newVector, super.listOptions());

	return OptionUtils.toEnumeration(newVector);
    }

    /**
     * Returns an array with the options of the classifier.
     * 
     * @return Array of options.
     */
    public String[] getOptions(){
	List<String> result = new ArrayList<>();
	OptionUtils.add(result, "S", this.getSeed());
	OptionUtils.add(result, super.getOptions());
	return OptionUtils.toArray(result);
    }

    /**
     * Sets the options to the given values in the array.
     *
     * @param options The options to be set.
     */
    public void setOptions(String[] options) throws Exception {
	setSeed(OptionUtils.parse(options, "S", 1));
	super.setOptions(options);
    }

	@Override
	public String getRevision() {
	    return RevisionUtils.extract("$Revision: 9117 $");
	}

	public static void main(String args[]) {
		ProblemTransformationMethod.evaluation(new SubsetMapper(), args);
	}

}
