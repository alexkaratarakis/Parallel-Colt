package cern.colt.matrix.impl;

import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import cern.colt.Timer;
import cern.colt.function.FloatProcedure;
import cern.colt.list.FloatArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.BenchmarkMatrixKernel;
import cern.colt.matrix.FComplexMatrix3D;
import cern.colt.matrix.FloatMatrix1D;
import cern.colt.matrix.FloatMatrix3D;
import cern.jet.math.FloatFunctions;
import edu.emory.mathcs.utils.ConcurrencyUtils;

public class BenchmarkDenseFloatMatrix3D {

	private static Timer t = new Timer();

	private static final String outputFile = "BenchmarkDenseFloatMatrix3D.txt";

	private static float[][][] a_3d, b_3d;

	private static float[] a_1d, b_1d;

	private static double[] noViewTimes;

	private static double[] viewTimes;

	public static void main(String[] args) {
		org.junit.runner.JUnitCore.main("cern.colt.matrix.impl.BenchmarkDenseFloatMatrix3D");
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		BenchmarkMatrixKernel.readSettings3D();
		Random rand = new Random(0);
		noViewTimes = new double[BenchmarkMatrixKernel.NTHREADS.length];
		viewTimes = new double[BenchmarkMatrixKernel.NTHREADS.length];
		ConcurrencyUtils.setThreadsBeginN_3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[0] * BenchmarkMatrixKernel.MATRIX_SIZE_3D[1] * BenchmarkMatrixKernel.MATRIX_SIZE_3D[2]);
		a_1d = new float[BenchmarkMatrixKernel.MATRIX_SIZE_3D[0] * BenchmarkMatrixKernel.MATRIX_SIZE_3D[1] * BenchmarkMatrixKernel.MATRIX_SIZE_3D[2]];
		a_3d = new float[BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]][BenchmarkMatrixKernel.MATRIX_SIZE_3D[1]][BenchmarkMatrixKernel.MATRIX_SIZE_3D[2]];
		int idx = 0;
		for (int s = 0; s < BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]; s++) {
			for (int r = 0; r < BenchmarkMatrixKernel.MATRIX_SIZE_3D[1]; r++) {
				for (int c = 0; c < BenchmarkMatrixKernel.MATRIX_SIZE_3D[2]; c++) {
					a_3d[s][r][c] = rand.nextFloat();
					a_1d[idx++] = a_3d[s][r][c];
				}
			}
		}
		b_1d = new float[BenchmarkMatrixKernel.MATRIX_SIZE_3D[0] * BenchmarkMatrixKernel.MATRIX_SIZE_3D[1] * BenchmarkMatrixKernel.MATRIX_SIZE_3D[2]];
		b_3d = new float[BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]][BenchmarkMatrixKernel.MATRIX_SIZE_3D[1]][BenchmarkMatrixKernel.MATRIX_SIZE_3D[2]];
		idx = 0;
		for (int s = 0; s < BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]; s++) {
			for (int r = 0; r < BenchmarkMatrixKernel.MATRIX_SIZE_3D[1]; r++) {
				for (int c = 0; c < BenchmarkMatrixKernel.MATRIX_SIZE_3D[2]; c++) {
					b_3d[s][r][c] = rand.nextFloat();
					b_1d[idx++] = b_3d[s][r][c];
				}
			}
		}
		BenchmarkMatrixKernel.writePropertiesToFile(outputFile, BenchmarkMatrixKernel.MATRIX_SIZE_3D);
		BenchmarkMatrixKernel.displayProperties(BenchmarkMatrixKernel.MATRIX_SIZE_3D);

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		a_1d = null;
		a_3d = null;
		b_1d = null;
		b_3d = null;
		ConcurrencyUtils.resetThreadsBeginN();
		System.gc();
	}

	@Test
	public void testAggregateFloatFloatFunctionFloatFunction() {
		/* No view */
		FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			float aSum = A.aggregate(FloatFunctions.plus, FloatFunctions.square);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				t.reset().start();
				aSum = A.aggregate(FloatFunctions.plus, FloatFunctions.square);
				t.stop();
				noViewTimes[i] += t.millis();
			}
			noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		/* View */
		FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			float aSum = Av.aggregate(FloatFunctions.plus, FloatFunctions.square);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				t.reset().start();
				aSum = Av.aggregate(FloatFunctions.plus, FloatFunctions.square);
				t.stop();
				viewTimes[i] += t.millis();
			}
			viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		String method = "aggregate(FloatFloatFunction, FloatFunction)";
		BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
		BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	}

	@Test
	public void testAggregateFloatFloatFunctionFloatFunctionFloatProcedure() {
		FloatProcedure procedure = new FloatProcedure() {
			public boolean apply(float element) {
				if (Math.abs(element) > 0.2) {
					return true;
				} else {
					return false;
				}
			}
		};
		/* No view */
		FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			float aSum = A.aggregate(FloatFunctions.plus, FloatFunctions.square, procedure);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				t.reset().start();
				aSum = A.aggregate(FloatFunctions.plus, FloatFunctions.square, procedure);
				t.stop();
				noViewTimes[i] += t.millis();
			}
			noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		/* View */
		FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			float aSum = Av.aggregate(FloatFunctions.plus, FloatFunctions.square, procedure);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				t.reset().start();
				aSum = Av.aggregate(FloatFunctions.plus, FloatFunctions.square, procedure);
				t.stop();
				viewTimes[i] += t.millis();
			}
			viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		String method = "aggregate(FloatFloatFunction, FloatFunction, FloatProcedure)";
		BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
		BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	}

	@Test
	public void testAggregateFloatFloatFunctionFloatFunctionIntArrayListIntArrayListIntArrayList() {
		/* No view */
		FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
		IntArrayList sliceList = new IntArrayList();
		IntArrayList rowList = new IntArrayList();
		IntArrayList columnList = new IntArrayList();
		for (int s = 0; s < BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]; s++) {
			for (int r = 0; r < BenchmarkMatrixKernel.MATRIX_SIZE_3D[1]; r++) {
				for (int c = 0; c < BenchmarkMatrixKernel.MATRIX_SIZE_3D[2]; c++) {
					sliceList.add(s);
					rowList.add(r);
					columnList.add(c);
				}
			}
		}
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			float aSum = A.aggregate(FloatFunctions.plus, FloatFunctions.square, sliceList, rowList, columnList);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				t.reset().start();
				aSum = A.aggregate(FloatFunctions.plus, FloatFunctions.square, sliceList, rowList, columnList);
				t.stop();
				noViewTimes[i] += t.millis();
			}
			noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		/* View */
		FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			float aSum = Av.aggregate(FloatFunctions.plus, FloatFunctions.square, sliceList, rowList, columnList);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				t.reset().start();
				aSum = Av.aggregate(FloatFunctions.plus, FloatFunctions.square, sliceList, rowList, columnList);
				t.stop();
				viewTimes[i] += t.millis();
			}
			viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		String method = "aggregate(FloatFloatFunction, FloatFunction, IntArrayList, IntArrayList, IntArrayList)";
		BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
		BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	}

	@Test
	public void testAggregateFloatMatrix3DFloatFloatFunctionFloatFloatFunction() {
		/* No view */
		FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
		FloatMatrix3D B = new DenseFloatMatrix3D(b_3d);
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			float aSum = A.aggregate(B, FloatFunctions.plus, FloatFunctions.mult);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				t.reset().start();
				aSum = A.aggregate(B, FloatFunctions.plus, FloatFunctions.mult);
				t.stop();
				noViewTimes[i] += t.millis();
			}
			noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		/* View */
		FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
		FloatMatrix3D Bv = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(b_3d);
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			float aSum = Av.aggregate(Bv, FloatFunctions.plus, FloatFunctions.mult);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				t.reset().start();
				aSum = Av.aggregate(Bv, FloatFunctions.plus, FloatFunctions.mult);
				t.stop();
				viewTimes[i] += t.millis();
			}
			viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		String method = "aggregate(FloatMatrix3D, FloatFloatFunction, FloatFloatFunction)";
		BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
		BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	}

	@Test
	public void testAssignFloat() {
		/* No view */
		FloatMatrix3D A = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[0], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[2]);
		float value = (float)Math.random();
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			A.assign(value);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				A.assign(0);
				t.reset().start();
				A.assign(value);
				t.stop();
				noViewTimes[i] += t.millis();
			}
			noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		/* View */
		FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			Av.assign(value);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				Av.assign(0);
				t.reset().start();
				Av.assign(value);
				t.stop();
				viewTimes[i] += t.millis();
			}
			viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		String method = "assign(float)";
		BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
		BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	}

	@Test
	public void testAssignFloatArray() {
		/* No view */
		FloatMatrix3D A = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[0], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[2]);
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			A.assign(a_1d);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				A.assign(0);
				t.reset().start();
				A.assign(a_1d);
				t.stop();
				noViewTimes[i] += t.millis();
			}
			noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		/* View */
		FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			Av.assign(a_1d);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				Av.assign(0);
				t.reset().start();
				Av.assign(a_1d);
				t.stop();
				viewTimes[i] += t.millis();
			}
			viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		String method = "assign(float[])";
		BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
		BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	}

	@Test
	public void testAssignFloatArrayArrayArray() {
		/* No view */
		FloatMatrix3D A = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[0], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[2]);
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			A.assign(a_3d);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				A.assign(0);
				t.reset().start();
				A.assign(a_3d);
				t.stop();
				noViewTimes[i] += t.millis();
			}
			noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		/* View */
		FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			Av.assign(a_3d);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				Av.assign(0);
				t.reset().start();
				Av.assign(a_3d);
				t.stop();
				viewTimes[i] += t.millis();
			}
			viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		String method = "assign(float[][][])";
		BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
		BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	}

	@Test
	public void testAssignFloatFunction() {
		/* No view */
		FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			A.assign(FloatFunctions.square);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				A.assign(a_3d);
				t.reset().start();
				A.assign(FloatFunctions.square);
				t.stop();
				noViewTimes[i] += t.millis();
			}
			noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		/* View */
		FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			Av.assign(FloatFunctions.square);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				Av.assign(a_3d);
				t.reset().start();
				Av.assign(FloatFunctions.square);
				t.stop();
				viewTimes[i] += t.millis();
			}
			viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		String method = "assign(FloatFunction)";
		BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
		BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	}

	@Test
	public void testAssignFloatMatrix3D() {
		/* No view */
		FloatMatrix3D A = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[0], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[2]);
		FloatMatrix3D B = new DenseFloatMatrix3D(a_3d);
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			A.assign(B);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				A.assign(0);
				t.reset().start();
				A.assign(B);
				t.stop();
				noViewTimes[i] += t.millis();
			}
			noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		/* View */
		FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
		FloatMatrix3D Bv = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(b_3d);
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
			// warm-up
			Av.assign(Bv);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				Av.assign(0);
				t.reset().start();
				Av.assign(Bv);
				t.stop();
				viewTimes[i] += t.millis();
			}
			viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		String method = "assign(FloatMatrix3D)";
		BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
		BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	}

	@Test
	public void testAssignFloatMatrix3DFloatFloatFunction() {
		/* No view */
		FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
		FloatMatrix3D B = new DenseFloatMatrix3D(b_3d);
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			A.assign(B, FloatFunctions.div);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				A.assign(a_3d);
				t.reset().start();
				A.assign(B, FloatFunctions.div);
				t.stop();
				noViewTimes[i] += t.millis();
			}
			noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		/* View */
		FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
		FloatMatrix3D Bv = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(b_3d);
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			Av.assign(Bv, FloatFunctions.div);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				Av.assign(a_3d);
				t.reset().start();
				Av.assign(Bv, FloatFunctions.div);
				t.stop();
				viewTimes[i] += t.millis();
			}
			viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		String method = "assign(FloatMatrix3D, FloatFloatFuction)";
		BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
		BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	}

	@Test
	public void testAssignFloatMatrix3DFloatFloatFunctionIntArrayListIntArrayListIntArrayList() {
		/* No view */
		FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
		FloatMatrix3D B = new DenseFloatMatrix3D(b_3d);
		IntArrayList sliceList = new IntArrayList();
		IntArrayList rowList = new IntArrayList();
		IntArrayList columnList = new IntArrayList();
		for (int s = 0; s < BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]; s++) {
			for (int r = 0; r < BenchmarkMatrixKernel.MATRIX_SIZE_3D[1]; r++) {
				for (int c = 0; c < BenchmarkMatrixKernel.MATRIX_SIZE_3D[2]; c++) {
					sliceList.add(s);
					rowList.add(r);
					columnList.add(c);
				}
			}
		}
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			A.assign(B, FloatFunctions.div, sliceList, rowList, columnList);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				A.assign(a_3d);
				t.reset().start();
				A.assign(B, FloatFunctions.div, sliceList, rowList, columnList);
				t.stop();
				noViewTimes[i] += t.millis();
			}
			noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		/* View */
		FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
		FloatMatrix3D Bv = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(b_3d);
        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			Av.assign(Bv, FloatFunctions.div, sliceList, rowList, columnList);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				Av.assign(a_3d);
				t.reset().start();
				Av.assign(Bv, FloatFunctions.div, sliceList, rowList, columnList);
				t.stop();
				viewTimes[i] += t.millis();
			}
			viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		String method = "assign(FloatMatrix3D, FloatFloatFuction, IntArrayList, IntArrayList, IntArrayList)";
		BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
		BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	}

	@Test
	public void testAssignFloatProcedureFloat() {
		/* No view */
		FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
		FloatProcedure procedure = new FloatProcedure() {
			public boolean apply(float element) {
				if (Math.abs(element) > 0.1) {
					return true;
				} else {
					return false;
				}
			}
		};
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			A.assign(procedure, -1);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				A.assign(a_3d);
				t.reset().start();
				A.assign(procedure, -1);
				t.stop();
				noViewTimes[i] += t.millis();
			}
			noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		/* View */
		FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			Av.assign(procedure, -1);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				Av.assign(a_3d);
				t.reset().start();
				Av.assign(procedure, -1);
				t.stop();
				viewTimes[i] += t.millis();
			}
			viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		String method = "assign(FloatProcedure, float)";
		BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
		BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	}

	@Test
	public void testAssignFloatProcedureFloatFunction() {
		/* No view */
		FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
		FloatProcedure procedure = new FloatProcedure() {
			public boolean apply(float element) {
				if (Math.abs(element) > 0.1) {
					return true;
				} else {
					return false;
				}
			}
		};
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			A.assign(procedure, FloatFunctions.square);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				A.assign(a_3d);
				t.reset().start();
				A.assign(procedure, FloatFunctions.square);
				t.stop();
				noViewTimes[i] += t.millis();
			}
			noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		/* View */
		FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1]).viewDice(2, 1, 0).assign(a_3d);
		for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
			ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

			// warm-up
			Av.assign(procedure, FloatFunctions.square);
			for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
				Av.assign(a_3d);
				t.reset().start();
				Av.assign(procedure, FloatFunctions.square);
				t.stop();
				viewTimes[i] += t.millis();
			}
			viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
		}
		String method = "assign(FloatProcedure, FloatFunction)";
		BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
		BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	}
	
	 @Test
	    public void testCardinality() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

	            // warm-up
	            int card = A.cardinality();
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                t.reset().start();
	                card = A.cardinality();
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        A = new DenseFloatMatrix3D(a_3d);
	        FloatMatrix3D Av = A.viewDice(2, 1, 0);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

	            // warm-up
	            int card = Av.cardinality();
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                t.reset().start();
	                card = Av.cardinality();
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "cardinality()";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }

	    @Test
	    public void testDct3() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            A.dct3(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                A.assign(a_3d);
	                t.reset().start();
	                A.dct3(true);
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            Av.dct3(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                Av.assign(a_3d);
	                t.reset().start();
	                Av.dct3(true);
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "dct3(boolean)";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }

	    @Test
	    public void testDct2Slices() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            A.dct2Slices(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                A.assign(a_3d);
	                t.reset().start();
	                A.dct2Slices(true);
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	    	for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            Av.dct2Slices(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                Av.assign(a_3d);
	                t.reset().start();
	                Av.dct2Slices(true);
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "dct2Slices(boolean)";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }
	    
	    @Test
	    public void testDst3() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            A.dst3(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                A.assign(a_3d);
	                t.reset().start();
	                A.dst3(true);
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            Av.dst3(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                Av.assign(a_3d);
	                t.reset().start();
	                Av.dst3(true);
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "dst3(boolean)";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }

	    @Test
	    public void testDst2Slices() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            A.dst2Slices(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                A.assign(a_3d);
	                t.reset().start();
	                A.dst2Slices(true);
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	    	for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            Av.dst2Slices(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                Av.assign(a_3d);
	                t.reset().start();
	                Av.dst2Slices(true);
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "dst2Slices(boolean)";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }
	    
	    @Test
	    public void testDht3() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            A.dht3();
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                A.assign(a_3d);
	                t.reset().start();
	                A.dht3();
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            Av.dht3();
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                Av.assign(a_3d);
	                t.reset().start();
	                Av.dht3();
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "dht3()";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }

	    @Test
	    public void testDht2Slices() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            A.dht2Slices();
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                A.assign(a_3d);
	                t.reset().start();
	                A.dht2Slices();
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	    	for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            Av.dht2Slices();
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                Av.assign(a_3d);
	                t.reset().start();
	                Av.dht2Slices();
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "dht2Slices()";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }
	    
	    @Test
	    public void testFft3() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            A.fft3();
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                A.assign(a_3d);
	                t.reset().start();
	                A.fft3();
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            Av.fft3();
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                Av.assign(a_3d);
	                t.reset().start();
	                Av.fft3();
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "fft3()";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }
	    
	    @Test
	    public void testGetFft3() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        FComplexMatrix3D Ac;
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

	            // warm-up
	            Ac = A.getFft3();
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                A.assign(a_3d);
	                t.reset().start();
	                Ac = A.getFft3();
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

	            // warm-up
	            Ac = Av.getFft3();
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                Av.assign(a_3d);
	                t.reset().start();
	                Ac = Av.getFft3();
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "getFft3()";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }

	    @Test
	    public void testGetFft2Slices() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        FComplexMatrix3D Ac;
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            Ac = A.getFft2Slices();
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                A.assign(a_3d);
	                t.reset().start();
	                Ac = A.getFft2Slices();
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }

	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            Ac = Av.getFft2Slices();
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                Av.assign(a_3d);
	                t.reset().start();
	                Ac = Av.getFft2Slices();
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }

	        String method = "getFft2Slices()";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }
	    
	    @Test
	    public void testGetIfft3() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        FComplexMatrix3D Ac;
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

	            // warm-up
	            Ac = A.getIfft3(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                A.assign(a_3d);
	                t.reset().start();
	                Ac = A.getIfft3(true);
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

	            // warm-up
	            Ac = Av.getIfft3(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                Av.assign(a_3d);
	                t.reset().start();
	                Ac = Av.getIfft3(true);
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "getIfft3(boolean)";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }

	    @Test
	    public void testGetIfft2Slices() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        FComplexMatrix3D Ac;
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            Ac = A.getIfft2Slices(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                A.assign(a_3d);
	                t.reset().start();
	                Ac = A.getIfft2Slices(true);
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }

	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            Ac = Av.getIfft2Slices(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                Av.assign(a_3d);
	                t.reset().start();
	                Ac = Av.getIfft2Slices(true);
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }

	        String method = "getIfft2Slices(true)";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }
	    
	    @Test
	    public void testGetNegativeValuesIntArrayListIntArrayListIntArrayListFloatArrayList() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        A.assign(FloatFunctions.mult(-1));
	        IntArrayList sliceList = new IntArrayList();
	        IntArrayList rowList = new IntArrayList();
	        IntArrayList columnList = new IntArrayList();
	        FloatArrayList valueList = new FloatArrayList();
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

	            // warm-up
	            A.getNegativeValues(sliceList, rowList, columnList, valueList);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                sliceList.clear();
	                rowList.clear();
	                columnList.clear();
	                valueList.clear();
	                t.reset().start();
	                A.getNegativeValues(sliceList, rowList, columnList, valueList);
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
            sliceList.clear();
            rowList.clear();
            columnList.clear();
            valueList.clear();
	        FloatMatrix3D Av = new DenseFloatMatrix3D(a_3d).viewDice(2, 1, 0);
	        Av.assign(FloatFunctions.mult(-1));
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

	            // warm-up
	            Av.getNegativeValues(sliceList, rowList, columnList, valueList);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                sliceList.clear();
	                rowList.clear();
	                columnList.clear();
	                valueList.clear();
	                t.reset().start();
	                Av.getNegativeValues(sliceList, rowList, columnList, valueList);
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "getNegativeValues(IntArrayList, IntArrayList, IntArrayList, FloatArrayList)";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }

	    @Test
	    public void testGetNonZerosIntArrayListIntArrayListIntArrayListFloatArrayList() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        IntArrayList sliceList = new IntArrayList();
	        IntArrayList rowList = new IntArrayList();
	        IntArrayList colList = new IntArrayList();
	        FloatArrayList valueList = new FloatArrayList();
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            A.getNonZeros(sliceList, rowList, colList, valueList);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                sliceList.clear();
	    	        rowList.clear();
	                colList.clear();
	                valueList.clear();
	                t.reset().start();
	                A.getNonZeros(sliceList, rowList, colList, valueList);
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
            sliceList.clear();
            rowList.clear();
	        colList.clear();
	        valueList.clear();
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            Av.getNonZeros(sliceList, rowList, colList, valueList);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                sliceList.clear();
	                rowList.clear();
	                colList.clear();
	                valueList.clear();
	                t.reset().start();
	                Av.getNonZeros(sliceList, rowList, colList, valueList);
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "getNonZeros(IntArrayList, IntArrayList, IntArrayList, FloatArrayList)";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }
	    
	    @Test
	    public void testGetPositiveValuesIntArrayListIntArrayListIntArrayListFloatArrayList() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        IntArrayList sliceList = new IntArrayList();
	        IntArrayList rowList = new IntArrayList();
	        IntArrayList columnList = new IntArrayList();
	        FloatArrayList valueList = new FloatArrayList();
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

	            // warm-up
	            A.getPositiveValues(sliceList, rowList, columnList, valueList);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                sliceList.clear();
	                rowList.clear();
	                columnList.clear();
	                valueList.clear();
	                t.reset().start();
	                A.getPositiveValues(sliceList, rowList, columnList, valueList);
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
            sliceList.clear();
            rowList.clear();
            columnList.clear();
            valueList.clear();
	        FloatMatrix3D Av = new DenseFloatMatrix3D(a_3d).viewDice(2, 1, 0);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

	            // warm-up
	            Av.getPositiveValues(sliceList, rowList, columnList, valueList);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                sliceList.clear();
	                rowList.clear();
	                columnList.clear();
	                valueList.clear();
	                t.reset().start();
	                Av.getPositiveValues(sliceList, rowList, columnList, valueList);
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "getPositiveValues(IntArrayList, IntArrayList, IntArrayList, FloatArrayList)";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }
	    
	    @Test
	    public void testIdct3() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            A.idct3(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                A.assign(a_3d);
	                t.reset().start();
	                A.idct3(true);
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            Av.idct3(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                Av.assign(a_3d);
	                t.reset().start();
	                Av.idct3(true);
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "idct3(boolean)";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }

	    @Test
	    public void testIdct2Slices() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            A.idct2Slices(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                A.assign(a_3d);
	                t.reset().start();
	                A.idct2Slices(true);
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	    	for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            Av.idct2Slices(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                Av.assign(a_3d);
	                t.reset().start();
	                Av.idct2Slices(true);
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "idct2Slices(boolean)";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }
	    
	    @Test
	    public void testIdst3() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            A.idst3(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                A.assign(a_3d);
	                t.reset().start();
	                A.idst3(true);
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            Av.idst3(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                Av.assign(a_3d);
	                t.reset().start();
	                Av.idst3(true);
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "idst3(boolean)";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }

	    @Test
	    public void testIdst2Slices() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            A.idst2Slices(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                A.assign(a_3d);
	                t.reset().start();
	                A.idst2Slices(true);
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	    	for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            Av.idst2Slices(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                Av.assign(a_3d);
	                t.reset().start();
	                Av.idst2Slices(true);
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "idst2Slices(boolean)";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }
	    
	    @Test
	    public void testIdht3() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            A.idht3(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                A.assign(a_3d);
	                t.reset().start();
	                A.idht3(true);
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            Av.idht3(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                Av.assign(a_3d);
	                t.reset().start();
	                Av.idht3(true);
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "idht3(boolean)";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }

	    @Test
	    public void testIdht2Slices() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            A.idht2Slices(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                A.assign(a_3d);
	                t.reset().start();
	                A.idht2Slices(true);
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	    	for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            Av.idht2Slices(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                Av.assign(a_3d);
	                t.reset().start();
	                Av.idht2Slices(true);
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "idht2Slices(boolean)";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }
	    
	    @Test
	    public void testIfft3() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            A.ifft3(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                A.assign(a_3d);
	                t.reset().start();
	                A.ifft3(true);
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);
	            // warm-up
	            Av.ifft3(true);
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                Av.assign(a_3d);
	                t.reset().start();
	                Av.ifft3(true);
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "ifft3(boolean)";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }

	    @Test
	    public void testMaxLocation() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

	            // warm-up
	            float[] maxAndLoc = A.getMaxLocation();
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                t.reset().start();
	                maxAndLoc = A.getMaxLocation();
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

	            // warm-up
	            float[] maxAndLoc = Av.getMaxLocation();
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                t.reset().start();
	                maxAndLoc = Av.getMaxLocation();
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "maxLocation()";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }
	   
	    @Test
	    public void testMinLocation() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

	            // warm-up
	            float[] minAndLoc = A.getMinLocation();
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                t.reset().start();
	                minAndLoc = A.getMinLocation();
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

	            // warm-up
	            float[] minAndLoc = Av.getMinLocation();
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                t.reset().start();
	                minAndLoc = Av.getMinLocation();
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "minLocation()";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }
	    
	    @Test
	    public void testSum() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

	            // warm-up
	            float aSum = A.zSum();
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                t.reset().start();
	                aSum = A.zSum();
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

	            // warm-up
	            float aSum = Av.zSum();
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                t.reset().start();
	                aSum = Av.zSum();
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "zSum()";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }

	    @Test
	    public void testToArray() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

	            // warm-up
	            float[][][] array = A.toArray();
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                t.reset().start();
	                array = A.toArray();
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

	            // warm-up
	            float[][][] array = Av.toArray();
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                t.reset().start();
	                array = Av.toArray();
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "toArray()";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }

	    @Test
	    public void testVectorize() {
	        /* No view */
	        FloatMatrix3D A = new DenseFloatMatrix3D(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

	            // warm-up
	            FloatMatrix1D B = A.vectorize();
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                t.reset().start();
	                B = A.vectorize();
	                t.stop();
	                noViewTimes[i] += t.millis();
	            }
	            noViewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        /* View */
	        FloatMatrix3D Av = new DenseFloatMatrix3D(BenchmarkMatrixKernel.MATRIX_SIZE_3D[2], BenchmarkMatrixKernel.MATRIX_SIZE_3D[1], BenchmarkMatrixKernel.MATRIX_SIZE_3D[0]).viewDice(2, 1, 0).assign(a_3d);
	        for (int i = 0; i < BenchmarkMatrixKernel.NTHREADS.length; i++) {
	            ConcurrencyUtils.setNumberOfProcessors(BenchmarkMatrixKernel.NTHREADS[i]);

	            // warm-up
	            FloatMatrix1D B = Av.vectorize();
	            for (int j = 0; j < BenchmarkMatrixKernel.NITERS; j++) {
	                t.reset().start();
	                B = Av.vectorize();
	                t.stop();
	                viewTimes[i] += t.millis();
	            }
	            viewTimes[i] /= BenchmarkMatrixKernel.NITERS;
	        }
	        String method = "vectorize()";
	        BenchmarkMatrixKernel.writeMatrixBenchmarkResultsToFile(outputFile, method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);
	        BenchmarkMatrixKernel.displayMatrixBenchmarkResults(method, BenchmarkMatrixKernel.NTHREADS, noViewTimes, viewTimes);

	    }
}
