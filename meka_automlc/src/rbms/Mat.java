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

package rbms;

import Jama.Matrix;
import java.util.*;

/**
 * Mat.java - Handy array operations
 * <br>
 * TODO	Clean up duplication with M.java
 * <br>
 * TODO Make a Matrix class (inheriting Jama), but don't delete this (keep some of the static methods, integrate others).
 *
 * @version 	April 2014
 * @author 		Jesse Read 
 */
public abstract class Mat {

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
	public static Matrix randomn(int nrows, int ncols, Random r) {
		return new Matrix(Mat.randn(nrows,ncols,r));
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

	/*
	// the derivative of the sigmoid fn
	public static final double sigmaP(double a) {
		return Math.exp(-a) / Math.pow(1. + Math.exp(a),2);
	}
	public static final double[] sigmaP(double v[]) {
		double u[] = new double[v.length];
		for(int j = 0; j < v.length; j++) {
			u[j] = sigmaP(v[j]);
		}
		return u;
	}
	public static final double[][] sigmaP(double A[][]) {
		double X[][] = new double[A.length][A[0].length];
		for(int i = 0; i < A.length; i++) {
			for(int j = 0; j < A[i].length; j++) {
				X[i][j] = sigmaP(A[i][j]);
			}
		}
		return X;
	}
	*/

	// derivative of the sigmoid function
	public static final double dsigma(double a) {
		double s = sigma(a);
		return s * (1. - s);
	}

	// derivative of the sigmoid function for a vector
	public static final double[] dsigma(double v[]) {
		double u[] = new double[v.length];
		for(int j = 0; j < v.length; j++) {
			u[j] = dsigma(v[j]);
		}
		return u;
	}

	// derivative of the sigmoid function applied to each value of a matrix
	public static final double[][] dsigma(double A[][]) {
		double X[][] = new double[A.length][A[0].length];
		for(int i = 0; i < A.length; i++) {
			for(int j = 0; j < A[i].length; j++) {
				X[i][j] = dsigma(A[i][j]);
			}
		}
		return X;
	}

	// derivative of the sigmoid function applied to each value of a matrix
	public static final Matrix dsigma(Matrix A) {
		return new Matrix(dsigma(A.getArray()));
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
	public static final Matrix sigma(Matrix A) {
		return new Matrix(sigma(A.getArray()));
	}

	// sample from matrix
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
	 * Threshold - apply threshold t to matrix P[][].
	 * @deprecated - See M.java
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

	// threshold function applied to vector
	public static final double[] threshold(double v[], double t) {
		double u[] = new double[v.length];
		for(int j = 0; j < v.length; j++) {
			u[j] = (v[j] > t) ? 1. : 0.;
		}
		return u;
	}

	// sigmoid function applied to vector
	public static final Matrix threshold(Matrix M, double t) {
		return new Matrix(threshold(M.getArray(),t));
	}

	public static double[][] transposeMultiply(double[][] A, double[][] B) {
        double[][] At = getTranspose(A);
        return multiply(At, B);
    }
    
    public static double[][] multiplyTranspose(double[][] A, double[][] B) {
        double[][] Bt = getTranspose(B);
        return multiply(A, Bt);
    }

    public static double[][] getTranspose(double[][] M) {
        double[][] C = new double[M[0].length][];
        for (int i = 0; i < M[0].length; i++) {
            C[i] = getCol(M, i);
        }
        return C;
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
		Matrix a_ = new Matrix(a,1);
		Matrix b_ = new Matrix(b,1);
		Matrix c_ = a_.arrayTimes(b_);
		return c_.getArray()[0];
		/*

		if (a.length != B.length) // no can do
			throw new IllegalArgumentException(" a.cols ("+a.length+") != B.rows ("+B.length+") ");

        final double[] u = new double[a.length];

		for (int k = 0; k < a.length; k++) {
			for (int i = 0; i < B.length; i++) {
				u[k] += (a[k] * B[k][i]);
			}
		}
		return u;
		*/
    }

	/**
	 * Multiply - multiply each value in A[][] by constant K.
	 * @deprecated (use M.java)
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

	public static Matrix addBias(Matrix M) {
		return new Matrix(addBias(M.getArray()));
		/*
		double[][] M_ = M.getArray();
		final double[][] C = new double[M_.length][M_[0].length+1];
		for (int i = 0; i < M_.length; i++) {
			C[i][0] = 1.0;
			for(int j = 0; j < M_[i].length; j++) {
				C[i][j+1] = M_[i][j];
			}
		}
		return new Matrix(C);
		*/
    }

	public static double[] removeBias(double[] x) {
		final double[] x2 = new double[x.length-1];
		for(int j = 1; j < x.length; j++) {
			x2[j-1] = x[j];
		}
        return x2;
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


	public static Matrix removeBias(Matrix M) {
		return new Matrix(removeBias(M.getArray()));
    }

	/**
	 * GetCol - return the k-th column of M (as a vector).
	 * @deprecated (Use M.java instead)
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
	 * @deprecated (Use M.java instead)
	 */
	public static int[] getCol(int[][] M, int k) {
        int[] col_k = new int[M.length];
        for (int i = 0; i < M.length; i++) {
            col_k[i] = M[i][k];
        }
        return col_k;
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

	/**
	 * MSE - Mean Squared Error.
	 */
	public static double MSE(double A[][], double B[][]) {
		return meanSquaredError(A,B);
	}

    public static double meanSquaredError(double[][] vectorBatch1, double[][] vectorBatch2) {
        double error = 0;
        for (int i = 0; i < vectorBatch1.length; i++) {
            error += squaredError(vectorBatch1[i], vectorBatch2[i]);
        }
        return error / vectorBatch1.length;
    }

	/*
	 * TODO: USE A.prod
	public static double product(double v[]) {
		double prod = 1.0;
		for(int i = 0; i < v.length; i++) {
			prod *= v[i];
		}
		return prod;
	}
	*/

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
		return threshold(M,r);
	}

	/**
	 * Sample - Returns vector c where each value c[j][k] is 1 with probability v[j][k] and 0 otherwise.
	 * (assume each value is in [0,1])
	 */
	public static double[] sample(double v[], Random r) {
		return threshold(new double[][]{v},r)[0];
	}

	/**
	 * Sample - Returns Matrix C where each value C[j][k] is 1 with probability M[j][k] and 0 otherwise.
	 * (assume each value is in [0,1])
	 */
	public static Matrix sample(Matrix M, Random r) {
		return new Matrix(sample(M.getArray(),r));
	}

	public static String toString(Matrix M) {
		return toString(M.getArray());
	}

	/**
	 * ToString - return a String representation (to adp decimal places).
	 * @deprecated Use M.toString(M,adp)
	 */
	public static String toString(double M_[][], int adp) {
		int width = adp > 0 ? adp + 2 : 0;
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
	 * @deprecated Use M.toString(M)
	 */
	public static String toString(double M_[][]) {
		return toString(M_,2);
		//sb.append(A.toString(s.predictions.get(i),2));
	}

	/**
	 * ToString - return a String representation.
	 * @deprecated Use M.toString(M)
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

	public static void printMatrix(double M_[][]) {
		Matrix M = new Matrix(M_);
		M.print(5,3);
	}

	public static void printDim(Matrix M) {
		printDim(M.getArray());
	}

	public static void printDim(double M[][]) {
		System.out.println(""+M.length+" x "+M[0].length+"     (rows x cols)");
	}

	public static String getDim(double M[][]) {
		return ""+M.length+" x "+M[0].length+"     (rows x cols)";
	}

	public static String getDim(Matrix M) {
		return getDim(M.getArray());
	}

	// returns argmax_{j,k} M[j][k]
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

	public static void main(String args[]) {
		double E = 0.4;
		double Y = 0.6;
		System.out.println("dY "+E * dsigma(Y));
		double dY = E * (sigma(Y)  * ( 1. - sigma(Y) )); 
		System.out.println("dY "+dY);

		System.out.println(""+sigma(1.));
		System.out.println(""+sigma(-4.));
		System.out.println(""+dsigma(-4.));
		System.out.println(""+sigma(4.));
		System.out.println(""+dsigma(4.));
	}



}


