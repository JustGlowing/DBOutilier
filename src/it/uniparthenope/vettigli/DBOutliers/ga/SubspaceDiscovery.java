package it.uniparthenope.vettigli.DBOutliers.ga;

import it.uniparthenope.vettigli.DBOutliers.DBOutliers;
import it.uniparthenope.vettigli.DBOutliers.gui.SimpleTextFrame;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;
import weka.core.Instances;

/**
 * Discovers a subspace where user example outliers are more significant
 * using a Genetic Algorithm. For more detail on user examples innesting
 * see the DBOutliers documentation.
 * 
 * @author Giuseppe Vettigli
 */
public class SubspaceDiscovery {
    private DBOutliers dbo;
    private SimpleTextFrame gui;

    /**
     * Create a new SubspaceDiscovery
     */
    public SubspaceDiscovery(Instances data) {
        dbo = new DBOutliers(data);
    }

    /**
     * Create a new SubspaceDiscovery
     * Gui is initialized but not visible, for make it visible use showGUI()
     * @param g is the number of samples to compute the mean number of neighbors for the normal data, default is 3
     */
    public SubspaceDiscovery(Instances data, int g) {
        dbo = new DBOutliers(data);
        dbo.set_g(g);
        gui = new SimpleTextFrame("Genetic Algorithm running...");
    }

    /**
     * Create a new SubspaceDiscovery workin on the dataset located in source
     * @param source is a strig with the dataset file location
     */
    public SubspaceDiscovery(String source) throws Exception {
        dbo = new DBOutliers(source);
    }

    /**
     * Shows a gui with algorithm steps
     */
    public void showGUI() {
        gui.show();
    }

    /**
     * Returns a subspace where user examples are more significant
     * @param t treshold for the fitness value
     * @param p_mut the mutation probability will be 1/p_mut
     */
    public Individual Discovery(double t, int p_mut) {
        int n = 10; /* starting population size */
        int chrom = dbo.numAttributes()-1;
        Vector<Individual> population = Individual.generatePopulation(chrom, n);
        for(Individual i : population)
           fitness(i);

        removeTwins(population); /* Twins can stuck the reproduction */
        
        Random rand = new Random();
        Individual a = null, b = null ,son = null;
        int step = 0;

        while(true)
        {
            step++;
            /* start selection */
            Collections.sort(population); /* rank by fitness */
            if(population.size() == 1) /* too many twins */
            {
                a = population.get(population.size()-1);
                b = Individual.generateRandomIndividual(chrom);
            }
            else if(population.size() == 0) /* too many twins again */
            {
                a = Individual.generateRandomIndividual(chrom);
                b = Individual.generateRandomIndividual(chrom);
            }
            else
            {
                a = population.get(population.size()-1);
                b = population.get(population.size()-2);
            } /* end selection */
            population = new Vector<Individual>();

            for(int i=0; i<n-2; i++) /* reproductions */
            {
                if(rand.nextInt(p_mut-1) == 1) /* mutation rate 1/p_mut */
                {
                    mutation(a);
                    mutation(b);
                }
                else /* crossover rate 1-(1/p_mut) */
                {
                    son = crossover(a, b);
                    fitness(son);
                    population.add(son);
                }

                /* if there is an individual with fitness > t it's the best */
                for(Individual k : population)
                {
                    if(k.getFitness() >= t)
                    {
                        gui.addText("The best is "+k+" in "+step+" GA steps");
                        return k;
                    }
                }

                removeTwins(population);
            }            
        }
    }

    /**
     * Remove the twins in population
     */
    private void removeTwins(Vector<Individual> population) {
        for(int i=0; i<population.size(); i++)
        {
            for(int j=0; j<population.size(); j++)
            {
                if(population.get(i).compareChromosomes(population.get(j)) && i != j)
                    population.remove(j);
            }
        }
    }

    /**
     * Returns a son of a and b
     */
    private Individual crossover(Individual a, Individual b) {
        gui.addText("***Crossing***************");
        gui.addText("a: " + a);
        gui.addText("b: " + b);
        int n = a.size();
        fitness(a);
        fitness(b);
        Individual t1 = new Individual(n);
        Individual t2 = new Individual(n);

        for(int i =0; i<n; i++) /* build t1 and t2 */
        {   /* t1(i) is 1 where a(i)=b(i)=1 */
            if(a.getChromosome(i) == true && b.getChromosome(i) == true)
                t1.setChromosome(i, Boolean.TRUE);
            else
                t1.setChromosome(i, Boolean.FALSE);
            /* t2(i) is 1 if a(i)!=b(i) */
            if(a.getChromosome(i) != b.getChromosome(i))
                t2.setChromosome(i, Boolean.TRUE);
            else
                t2.setChromosome(i, Boolean.FALSE);
        }

        Individual s = new Individual(t1.getWeight());
        Individual q = new Individual(n);
        q.copyChromosomes(t1);
        double nowfit = 0.0;
        double oldfit = 0.0;
        int besti = 0;
        int step = 0;

        /* while s is worse than a and b*/
        while(s.getFitness() <= a.getFitness() && s.getFitness() <= b.getFitness())
        {
            step++;

            if(step == 50) /* chromosomes can be incompatible for crossing */
            {              /* return the best parent */
                gui.addText("*Incompatible individual");
                if(a.getFitness()>b.getFitness())
                    return a;
                else
                    return b;
            }

            for(int i =0; i<n; i++)
            {
                if(t2.getChromosome(i) == true)
                {
                    q.setChromosome(i, Boolean.TRUE);

                    nowfit = fitness(q);
                    if(nowfit > oldfit) /* is the best? */
                    {
                        oldfit = nowfit;
                        besti = i;
                    }

                    q.copyChromosomes(t1);
                }
            }

            q.setChromosome(besti, Boolean.TRUE); 
            s.copyChromosomes(q); /* it's the best at this time */
            fitness(s);
            
            t2 = difference(t2,difference(s,t1));
            t1.copyChromosomes(s);
        }

        gui.addText("c: " + s + " in " + step + " cross_steps");
        gui.addText("**************************");
        return s;
    }

    private Individual difference(Individual a, Individual b) {
        int n = a.size();
        Individual result = new Individual(n);
        result.copyChromosomes(a);
        for(int i =0; i<n; i++)
        {
            if(b.getChromosome(i) == true)
                result.setChromosome(i, Boolean.FALSE);
        }

        return result;
    }

    /**
     * Inverts all chromosomes in sub
     */
    private void mutation(Individual sub) {
        gui.addText("****Mutating**************");
        gui.addText("i: "+sub);
        for(int i=0; i<sub.size(); i++)
            sub.invertChromosome(i);
        gui.addText("o: "+sub);
    }

    /**
     * Return the fitness value for the individual sub.
     * The fitness value will be also saved in sub,
     * ( To read the value use sub.getFitness() )
     */
    private double fitness(Individual sub) {
        int q = 0;
        double gap = 0.0;
        double a_no = 0.0;
        int k = sub.getDimensionNum();
        double stepsize = Math.sqrt(k)/20;

        for(double d=0.0; d<Math.sqrt(k); d += stepsize)
        {
            /* gap beetwen meanNeighbors of normal
                data and meanNeighbors of examples */
            gap = dbo.meanNeighbors(d, sub, DBOutliers.NORMAL)
                    - dbo.meanNeighbors(d, sub, DBOutliers.EXAMPLE);
            if(gap > 0)
                a_no += gap;
            q++;
        }

        if(a_no == 0.0)
        {
            sub.setFitness(0.0);
            return 0.0;
        }

        sub.setFitness(a_no/(k*q)); /* save the value in the individual */
        return a_no/(k*q);
    }

    // testing main
    public static void main(String[] args) {
        try {
            SubspaceDiscovery sd =
                    new SubspaceDiscovery("//home//giu//OutliersDetection//withExamples1.arff");
            /*
            // crossover test
            int chrome[] = {1, 0, 1, 0, 1};
            Individual ind = new Individual(chrome);
            int chrome1[] = {0, 0, 0, 0, 0};
            Individual ind1 = new Individual(chrome1);
            sd.crossover(ind,ind1);
             */

            // complete discovery test
            System.out.println("Result: " + sd.Discovery(0.1,10));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}