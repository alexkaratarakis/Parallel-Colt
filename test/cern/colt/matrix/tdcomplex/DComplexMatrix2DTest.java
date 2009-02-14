package cern.colt.matrix.tdcomplex;

import java.util.ArrayList;

import junit.framework.TestCase;
import cern.colt.function.tdcomplex.DComplexProcedure;
import cern.colt.function.tdcomplex.IntIntDComplexFunction;
import cern.colt.list.tint.IntArrayList;
import cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.jet.math.tdcomplex.DComplex;
import cern.jet.math.tdcomplex.DComplexFunctions;
import edu.emory.mathcs.utils.AssertUtils;
import edu.emory.mathcs.utils.ConcurrencyUtils;

public abstract class DComplexMatrix2DTest extends TestCase {
    /**
     * Matrix to test
     */
    protected DComplexMatrix2D A;

    /**
     * Matrix of the same size as A
     */
    protected DComplexMatrix2D B;

    /**
     * Matrix of the size A.columns() x A.rows()
     */
    protected DComplexMatrix2D Bt;

    protected int NROWS = 13;

    protected int NCOLUMNS = 17;

    protected double TOL = 1e-10;

    /**
     * Constructor for DoubleMatrix2DTest
     */
    public DComplexMatrix2DTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        createMatrices();
        populateMatrices();
    }

    protected abstract void createMatrices() throws Exception;

    protected void populateMatrices() {
        ConcurrencyUtils.setThreadsBeginN_2D(1);
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                A.setQuick(r, c, new double[] { Math.random(), Math.random() });
            }
        }

        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                B.setQuick(r, c, new double[] { Math.random(), Math.random() });
            }
        }

        for (int r = 0; r < NCOLUMNS; r++) {
            for (int c = 0; c < NROWS; c++) {
                Bt.setQuick(r, c, new double[] { Math.random(), Math.random() });
            }
        }
    }

    @Override
    protected void tearDown() throws Exception {
        A = B = Bt = null;
    }

    public void testAggregateComplexComplexComplexFunctionComplexComplexFunction() {
        double[] actual = A.aggregate(DComplexFunctions.plus, DComplexFunctions.square);
        double[] expected = new double[2];
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                expected = DComplex.plus(expected, DComplex.square(A.getQuick(r, c)));
            }
        }
        assertEquals(expected, actual, TOL);
    }

    public void testAggregateComplexMatrix2DComplexComplexComplexFunctionComplexComplexComplexFunction() {
        double[] actual = A.aggregate(B, DComplexFunctions.plus, DComplexFunctions.mult);
        double[] expected = new double[2];
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                expected = DComplex.plus(expected, DComplex.mult(A.getQuick(r, c), B.getQuick(r, c)));
            }
        }
        assertEquals(expected, actual, TOL);
    }

    public void testAssignComplexComplexFunction() {
        DComplexMatrix2D Acopy = A.copy();
        A.assign(DComplexFunctions.acos);
        double[] tmp;
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                tmp = DComplex.acos(Acopy.getQuick(r, c));
                assertEquals(tmp, A.getQuick(r, c), TOL);
            }
        }
    }

    public void testAssignComplexMatrix2D() {
        A.assign(B);
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                assertEquals(B.getQuick(r, c), A.getQuick(r, c), TOL);
            }
        }
    }

    public void testAssignComplexMatrix2DComplexComplexComplexFunction() {
        DComplexMatrix2D Acopy = A.copy();
        A.assign(B, DComplexFunctions.div);
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                assertEquals(DComplex.div(Acopy.getQuick(r, c), B.getQuick(r, c)), A.getQuick(r, c), TOL);
            }
        }
    }

    public void testAssignComplexProcedureComplexComplexFunction() {
        DComplexMatrix2D Acopy = A.copy();
        A.assign(new DComplexProcedure() {
            public boolean apply(double[] element) {
                if (DComplex.abs(element) > 3) {
                    return true;
                } else {
                    return false;
                }
            }
        }, DComplexFunctions.tan);
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                if (DComplex.abs(Acopy.getQuick(r, c)) > 3) {
                    assertEquals(DComplex.tan(Acopy.getQuick(r, c)), A.getQuick(r, c), TOL);
                } else {
                    assertEquals(Acopy.getQuick(r, c), A.getQuick(r, c), TOL);
                }
            }
        }
    }

    public void testAssignComplexProcedureDoubleArray() {
        DComplexMatrix2D Acopy = A.copy();
        double[] value = new double[] { Math.random(), Math.random() };
        A.assign(new DComplexProcedure() {
            public boolean apply(double[] element) {
                if (DComplex.abs(element) > 3) {
                    return true;
                } else {
                    return false;
                }
            }
        }, value);
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                if (DComplex.abs(A.getQuick(r, c)) > 3) {
                    assertEquals(value, A.getQuick(r, c), TOL);
                } else {
                    assertEquals(Acopy.getQuick(r, c), A.getQuick(r, c), TOL);
                }
            }
        }
    }

    public void testAssignComplexRealFunction() {
        DComplexMatrix2D Acopy = A.copy();
        A.assign(DComplexFunctions.abs);
        double[] tmp;
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                tmp = A.getQuick(r, c);
                assertEquals(DComplex.abs(Acopy.getQuick(r, c)), tmp[0], TOL);
                assertEquals(0, tmp[1], TOL);
            }
        }
    }

    public void testAssignDoubleArray() {
        double[] expected = new double[2 * A.size()];
        for (int i = 0; i < 2 * A.size(); i++) {
            expected[i] = Math.random();
        }
        A.assign(expected);
        int idx = 0;
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                double[] elem = A.getQuick(r, c);
                assertEquals(expected[idx], elem[0], TOL);
                assertEquals(expected[idx + 1], elem[1], TOL);
                idx += 2;
            }
        }
    }

    public void testAssignDoubleArrayArray() {
        double[][] expected = new double[NROWS][2 * NCOLUMNS];
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < 2 * NCOLUMNS; c++) {
                expected[r][c] = Math.random();
            }
        }
        A.assign(expected);
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                double[] elem = A.getQuick(r, c);
                assertEquals(expected[r][2 * c], elem[0], TOL);
                assertEquals(expected[r][2 * c + 1], elem[1], TOL);
            }
        }
    }

    public void testAssignDoubleDouble() {
        double[] value = new double[] { Math.random(), Math.random() };
        A.assign(value[0], value[1]);
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                double[] elem = A.getQuick(r, c);
                assertEquals(value, elem, TOL);
            }
        }
    }

    public void testAssignFloatArray() {
        float[] expected = new float[NROWS * 2 * NCOLUMNS];
        for (int i = 0; i < 2 * A.size(); i++) {
            expected[i] = (float) Math.random();
        }
        A.assign(expected);
        int idx = 0;
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                double[] elem = A.getQuick(r, c);
                assertEquals(expected[idx], elem[0], TOL);
                assertEquals(expected[idx + 1], elem[1], TOL);
                idx += 2;
            }
        }
    }

    public void testAssignImaginary() {
        DoubleMatrix2D Im = DoubleFactory2D.dense.random(NROWS, NCOLUMNS);
        DComplexMatrix2D Acopy = A.copy();
        A.assignImaginary(Im);
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                assertEquals(Acopy.getQuick(r, c)[0], A.getQuick(r, c)[0], TOL);
                assertEquals(Im.getQuick(r, c), A.getQuick(r, c)[1], TOL);
            }
        }
    }

    public void testAssignReal() {
        DoubleMatrix2D Re = DoubleFactory2D.dense.random(NROWS, NCOLUMNS);
        DComplexMatrix2D Acopy = A.copy();
        A.assignReal(Re);
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                assertEquals(Acopy.getQuick(r, c)[1], A.getQuick(r, c)[1], TOL);
                assertEquals(Re.getQuick(r, c), A.getQuick(r, c)[0], TOL);
            }
        }
    }

    public void testCardinality() {
        int card = A.cardinality();
        assertEquals(A.size(), card);
    }

    public void testEqualsDoubleArray() {
        double[] value = new double[] { Math.random(), Math.random() };
        A.assign(value[0], value[1]);
        boolean eq = A.equals(value);
        assertEquals(true, eq);
        eq = A.equals(new double[] { value[0] + 1, value[1] + 1 });
        assertEquals(false, eq);
    }

    public void testEqualsObject() {
        boolean eq = A.equals(A);
        assertEquals(true, eq);
        eq = A.equals(B);
        assertEquals(false, eq);
    }

    public void testForEachNonZero() {
        DComplexMatrix2D Acopy = A.copy();
        IntIntDComplexFunction function = new IntIntDComplexFunction() {
            public double[] apply(int first, int second, double[] third) {
                return DComplex.sqrt(third);
            }
        };
        A.forEachNonZero(function);
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                assertEquals(DComplex.sqrt(Acopy.getQuick(r, c)), A.getQuick(r, c), TOL);
            }
        }
    }

    public void testGetConjugateTranspose() {
        DComplexMatrix2D Aconj = A.getConjugateTranspose();
        assertEquals(A.rows(), Aconj.columns());
        assertEquals(A.columns(), Aconj.rows());
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                assertEquals(A.getQuick(r, c)[0], Aconj.getQuick(c, r)[0], TOL);
                assertEquals(-A.getQuick(r, c)[1], Aconj.getQuick(c, r)[1], TOL);
            }
        }
    }

    public void testGetImaginaryPart() {
        DoubleMatrix2D Im = A.getImaginaryPart();
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                assertEquals(A.getQuick(r, c)[1], Im.getQuick(r, c), TOL);
            }
        }
    }

    public void testGetNonZeros() {
        IntArrayList rowList = new IntArrayList();
        IntArrayList colList = new IntArrayList();
        ArrayList<double[]> valueList = new ArrayList<double[]>();
        A.getNonZeros(rowList, colList, valueList);
        assertEquals(A.size(), rowList.size());
        assertEquals(A.size(), colList.size());
        assertEquals(A.size(), valueList.size());
        int idx = 0;
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                assertEquals(A.getQuick(rowList.get(idx), colList.get(idx)), valueList.get(idx), TOL);
                idx++;
            }
        }
    }

    public void testGetRealPart() {
        DoubleMatrix2D Re = A.getRealPart();
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                assertEquals(A.getQuick(r, c)[0], Re.getQuick(r, c), TOL);
            }
        }
    }

    public void testToArray() {
        double[][] array = A.toArray();
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                assertEquals(A.getQuick(r, c)[0], array[r][2 * c], TOL);
                assertEquals(A.getQuick(r, c)[1], array[r][2 * c + 1], TOL);
            }
        }
    }

    public void testVectorize() {
        DComplexMatrix1D B = A.vectorize();
        int idx = 0;
        for (int c = 0; c < NCOLUMNS; c++) {
            for (int r = 0; r < NROWS; r++) {
                AssertUtils.assertArrayEquals(A.getQuick(r, c), B.getQuick(idx++), TOL);
            }
        }
    }

    public void testViewColumn() {
        DComplexMatrix1D B = A.viewColumn(NCOLUMNS / 2);
        assertEquals(NROWS, B.size());
        for (int r = 0; r < NROWS; r++) {
            assertEquals(A.getQuick(r, NCOLUMNS / 2), B.getQuick(r), TOL);
        }
    }

    public void testViewColumnFlip() {
        DComplexMatrix2D B = A.viewColumnFlip();
        assertEquals(A.size(), B.size());
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                assertEquals(A.getQuick(r, NCOLUMNS - 1 - c), B.getQuick(r, c), TOL);
            }
        }
    }

    public void testViewDice() {
        DComplexMatrix2D B = A.viewDice();
        assertEquals(A.rows(), B.columns());
        assertEquals(A.columns(), B.rows());
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                assertEquals(A.getQuick(r, c), B.getQuick(c, r), TOL);
            }
        }
    }

    public void testViewPart() {
        DComplexMatrix2D B = A.viewPart(NROWS / 2, NCOLUMNS / 2, NROWS / 3, NCOLUMNS / 3);
        for (int r = 0; r < NROWS / 3; r++) {
            for (int c = 0; c < NCOLUMNS / 3; c++) {
                AssertUtils.assertArrayEquals(A.getQuick(NROWS / 2 + r, NCOLUMNS / 2 + c), B.getQuick(r, c), TOL);
            }
        }
    }

    public void testViewRow() {
        DComplexMatrix1D B = A.viewRow(NROWS / 2);
        assertEquals(NCOLUMNS, B.size());
        for (int c = 0; c < NCOLUMNS; c++) {
            assertEquals(A.getQuick(NROWS / 2, c), B.getQuick(c), TOL);
        }
    }

    public void testViewRowFlip() {
        DComplexMatrix2D B = A.viewRowFlip();
        assertEquals(A.size(), B.size());
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                assertEquals(A.getQuick(NROWS - 1 - r, c), B.getQuick(r, c), TOL);
            }
        }
    }

    public void testViewSelectionComplexMatrix1DProcedure() {
        final double[] value = new double[] { Math.random(), Math.random() };
        A.setQuick(NROWS / 3, 0, value);
        A.setQuick(NROWS / 2, 0, value);
        DComplexMatrix2D B = A.viewSelection(new DComplexMatrix1DProcedure() {
            public boolean apply(DComplexMatrix1D element) {
                return DComplex.isEqual(element.getQuick(0), value, TOL);
            }
        });
        assertEquals(2, B.rows());
        assertEquals(A.columns(), B.columns());
        assertEquals(A.getQuick(NROWS / 3, 0), B.getQuick(0, 0), TOL);
        assertEquals(A.getQuick(NROWS / 2, 0), B.getQuick(1, 0), TOL);
    }

    public void testViewSelectionIntArrayIntArray() {
        int[] rowIndexes = new int[] { NROWS / 6, NROWS / 5, NROWS / 4, NROWS / 3, NROWS / 2 };
        int[] colIndexes = new int[] { NCOLUMNS / 6, NCOLUMNS / 5, NCOLUMNS / 4, NCOLUMNS / 3, NCOLUMNS / 2, NCOLUMNS - 1 };
        DComplexMatrix2D B = A.viewSelection(rowIndexes, colIndexes);
        assertEquals(rowIndexes.length, B.rows());
        assertEquals(colIndexes.length, B.columns());
        for (int r = 0; r < rowIndexes.length; r++) {
            for (int c = 0; c < colIndexes.length; c++) {
                assertEquals(A.getQuick(rowIndexes[r], colIndexes[c]), B.getQuick(r, c), TOL);
            }
        }
    }

    public void testViewStrides() {
        int rowStride = 3;
        int colStride = 5;
        DComplexMatrix2D B = A.viewStrides(rowStride, colStride);
        for (int r = 0; r < B.rows(); r++) {
            for (int c = 0; c < B.columns(); c++) {
                assertEquals(A.getQuick(r * rowStride, c * colStride), B.getQuick(r, c), TOL);
            }
        }
    }

    public void testZMultDComplexMatrix1DDComplexMatrix1DDComplexDComplexBoolean() {
        DComplexMatrix1D y = new DenseDComplexMatrix1D(NCOLUMNS);
        for (int i = 0; i < y.size(); i++) {
            y.setQuick(i, new double[] { Math.random(), Math.random() });
        }
        double[] alpha = new double[] { 3, 2 };
        double[] beta = new double[] { 5, 4 };
        DComplexMatrix1D z = null;
        z = A.zMult(y, z, alpha, beta, false);
        double[] expected = new double[2 * NROWS];
        double[] tmp = new double[2];
        for (int r = 0; r < NROWS; r++) {
            double[] s = new double[2];
            for (int c = 0; c < NCOLUMNS; c++) {
                s = DComplex.plus(s, DComplex.mult(A.getQuick(r, c), y.getQuick(c)));
            }
            tmp[0] = expected[2 * r];
            tmp[1] = expected[2 * r + 1];
            tmp = DComplex.mult(beta, tmp);
            tmp = DComplex.plus(tmp, DComplex.mult(alpha, s));
            expected[2 * r] = tmp[0];
            expected[2 * r + 1] = tmp[1];
        }

        for (int r = 0; r < NROWS; r++) {
            assertEquals(expected[2 * r], z.getQuick(r)[0], TOL);
            assertEquals(expected[2 * r + 1], z.getQuick(r)[1], TOL);
        }
        //transpose
        y = new DenseDComplexMatrix1D(NROWS);
        for (int i = 0; i < y.size(); i++) {
            y.setQuick(i, new double[] { Math.random(), Math.random() });
        }
        z = null;
        z = A.zMult(y, z, alpha, beta, true);
        expected = new double[2 * NCOLUMNS];
        for (int r = 0; r < NCOLUMNS; r++) {
            double[] s = new double[2];
            for (int c = 0; c < NROWS; c++) {
                s = DComplex.plus(s, DComplex.mult(DComplex.conj(A.getQuick(c, r)), y.getQuick(c)));
            }
            tmp[0] = expected[2 * r];
            tmp[1] = expected[2 * r + 1];
            tmp = DComplex.mult(beta, tmp);
            tmp = DComplex.plus(tmp, DComplex.mult(alpha, s));
            expected[2 * r] = tmp[0];
            expected[2 * r + 1] = tmp[1];
        }
        for (int r = 0; r < NCOLUMNS; r++) {
            assertEquals(expected[2 * r], z.getQuick(r)[0], TOL);
            assertEquals(expected[2 * r + 1], z.getQuick(r)[1], TOL);
        }
    }
    
    public void testZMultDoubleMatrix2DDoubleMatrix2DDoubleDoubleBooleanBoolean() {
        double[] alpha = new double[] { 3, 2 };
        double[] beta = new double[] { 5, 4 };
        double[] tmp = new double[2];
        DComplexMatrix2D C = null;
        C = A.zMult(Bt, C, alpha, beta, false, false);
        double[][] expected = new double[NROWS][2 * NROWS];
        for (int j = 0; j < NROWS; j++) {
            for (int i = 0; i < NROWS; i++) {
                double[] s = new double[2];
                for (int k = 0; k < NCOLUMNS; k++) {
                    s = DComplex.plus(s, DComplex.mult(A.getQuick(i, k), Bt.getQuick(k, j)));
                }
                tmp[0] = expected[i][2 * j];
                tmp[1] = expected[i][2 * j + 1];
                tmp = DComplex.mult(tmp, beta);
                tmp = DComplex.plus(tmp, DComplex.mult(s, alpha));
                expected[i][2 * j] = tmp[0];
                expected[i][2 * j + 1] = tmp[1];
            }
        }
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NROWS; c++) {
                assertEquals(expected[r][2 * c], C.getQuick(r, c)[0], TOL);
                assertEquals(expected[r][2 * c + 1], C.getQuick(r, c)[1], TOL);
            }
        }
        
        //transposeA
        C = null;
        C = A.zMult(B, C, alpha, beta, true, false);
        expected = new double[NCOLUMNS][2 * NCOLUMNS];
        for (int j = 0; j < NCOLUMNS; j++) {
            for (int i = 0; i < NCOLUMNS; i++) {
                double[] s = new double[2];
                for (int k = 0; k < NROWS; k++) {
                    s = DComplex.plus(s, DComplex.mult(DComplex.conj(A.getQuick(k, i)), B.getQuick(k, j)));
                }
                tmp[0] = expected[i][2 * j];
                tmp[1] = expected[i][2 * j + 1];
                tmp = DComplex.mult(tmp, beta);
                tmp = DComplex.plus(tmp, DComplex.mult(s, alpha));
                expected[i][2 * j] = tmp[0];
                expected[i][2 * j + 1] = tmp[1];
            }
        }
        for (int r = 0; r < NCOLUMNS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                assertEquals(expected[r][2 * c], C.getQuick(r, c)[0], TOL);
                assertEquals(expected[r][2 * c + 1], C.getQuick(r, c)[1], TOL);
            }
        }
        //transposeB
        C = null;
        C = A.zMult(B, C, alpha, beta, false, true);
        expected = new double[NROWS][2 * NROWS];
        for (int j = 0; j < NROWS; j++) {
            for (int i = 0; i < NROWS; i++) {
                double[] s = new double[2];
                for (int k = 0; k < NCOLUMNS; k++) {
                    s = DComplex.plus(s, DComplex.mult(A.getQuick(i, k), DComplex.conj(B.getQuick(j, k))));
                }
                tmp[0] = expected[i][2 * j];
                tmp[1] = expected[i][2 * j + 1];
                tmp = DComplex.mult(tmp, beta);
                tmp = DComplex.plus(tmp, DComplex.mult(s, alpha));
                expected[i][2 * j] = tmp[0];
                expected[i][2 * j + 1] = tmp[1];
            }
        }
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NROWS; c++) {
                assertEquals(expected[r][2 * c], C.getQuick(r, c)[0], TOL);
                assertEquals(expected[r][2 * c + 1], C.getQuick(r, c)[1], TOL);
            }
        }
        //transposeA and transposeB
        C = null;
        C = A.zMult(Bt, C, alpha, beta, true, true);
        expected = new double[NCOLUMNS][2 * NCOLUMNS];
        for (int j = 0; j < NCOLUMNS; j++) {
            for (int i = 0; i < NCOLUMNS; i++) {
                double[] s = new double[2];
                for (int k = 0; k < NROWS; k++) {
                    s = DComplex.plus(s, DComplex.mult(DComplex.conj(A.getQuick(k, i)), DComplex.conj(Bt.getQuick(j, k))));
                }
                tmp[0] = expected[i][2 * j];
                tmp[1] = expected[i][2 * j + 1];
                tmp = DComplex.mult(tmp, beta);
                tmp = DComplex.plus(tmp, DComplex.mult(s, alpha));
                expected[i][2 * j] = tmp[0];
                expected[i][2 * j + 1] = tmp[1];
            }
        }
        for (int r = 0; r < NCOLUMNS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                assertEquals(expected[r][2 * c], C.getQuick(r, c)[0], TOL);
                assertEquals(expected[r][2 * c + 1], C.getQuick(r, c)[1], TOL);
            }
        }

    }
    

    public void testZSum() {
        double[] actual = A.zSum();
        double[] expected = new double[2];
        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                expected = DComplex.plus(expected, A.getQuick(r, c));
            }
        }
        assertEquals(expected, actual, TOL);
    }

    protected void assertEquals(double[] expected, double[] actual, double tol) {
        for (int i = 0; i < actual.length; i++) {
            assertEquals(expected[i], actual[i], tol);
        }
    }

}
