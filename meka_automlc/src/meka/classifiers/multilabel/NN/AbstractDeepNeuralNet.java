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

package meka.classifiers.multilabel.NN;

import meka.core.OptionUtils;
import weka.core.Option;

import java.util.*;

/**
 * AbstractDeepNeuralNet.java - Extends AbstractNeuralNet with depth options. 
 * @author Jesse Read (jesse@tsc.uc3m.es)
 * @version December 2012
 */
public abstract class AbstractDeepNeuralNet extends AbstractNeuralNet  {

	private static final long serialVersionUID = 5416731163612885485L;

	protected int m_N = 2;
        
        /** number of hidden units for DBPNN */
	protected int m_H_dbpnn = 10;

	/** number of epochs for DBPNN */
	protected int m_E_dbpnn = 1000;

	/** learning rate for dbpnn */
	protected double m_R_dbpnn = 0.1;

	/** momentum for DBPNN */
	protected double m_M_dbpnn = 0.1;
        
        
        

	public int getN() { 
		return m_N;
	}

        public int getH_dbpnn() {
            return m_H_dbpnn;
        }

        public int getE_dbpnn() {
            return m_E_dbpnn;
        }

        public double getR_dbpnn() {
            return m_R_dbpnn;
        }

        public double getM_dbpnn() {
            return m_M_dbpnn;
        }
        
        

	public void setN(int n) { 
		m_N = n;
	}

        public void setH_dbpnn(int m_H_dbpnn) {
            this.m_H_dbpnn = m_H_dbpnn;
        }

        public void setE_dbpnn(int m_E_dbpnn) {
            this.m_E_dbpnn = m_E_dbpnn;
        }

        public void setR_dbpnn(double m_R_dbpnn) {
            this.m_R_dbpnn = m_R_dbpnn;
        }

        public void setM_dbpnn(double m_M_dbpnn) {
            this.m_M_dbpnn = m_M_dbpnn;
        }
        
        
        

	public String nTipText() {
		return "The number of RBMs.";
	}

	@Override
	public Enumeration listOptions() {
		Vector result = new Vector();
		result.addElement(new Option("\tSets the number of RBMs\n\tdefault: 2", "N", 1, "-N <value>"));
                result.addElement(new Option("\tSets the number of hidden units for DBPNN\n\tdefault: 10", "H_dbpnn", 1, "-H_dbpnn <value>"));
		result.addElement(new Option("\tSets the maximum number of epochs for DBPNN\n\tdefault: 1000\t(auto-cut-out)", "E_dbpnn", 1, "-E_dbpnn <value>"));
		result.addElement(new Option("\tSets the learning rate for DBPNN(tyically somewhere between 'very small' and 0.1)\n\tdefault: 0.1", "R_dbpnn", 1, "-R_dbpnn <value>"));
		result.addElement(new Option("\tSets the momentum for DBPNN(typically somewhere between 0.1 and 0.9)\n\tdefault: 0.1", "M_dbpnn", 1, "-M_dbpnn <value>"));
		
		OptionUtils.add(result, super.listOptions());
		return OptionUtils.toEnumeration(result);
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		setN(OptionUtils.parse(options, 'N', 2));
		setH_dbpnn(OptionUtils.parse(options, "H_dbpnn", 10));
		setE_dbpnn(OptionUtils.parse(options, "E_dbpnn", 1000));
		setR_dbpnn(OptionUtils.parse(options, "R_dbpnn", 0.1));
		setM_dbpnn(OptionUtils.parse(options, "M_dbpnn", 0.1));
		super.setOptions(options);
	}

	@Override
	public String [] getOptions() {
	  	List<String> result = new ArrayList<>();
		OptionUtils.add(result, 'N', getN());
                OptionUtils.add(result, "H_dbpnn", this.getH_dbpnn());
                OptionUtils.add(result, "E_dbpnn", this.getE_dbpnn());
                OptionUtils.add(result, "M_dbpnn", this.getM_dbpnn());
                OptionUtils.add(result, "R_dbpnn", this.getR_dbpnn());
		OptionUtils.add(result, super.getOptions());
		return OptionUtils.toArray(result);
	}
}

