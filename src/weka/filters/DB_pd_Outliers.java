package weka.filters;

import it.uniparthenope.vettigli.DBOutliers.DBOutliers;
import it.uniparthenope.vettigli.DBOutliers.ga.Individual;
import java.util.Vector;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;


public class DB_pd_Outliers extends SimpleBatchFilter {

    private double d = 0.2;
    private double p = 0.6;

    public String globalInfo() {
        return "Distance-Based (p,d)Outlier detection filter, the instances not labeled as outlier will be removed.\n\n"
                + "An object O in a dataset T is a DB(p,d)-outlier"
                + "if at least fraction p of the objects in T lie greater than distance d from O.\n"
                + "\n\nGiuseppe Vettigli";
    }

    public Capabilities getCapabilities() {
        Capabilities result = super.getCapabilities();
        result.enableAllAttributes();
        result.enableAllClasses();
        result.enable(Capability.NO_CLASS); //// filter doesn’t need class to be set//
        return result;
    }

    protected Instances determineOutputFormat(Instances inputFormat) {
        Instances result = new Instances(inputFormat, 0);
        return result;
    }

    protected Instances process(Instances inst) {
        Instances result = new Instances(determineOutputFormat(inst), 0);
        DBOutliers dbo = new DBOutliers(inst);
        Individual sub = new Individual(result.numAttributes());
        for (int i = 0; i < sub.size(); i++) {
            sub.invertChromosome(i); /* all the subspaces are considered */
        }
        Vector<Instance> outliers = dbo.detectOutliers(p, d, sub);

        /* new dataset with outliers only */
        for (Instance myist : outliers) {
            result.add(new Instance(1, myist.toDoubleArray()));
        }

        return result;
    }

    public static void main(String[] args) {
        runFilter(new DB_Outliers4Examples(), args);
    }

    /**
     * @return the d
     */
    public double getD() {
        return d;
    }

    /**
     * @param d the d to set
     */
    public void setD(double d) {
        this.d = d;
    }

    /**
     * @return the p
     */
    public double getP() {
        return p;
    }

    /**
     * @param p the p to set
     */
    public void setP(double p) {
        this.p = p;
    }

    public String pTipText() {
        return "must be in [0 1], 0->0% ... 0.5->50% ... 1->100%";
    }
}
