package edu.tulane.cs.hetml.mSpRL.Eval;

/**
 * Created by taher on 2017-03-05.
 */
public class ExactComparer implements EvalComparer {

    @Override
    public boolean isEqual(SpRLEval a, SpRLEval b) {
        if (a == null && b == null)
            return true;
        if (a == null || b == null)
            return false;
        return a.isEqual(b);
    }
}
