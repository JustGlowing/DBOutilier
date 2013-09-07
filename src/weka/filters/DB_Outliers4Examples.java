package weka.filters;

import it.uniparthenope.vettigli.DBOutliers.DBOutliers;
import it.uniparthenope.vettigli.DBOutliers.ga.Individual;
import java.util.Vector;
import weka.core.*;
import weka.core.Capabilities.*;

public class DB_Outliers4Examples extends SimpleBatchFilter {
  public String globalInfo() {
    return   "Distance-Based Outlier detection filter, this filter detect outliers tring various parameter (p,d) " +
             "computed using user example outliers.\n" +
             "The instances not labeled as outlier will be removed.\n\n" +
             //"An object O in a dataset T is a DB(p,d)-outlier" +
             //"if at least fraction p of the objects in T lie greater than distance d from O.\n" +
             "For user example the last attribute of the data must specify if the instance" +
             "is user example or normale example. User data must be specified with the second" +
             "possible value and normal data with the first. Example:\n\n" +
             " * ARFF file example: \n @attribute a0 numeric \n @attribute a1 numeric " +
             "\n @attribute a2 numeric \n @attribute a3 numeric \n @attribute a4 numeric " +
             "\n @attribute isuserexample {normal,example}" +
             "\n\nThe data must be normalized in [0 1]" +
             "\n\nGiuseppe Vettigli";
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
    result.insertAttributeAt(new Attribute("outlierness",Attribute.STRING), result.numAttributes());
    return result;
  }

  protected Instances process(Instances inst) {
    Instances result = new Instances(determineOutputFormat(inst), 0);
    DBOutliers dbo = new DBOutliers(inst);

    Individual sub = new Individual(result.numAttributes());
    //int chrome[] = {1, 1, 0, 0, 0};
    //Individual sub = new Individual(chrome);

    for(int i=0; i<sub.size(); i++)
        sub.invertChromosome(i);

    Vector<Instance> outliers = dbo.detectOutliers(sub);
    
    int[] ow = new int[outliers.size()];
    for(int i=0; i<outliers.size(); i++) /* computing outlaierness */
    {
        for(int j=0; j<outliers.size(); j++)
        {
            if(dbo.compareInstance(outliers.get(i), outliers.get(j)) && i != j)
                ow[i] += 1;
        }
    }

    /* old instances */
    /*
    for (int j = 0; j < inst.numInstances(); j++) {
      double[] oldValues = inst.instance(j).toDoubleArray();
      double[] newValues = new double[inst.numAttributes() + 1];
      for (int i = 0; i < inst.numAttributes(); i++)
        newValues[i] = oldValues[i];
      newValues[newValues.length - 1] = 0;
      result.add(new Instance(1, newValues));
    }*/

    /* new dataset with outliers only */
    int j = 0;
    for (Instance myist : outliers) {
        double[] oldValues = myist.toDoubleArray();
        double[] newValues = new double[inst.numAttributes() + 1];
        for (int i = 0; i < inst.numAttributes(); i++)
            newValues[i] = oldValues[i];
        newValues[newValues.length - 1] = ow[j];
        result.add(new Instance(1, newValues));
        j++;
    }

    return result;
  }

  public static void main(String[] args) {
    runFilter(new DB_Outliers4Examples(), args);
  }
}
