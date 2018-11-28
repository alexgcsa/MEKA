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

import meka.classifiers.multilabel.CC;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.classifiers.multilabel.ProblemTransformationMethod;
import meka.core.OptionUtils;
import weka.core.Instance;
import weka.core.Option;
import weka.core.Randomizable;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import meka.classifiers.multilabel.AbstractMultiLabelClassifier;

/**
 * MultilabelMetaClassifier.java - For ensembles of multi-label methods.
 * @author Jesse Read (jmr30@cs.waikato.ac.nz)
 */
public abstract class MetaMethod extends AbstractMultiLabelClassifier {

	/** for serialization. */
	private static final long serialVersionUID = -6604797895790690612L;


	/**
	 * Description to display in the GUI.
	 * 
	 * @return		the description
	 */
	@Override
	public String globalInfo() {
		return "For ensembles of multi-label methods.";
	}

        @Override
        public double[] distributionForInstance(Instance i) throws Exception {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }


	@Override
	public Enumeration listOptions() {
		Vector result = new Vector();
		OptionUtils.add(result, super.listOptions());
		return OptionUtils.toEnumeration(result);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {
		List<String> result = new ArrayList<>();
		OptionUtils.add(result, super.getOptions());
		return OptionUtils.toArray(result);
	}

	/**
	 * Returns a string representation of the model.
	 *
	 * @return      the model
	 */
	public String getModel() {
            return "";
	}
}
