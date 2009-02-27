package cern.colt.matrix.tdouble.algo.solver;

import cern.colt.matrix.tdouble.algo.solver.preconditioner.DoubleSSOR;
import cern.colt.matrix.tdouble.impl.RCDoubleMatrix2D;

/**
 * Test of DoubleIR with SSOR
 */
public class DoubleIRSSORTest extends DoubleIRTest {

    public DoubleIRSSORTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void createSolver() throws Exception {
        super.createSolver();
        double omega = Math.random() + 1;
        M = new DoubleSSOR((RCDoubleMatrix2D) new RCDoubleMatrix2D(A.rows(), A.columns()).assign(A), true, omega, omega);
    }

}
