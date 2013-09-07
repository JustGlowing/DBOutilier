package weka.filters;

/**
 *
 * @author Giuseppe Vettigli
 */
import it.uniparthenope.vettigli.DBOutliers.ga.Individual;
import it.uniparthenope.vettigli.DBOutliers.ga.SubspaceDiscovery;
import weka.core.*;
import weka.core.Capabilities.*;
public class SubspaceDiscoveryGA extends Filter {
  private int g = 3;
  private double fitness_treshold = 0.1;
  private int mutation_prob = 10;

  public String globalInfo() {
    return   "Discover a subspace where user example outliers are more significant" +
             "using a Genetic Algorithm. The other subspace will be removed.\n" +
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
    result.enable(Capability.NO_CLASS); // filter doesn’t need class to be set
    return result;
  }

  public boolean batchFinished() throws Exception {
    if (getInputFormat() == null)
      throw new NullPointerException("No input instance format defined");

    Instances inst = getInputFormat();
    Individual sub = null;
    int removed = 0;

    if(inst.attribute(inst.numAttributes()-1).name().compareTo("isuserexample") != 0)
        throw new NullPointerException("No user examples defined");
    /* Execute GA and remove attributes */
    if (!isFirstBatchDone()) {
        SubspaceDiscovery sd = new SubspaceDiscovery(inst,g);
        sd.showGUI();
        sub = sd.Discovery(fitness_treshold, getMutation_prob());

        //int chrome[] = {1, 1, 0, 1, 0};
        //sub = new Individual(chrome);

        Instances outFormat = new Instances(getInputFormat(), 0);
        removed = 0;
        for(int i=0; i<sub.size(); i++)
        {
            if(sub.getChromosome(i) == false)
            {
                outFormat.deleteAttributeAt(i-removed);
                removed++;
            }
        }
      setOutputFormat(outFormat);
    }

    /* copy original value */    
    Instances outFormat = getOutputFormat();
    for (int i = 0; i < inst.numInstances(); i++) {
      double[] newValues = new double[outFormat.numAttributes()];
      double[] oldValues = inst.instance(i).toDoubleArray();
      removed = 0;
      for(int j=0; j<sub.size(); j++)
      {
          if(sub.getChromosome(j) == true)          
              newValues[j-removed] = oldValues[j];          
          else
              removed++;          
      }
      // user examples assignment
      newValues[newValues.length-1] = oldValues[oldValues.length-1];
      push(new Instance(1.0, newValues));
    }

    flushInput();
    m_NewBatch = true;
    m_FirstBatchDone = true;
    return (numPendingOutput() != 0);
  }

  public static void main(String[] args) {
    runFilter(new SubspaceDiscoveryGA(), args);
  }

    /**
     * @return the g
     */
    public int getG() {
        return g;
    }

    /**
     * @param g the g to set
     */
    public void setG(int g) {
        this.g = g;
    }

    public String gTipText() {
        return "g is the number of samples for compute the mean " +
                "number of neighbors for the normal data";
    }

    /**
     * @return the fitness_treshold
     */
    public double getFitness_treshold() {
        return fitness_treshold;
    }

    /**
     * @param fitness_treshold the fitness_treshold to set
     */
    public void setFitness_treshold(double fitness_treshold) {
        this.fitness_treshold = fitness_treshold;
    }

    public String fitness_tresholdTipText( ) {
        return "treshold for the the fitness in the Genetic Algorithm";
    }

    /**
     * @return the mutation_prob
     */
    public int getMutation_prob() {
        return mutation_prob;
    }

    /**
     * @param mutation_prob the mutation_prob to set
     */
    public void setMutation_prob(int mutation_prob) {
        this.mutation_prob = mutation_prob;
    }

    public String mutation_probTipText() {
        return "the mutation probability in the Genetic Algorithm will be 1/mutation_prob_mut";
    }
}

