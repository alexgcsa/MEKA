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

/**
 * MatrixUtils.java
 * Copyright (C) 2015 University of Mainz, Germany (to/from instances)
 */

package meka.core;

import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.matrix.Matrix;

import java.util.Arrays;
import java.util.Random;

/**
 * Utility functions relating to matrices.
 *
 * @author Jesse Read (jesse@tsc.uc3m.es)
 * @author Joerg Wicker (wicker@uni-mainz.de)
 * @version $Revision$
 */
public class MatrixUtils {

	/**
	 * Helper method that transforma an Instances object to a Matrix object.
	 *
	 * @param inst The Instances to transform.
	 * @return  The resulting Matrix object.
	 */
	public static Matrix instancesToMatrix(Instances inst){
		double[][] darr = new double[inst.numInstances()][inst.numAttributes()];
		for (int i =0 ; i < inst.numAttributes(); i++) {
			for (int j = 0; j < inst.attributeToDoubleArray(i).length; j++) {
				darr[j][i] = inst.attributeToDoubleArray(i)[j];
			}
		}
		return new Matrix(darr);
	}

	/**
	 * Helper method that transforms a Matrix object to an Instances object.
	 *
	 * @param mat The Matrix to transform.
	 * @param patternInst the Instances template to use
	 * @return  The resulting Instances object.
	 */
	public static Instances matrixToInstances(Matrix mat, Instances patternInst){
		Instances result = new Instances(patternInst);
		for (int i = 0; i < mat.getRowDimension(); i++) {
			double[] row =  mat.getArray()[i];
			DenseInstance denseInst = new DenseInstance(1.0, row);
			result.add(denseInst);
		}

		return result;
	}

	/**
	 * GetCol - return the k-th column of M (as a vector).
	 */
	public static double[] getCol(double[][] M, int k) {
		double[] col_k = new double[M.length];
		for (int i = 0; i < M.length; i++) {
			col_k[i] = M[i][k];
		}
		return col_k;
	}

	/**
	 * GetCol - return the k-th column of M (as a vector).
	 */
	public static int[] getCol(int[][] M, int k) {
		int[] col_k = new int[M.length];
		for (int i = 0; i < M.length; i++) {
			col_k[i] = M[i][k];
		}
		return col_k;
	}

	public static double[] addBias(double[] x) {
		final double[] x2 = new double[x.length+1];
		x2[0] = 1.0;
		for(int j = 0; j < x.length; j++) {
			x2[j+1] = x[j];
		}
		return x2;
	}

	public static double[][] addBias(double[][] M) {
		final double[][] C = new double[M.length][M[0].length+1];
		for (int i = 0; i < M.length; i++) {
			C[i][0] = 1.0;
			for(int j = 0; j < M[i].length; j++) {
				C[i][j+1] = M[i][j];
			}
		}
		return C;
	}

	public static Jama.Matrix addBias(Jama.Matrix M) {
		double[][] M_ = M.getArray();
		final double[][] C = new double[M_.length][M_[0].length+1];
		for (int i = 0; i < M_.length; i++) {
			C[i][0] = 1.0;
			for(int j = 0; j < M_[i].length; j++) {
				C[i][j+1] = M_[i][j];
			}
		}
		return new Jama.Matrix(C);
	}

	public static double[] removeBias(double[] x) {
		final double[] x2 = new double[x.length-1];
		for(int j = 1; j < x.length; j++) {
			x2[j-1] = x[j];
		}
        return x2;
    }

	public static Jama.Matrix removeBias(Jama.Matrix M) {
		return new Jama.Matrix(removeBias(M.getArray()));
    }

	public static double[][] removeBias(double[][] M) {
		final double[][] C = new double[M.length][M[0].length-1];
		for (int i = 0; i < M.length; i++) {
			for(int j = 1; j < M[i].length; j++) {
				C[i][j-1] = M[i][j];
			}
		}
		return C;
	}

	/**
	 * Multiply - multiply each value in A[][] by constant K.
	 */
	public static double[][] multiply(final double[][] A, double K) {

		final double[][] C = new double[A.length][A[0].length];

		for (int i = 0; i < A.length; i++) {
			for(int j = 0; j < A[i].length; j++) {
				C[i][j] = A[i][j] * K;
			}
		}
		return C;
	}

	public static String toString(Jama.Matrix M) {
		return toString(M.getArray());
	}

	/**
	 * ToString - return a String representation (to adp decimal places).
	 */
	public static String toString(double M_[][], int adp) {
		StringBuilder sb = new StringBuilder();
		for(int j = 0; j < M_.length; j++) {
			for(int k = 0; k < M_[j].length; k++) {
				//sb.append(Utils.doubleToString(v[k],w,adp));
				double d = M_[j][k];
				String num = String.format("%6.2f", d);
				if (adp == 0) // cheap override
					num = String.format("%2.0f", d);
				sb.append(num);
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * ToString - return a String representation.
	 */
	public static String toString(double M_[][]) {
		return toString(M_,2);
		//sb.append(A.toString(s.predictions.get(i),2));
	}

	/**
	 * ToString - return a String representation.
	 */
	public static String toString(int M_[][]) {
		StringBuilder sb = new StringBuilder();
		for(int j = 0; j < M_.length; j++) {
			for(int k = 0; k < M_[j].length; k++) {
				String num = String.format("%5d", M_[j][k]);
				sb.append(num);
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * ToString - return a String representation of 'M', in Matlab format, called 'name'.
	 */
	public static String toString(double M[][], String name) {
		StringBuilder sb = new StringBuilder(name+" = [\n");
		for(int j = 0; j < M.length; j++) {
			for(int k = 0; k < M[j].length; k++) {
				sb.append(String.format("%6.2f ", M[j][k]));
			}
			sb.append(";\n");
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Threshold - apply threshold t to matrix P[][].
	 */
	public static final double[][] threshold(double P[][], double t) {
		double X[][] = new double[P.length][P[0].length];
		for(int i = 0; i < P.length; i++) {
			for(int j = 0; j < P[i].length; j++) {
				X[i][j] = (P[i][j] > t) ? 1. : 0.;
			}
		}
		return X;
	}

	/**
	 * Flatten - turn Matrix [0 1; 2 3] into vector [0 1 2 3].
	 */
	public static int[] flatten(int M[][]) {
		int v[] = new int[M.length * M[0].length];
		int k = 0;
		for(int i = 0; i < M.length; i++) {
			for(int j = 0; j < M[i].length; j++) {
				v[k++] = M[i][j];
			}
		}
		return v;
	}

	/**
	 * Flatten - turn Matrix [0. 1.; 2. 3.] into vector [0. 1. 2. 3.].
	 */
	public static double[] flatten(double M[][]) {
		double v[] = new double[M.length * M[0].length];
		int k = 0;
		for(int i = 0; i < M.length; i++) {
			for(int j = 0; j < M[i].length; j++) {
				v[k++] = M[i][j];
			}
		}
		return v;
	}

	public static double[][] subtract(double[][] A, double[][] B) {
		//if (A.length != bRows) // no can do
		//	throw new IllegalArgumentException(" A.cols ("+aCols+") != B.rows ("+bRows+") ");
		double[][] C = new double[A.length][A[0].length];
		for (int i = 0; i < A.length; i++) {
			for (int j = 0; j < A[i].length; j++ ) {
				C[i][j] = A[i][j] - B[i][j];
			}
		}
		return C;
	}

	/**
	 * absolute value
 	 */
	public static double[][] abs(double[][] A) {
		double[][] C = new double[A.length][A[0].length];
		for (int i = 0; i < A.length; i++) {
			for (int j = 0; j < A[i].length; j++ ) {
				C[i][j] = Math.abs(A[i][j]);
			}
		}
		return C;
	}

	/**
	 * squared sum
 	 */
	public static double SS(double M[][]) {
		double sum = 0;
		for (int i = 0; i < M.length; i++) {
			for (int j = 0; j < M[i].length; j++) {
				sum += M[i][j];
			}
		}
		return sum;
	}

	/**
	 * Sigmoid / Logistic function
	 */
	public static final double sigma(double a) {
		return 1.0/(1.0+Math.exp(-a));
	}

	/**
	 * Sigmoid function applied to vector
	 */
	public static final double[] sigma(double v[]) {
		double u[] = new double[v.length];
		for(int j = 0; j < v.length; j++) {
			u[j] = sigma(v[j]);
		}
		return u;
	}

	/**
	 * Sigmoid function applied to matrix (2D array)
	 */
	public static final double[][] sigma(double A[][]) {
		double X[][] = new double[A.length][A[0].length];
		for(int i = 0; i < A.length; i++) {
			for(int j = 0; j < A[i].length; j++) {
				X[i][j] = sigma(A[i][j]);
			}
		}
		return X;
	}

	/**
	 * Sigmoid function applied to Matrix
	 */
	public static final Jama.Matrix sigma(Jama.Matrix A) {
		return new Jama.Matrix(sigma(A.getArray()));
	}

	/**
	 * Derivative of the sigmoid function applied to scalar
	 */
	public static final double dsigma(double a) {
		double s = sigma(a);
		return s * (1. - s);
	}

	/**
	 * Derivative of the sigmoid function applied to vector
	 */
	public static final double[] dsigma(double v[]) {
		double u[] = new double[v.length];
		for(int j = 0; j < v.length; j++) {
			u[j] = dsigma(v[j]);
		}
		return u;
	}

	/**
	 * Derivative of the sigmoid function applied to Matrix
	 */
	public static final double[][] dsigma(double A[][]) {
		double X[][] = new double[A.length][A[0].length];
		for(int i = 0; i < A.length; i++) {
			for(int j = 0; j < A[i].length; j++) {
				X[i][j] = dsigma(A[i][j]);
			}
		}
		return X;
	}

	/**
	 * Derivative of the sigmoid function applied to Jama Matrix
	 */
	public static final Jama.Matrix dsigma(Jama.Matrix A) {
		double A_[][] = A.getArray();
		double X[][] = new double[A_.length][A_[0].length];
		for(int i = 0; i < A_.length; i++) {
			for(int j = 0; j < A_[i].length; j++) {
				X[i][j] = dsigma(A_[i][j]);
			}
		}
		return new Jama.Matrix(X);
	}

	/**
	 * Deep Copy - Make a deep copy of M[][].
	 */
	public static int[][] deep_copy(int M[][]) {
		int[][] C = new int[M.length][];
		for(int i = 0; i < C.length; i++) {
			C[i] = Arrays.copyOf(M[i], M[i].length);
		}
		return C;
	}

	/**
	 * Ones - return a vector full of 1s.
	 */
	public static double[] ones(int L) {
		double m[] = new double[L];
		Arrays.fill(m,1.);
		return m;
	}

	/**
	 * Sum - sum this matrix.
	 */
	public static int[] sum(int M[][]) {
		int s[] = new int[M.length];
		for(int j = 0; j < M.length; j++) {
			for(int k = 0; k < M[j].length; k++) {
				s[j] += M[j][k];
			}
		}
		return s;
	}

	/**
	 * Sum - sum this matrix.
	 */
	public static double[] sum(double M[][]) {
		double s[] = new double[M.length];
		for(int j = 0; j < M.length; j++) {
			for(int k = 0; k < M[j].length; k++) {
				s[j] += M[j][k];
			}
		}
		return s;
	}

	public static final void fillRow(double M[][], int k, double val) {
		for(int j = 0; j < M[k].length; j++) {
			M[k][j] = val;
		}
	}

	public static final void fillCol(double M[][], int k, double val) {
		for(int i = 0; i < M.length; i++) {
			M[i][k] = val;
		}
	}

	/** A 2D array of Gaussian random numbers */
	public static double[][] randn(int rows, int cols, Random r) {
		double X[][] = new double[rows][cols];
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++) {
				X[i][j] = r.nextGaussian();
			}
		}
		return X;
	}

	/** A matrix of Gaussian random numbers */
	public static Jama.Matrix randomn(int nrows, int ncols, Random r) {
		return new Jama.Matrix(randn(nrows, ncols, r));
	}

	/** Copy a 2D array */
	public static final double[][] copy(double P[][]) {
		double X[][] = new double[P.length][P[0].length];
		for(int i = 0; i < P.length; i++) {
			for(int j = 0; j < P[i].length; j++) {
				X[i][j] = P[i][j];
			}
		}
		return X;
	}

	/**
	 * sample from matrix
	 */
	public static final double[][] threshold(double P[][], Random r) {
		double X[][] = new double[P.length][P[0].length];
		for(int i = 0; i < P.length; i++) {
			for(int j = 0; j < P[i].length; j++) {
				X[i][j] = (P[i][j] > r.nextDouble()) ? 1.0 : 0.0;
			}
		}
		return X;
	}

	/**
	 * threshold function applied to vector
 	 */
	public static final double[] threshold(double v[], double t) {
		double u[] = new double[v.length];
		for(int j = 0; j < v.length; j++) {
			u[j] = (v[j] > t) ? 1. : 0.;
		}
		return u;
	}

	/**
	 * sigmoid function applied to vector
	 */
	public static final Jama.Matrix threshold(Jama.Matrix M, double t) {
		return new Jama.Matrix(threshold(M.getArray(), t));
	}

	public static double[][] transposeMultiply(double[][] A, double[][] B) {
        double[][] At = getTranspose(A);
        return multiply(At, B);
    }

	public static double[][] multiplyTranspose(double[][] A, double[][] B) {
        double[][] Bt = getTranspose(B);
        return multiply(A, Bt);
    }

	/**
	 * Multiply - multiply matrices A and B together.
	 */
	public static double[][] multiply(final double[][] A, final double[][] B) {

		int aRows = A.length;
		int aCols = A[0].length;
		int bRows = B.length;
		int bCols = B[0].length;

		if (aCols != bRows) // no can do
			throw new IllegalArgumentException(" A.cols ("+aCols+") != B.rows ("+bRows+") ");

		double C[][] = new double[aRows][bCols];

		for (int i = 0; i < aRows; i++) {
			for (int k = 0; k < aCols; k++) {
				for(int j = 0; j < bCols; j++) {
					C[i][j] += A[i][k] * B[k][j];
				}
			}
		}
        return C;
    }

	/**
	 * Multiply - multiply vectors a and b together.
	 */
	public static double[] multiply(final double[] a, final double[] b) throws Exception {
		Jama.Matrix a_ = new Jama.Matrix(a,1);
		Jama.Matrix b_ = new Jama.Matrix(b,1);
		Jama.Matrix c_ = a_.arrayTimes(b_);
		return c_.getArray()[0];
    }

	public static double[][] getTranspose(double[][] M) {
        double[][] C = new double[M[0].length][];
        for (int i = 0; i < M[0].length; i++) {
            C[i] = getCol(M, i);
        }
        return C;
    }

	public static double[][] add(double[][] A, double[][] B) {
        double[][] C = new double[A.length][A[0].length];
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[i].length; j++ ) {
                C[i][j] = A[i][j] + B[i][j];
            }
        }
        return C;
    }

	public static double[][] add(double[][] A, double v) {
        double[][] C = new double[A.length][A[0].length];
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[i].length; j++ ) {
                C[i][j] = A[i][j] + v;
            }
        }
        return C;
    }

	public static double squaredError(double[] vector1, double[] vector2) {
        double squaredError = 0;
        for (int i = 0; i < vector1.length; i++) {
            squaredError += (vector1[i] - vector2[i]) * (vector1[i] - vector2[i]);
        }
        return squaredError;
    }

	public static double meanSquaredError(double[][] vectorBatch1, double[][] vectorBatch2) {
		double error = 0;
		for (int i = 0; i < vectorBatch1.length; i++) {
			error += squaredError(vectorBatch1[i], vectorBatch2[i]);
		}
		return error / vectorBatch1.length;
	}

	/** dot product of two vectors */
	public static double dot(double v[], double u[]) {
		double sum = 0.0;
		for(int i = 0; i < v.length; i++) {
			sum += (v[i] * u[i]);
		}
		return sum;
	}

	/**
	 * Sample - Returns Matrix C where each value C[j][k] is 1 with probability M[j][k] and 0 otherwise.
	 * (assume each value is in [0,1])
	 */
	public static double[][] sample(double M[][], Random r) {
		return threshold(M, r);
	}

	/**
	 * Sample - Returns vector c where each value c[j][k] is 1 with probability v[j][k] and 0 otherwise.
	 * (assume each value is in [0,1])
	 */
	public static double[] sample(double v[], Random r) {
		return threshold(new double[][]{v}, r)[0];
	}

	/**
	 * Sample - Returns Matrix C where each value C[j][k] is 1 with probability M[j][k] and 0 otherwise.
	 * (assume each value is in [0,1])
	 */
	public static Jama.Matrix sample(Jama.Matrix M, Random r) {
		return new Jama.Matrix(sample(M.getArray(),r));
	}

	public static void printMatrix(double M_[][]) {
		Jama.Matrix M = new Jama.Matrix(M_);
		M.print(5,3);
	}

	public static void printDim(Jama.Matrix M) {
		printDim(M.getArray());
	}

	public static void printDim(double M[][]) {
		System.out.println(""+M.length+" x "+M[0].length+"     (rows x cols)");
	}

	public static String getDim(double M[][]) {
		return ""+M.length+" x "+M[0].length+"     (rows x cols)";
	}

	public static String getDim(Jama.Matrix M) {
		return getDim(M.getArray());
	}

	/**
	 * returns argmax_{j,k} M[j][k]
 	 */
	public static int[] maxIndices(double M[][]) {
		double max = Double.MIN_VALUE;
		int i_max = -1;
		int j_max = -1;
		for(int i = 0; i < M.length; i++) {
			for(int j = 0; j < M[i].length; j++) {
				if (M[i][j] > max) {
					max = M[i][j];
					i_max = i;
					j_max = j;
				}
			}
		}
		return new int[]{i_max,j_max};
	}
}
