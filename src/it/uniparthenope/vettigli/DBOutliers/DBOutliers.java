package it.uniparthenope.vettigli.DBOutliers;

import it.uniparthenope.vettigli.DBOutliers.ga.Individual;
import it.uniparthenope.vettigli.DBOutliers.gui.SimpleTextFrame;
import java.util.Random;
import java.util.Vector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;


/**
 * Distance-Based Outlier detection tool. An object O in a dataset T is a DB(p,d)-outlier
 * if at least a fraction p of the objects in T lie in a distance d greater than d from O.
 *
 * Supports subspace selection and user examples outliers.
 * 
 * For user examples: the last attribute of the data must specify if the instance
 * is a user example or normale observation.
 *
 * ARFF file example:
 *
 * @attribute a0 numeric
 * @attribute a1 numeric
 * @attribute a2 numeric
 * @attribute a3 numeric
 * @attribute a4 numeric
 * @attribute isuserexample {normal,example}
 *
 * @data
 *  1.136449,0.406764,0.314059,0.245204,0.934104,example
 * 0.217732,0.717597,0.211276,0.34361,0.966326,example
 * 0.339498,0.398419,0.212731,0.327212,0.957366,example
 * 1.290844,0.645635,0.231431,0.330941,0.966143,example
 * ...
 * ...
 * 1.136449,0.486764,0.314059,0.245204,0.934104,normal
 * 0.247732,0.817597,0.211276,0.34361,0.966326,normal
 * 0.369498,0.388419,0.212731,0.327212,0.957366,normal
 * 1.200844,0.615635,0.231431,0.330941,0.966143,normal
 *
 * @author Giuseppe Vettigli
 */
public class DBOutliers {
    static public double NORMAL = 0.0;
    static public double EXAMPLE = 1.0;

    private Instances data;
    private int g;

    /**
     * Creates a new DBOutliers
     */
    public DBOutliers(Instances data) {
        this.data = data;
        g = 3;
    }

    /**
     * Creates a new DBOutliers that works on the dataset located in source
     * @param source a string with a dataset file position
     */
    public DBOutliers(String source) throws Exception {
        data = (new DataSource(source)).getDataSet();
        g = 3;
    }

    /**
     * Detects all the DB(p,d)outliers in the dataset considering the subspace sub
     * at various value of p and d.
     * Data must be normalized in [0,1]
     * @return a vector with all the outliers, duplicated instances have a higer degree of "outlier-ness"
     */
    public Vector<Instance> detectOutliers(Individual sub) {
        int k = sub.getDimensionNum();
        double stepsize = Math.sqrt(k)/10;
        double gap = 0.0;
        double maxgap = 0.0;
        SimpleTextFrame win = new SimpleTextFrame("Outlier Detection...");
        win.show();
        
        for(double d=0.0; d<Math.sqrt(k); d += stepsize)
        {
            gap = meanNeighborsComplete(d, sub)
                    - meanNeighbors(d, sub, DBOutliers.EXAMPLE);

            if(gap > maxgap)
                maxgap = gap;
        }

        Vector<Instance> result = new Vector<Instance>();
        Vector<Instance> localOutliers;
        double N_mn = 0.0;
        for(double d=0.0; d<Math.sqrt(k); d += stepsize)
        {
            N_mn = meanNeighborsComplete(d, sub);
            gap = N_mn - meanNeighbors(d, sub, DBOutliers.EXAMPLE);

            if(gap > maxgap/2)
            {
                localOutliers = detectOutliers(1-(N_mn/data.numInstances()),d,sub);
                for(Instance i : localOutliers)
                        result.add(i);
                win.addText("DB(p="+ (1-N_mn/data.numInstances()) +" d="+d+")outliers detected: "+localOutliers.size());
            }
        }

        return result;
    }

    /*
     * Detects DB(p,d)outliers in the Dataset considering the subspace sub
     *
     * @param p must be in [0 1], (1 all objects, 0.5 50% of all objects...)
     * @return A vector with all the DB(p,d)outliers detected
     */
    public Vector<Instance> detectOutliers(double p, double d, Individual sub) {
        Vector<Instance> outliers = new Vector<Instance>();
        int n = data.numInstances();
        int fraction = (int) Math.round(n*p);
        
        for(int i=0; i<n; i++)
        {   
            if(getNinD(data.instance(i), d, sub) < fraction)
                outliers.add(data.instance(i));
        }

        return outliers;
    }

    public int numAttributes() {
        return data.numAttributes();
    }

    /**
     * Returns the mean number of neighbours at distance d in the subspace sub
     * of the type datatype (DBOutliers.NORMAL data or usere DBOutliers.EXAMPLE data).
     * NOTE: if datatype is NORMAL only g random instance are used.
     * g can be specified using this.set_g(int), default is g = 3
     */
    public double meanNeighbors(double d, Individual sub, double datatype) {
        int n = data.numInstances();
        int s = 0;
        Random rand = new Random();

        if(datatype == DBOutliers.NORMAL)
        {
            for(int i=0; i<g; i++)
                s += getNinD(data.instance(rand.nextInt(n)),d,sub,datatype);
        }
        else
        {
            for(int i=0; i<n; i++)
                s += getNinD(data.instance(i),d,sub,datatype);
        }

        return s/n;
    }

    /**
     * g is the number of samples to compute the mean number of neighbours
     * for the normal data, default is 3
     */
    public void set_g(int g) {
        this.g = g;
    }

    /**
     * Compares two instace a and b
     * @return true if a is equal to b, false otherwise
     */
    public boolean compareInstance(Instance a, Instance b) {
        for(int i=0; i<a.numAttributes()-1; i++)
            if(a.value(i) != b.value(i))
                return false;

        return true;
    }

    /**
     * Returns the number of elements such that the distance from x is smaller than d
     * in the subspace given by sub.
     * @param datatype if it's DBOutliers.NORMAL only normal data will be considered,
     *                 if it's DBOutliers.EXAMPLE only user examples will be considered.
     */
    public int getNinD(Instance x, double d, Individual sub, double datatype) {
        int cnt = 0;

        Instance current;
        for(int i=0; i<data.numInstances(); i++)
        {
            current = data.instance(i);
            if(current.value(current.numAttributes()-1) == datatype) //if it's in datatype
                if(dist(x,current,sub) < d)
                  cnt++;
        }

        return cnt;
    }

    /**
     * Returns the number of elements such that the distance from x is smaller than d
     * considering the subspace sub
     */
    private int getNinD(Instance x, double d, Individual sub) {
        int cnt = 0;
        Instance current;
        for(int i=0; i<data.numInstances(); i++)
        {
            current = data.instance(i);
            if(dist(x,current,sub) < d)
              cnt++;
        }

        return cnt;
    }

     /**
     * Returns the euclidean distance between a and b
     * in the subspace given by sub.
     */
    private double dist(Instance a, Instance b, Individual sub) {
        double s = 0;
        int w[] = sub.getWeight();
        // -1 per non considerare l'atributo classe
        for(int i=0; i<a.numAttributes()-1; i++)
            s += Math.pow(a.value(i) - b.value(i),2)*w[i];

        return Math.sqrt(s);
    }

    /**
     * Returns the mean number of neighbours at distance d in the subspace sub
     * computed over all the instances
     */
    private double meanNeighborsComplete(double d, Individual sub) {
        int n = data.numInstances();
        int s = 0;

        for(int i=0; i<n; i++)
            s += getNinD(data.instance(i),d,sub,DBOutliers.NORMAL);

        return s/n;
    }

    // testing main
    public static void main(String[] args) {
        try {
            // dataset semplice di 5 elementi per provare la detection semplice
            //DBOutliers dbo = new DBOutliers("//home//giu//OutliersDetection//1centroide5.arff");
            // dataset sintetico completo
            DBOutliers dbo = new DBOutliers("//home//giu//OutliersDetection//withExamples1.arff");


            // prove meanNeighbors
            double d = 0.2;

            int chrome[] = {1, 1, 0, 0, 0};
            Individual ind = new Individual(chrome);
            System.out.println("subsace: " + ind);
            System.out.println("media vicini normal:" + dbo.meanNeighbors(d, ind, DBOutliers.NORMAL)
                    + "\nmedia vicini example: " + dbo.meanNeighbors(d, ind, DBOutliers.EXAMPLE));

            int chrome1[] = {1, 1, 1, 1, 1};
            Individual ind1 = new Individual(chrome1);
            System.out.println("\nsubsace: " + ind1);
            System.out.println("media vicini normal:" + dbo.meanNeighbors(d, ind1, DBOutliers.NORMAL)
                    + "\nmedia vicini example: " + dbo.meanNeighbors(d, ind1, DBOutliers.EXAMPLE));


            /*
            // outlier detection singola
            Vector<Instance> o = dbo.detectOutliers(0.5,0.2,ind);
            for(Instance i : o)
                System.out.println(i);
             */

            // prove di outlier detection con a vari parametri
            Vector<Instance> o = dbo.detectOutliers(ind);

            System.out.println("outliers number: "+o.size());
            int[] ow = new int[o.size()];
            for(int i=0; i<o.size(); i++)
            {
                for(int j=0; j<o.size(); j++)
                {
                    if(dbo.compareInstance(o.get(i), o.get(j)) && i != j)
                        ow[i] += 1;
                }
            }

            int j = 0;
            for(Instance i : o)
            {
                System.out.println(i + ",o" + (ow[j]+1));
                j++;
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
