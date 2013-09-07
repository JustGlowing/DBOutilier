package it.uniparthenope.vettigli.DBOutliers.ga;

import java.util.Random;
import java.util.Vector;

/**
 * Genetic Algorithm individual
 * 
 * @author Giuseppe Vettigli
 */
public class Individual  implements Comparable<Individual> {
    private Vector<Boolean> chromosomes;
    private double fitness;

    /**
     * Creates an Individual with n chromosomes, all chromosomes are set to 0.
     */
    public Individual(int n){
        chromosomes = new Vector<Boolean>();
        for(int i=0; i<n; i++)
            chromosomes.add(Boolean.FALSE);
        fitness = 0.0;
    }

    /**
     * Creates an Individual with the specified chromosomes.
     */
    public Individual(Vector<Boolean> chromosomes){
        this.chromosomes = chromosomes;
        fitness = 0.0;
    }

    /**
     * Creates an individual using an integers vector.
     * @param w all component must be 0 or 1
     */
    public Individual(int[] w){
        chromosomes = new Vector<Boolean>();
        
        for(int i=0; i<w.length; i++)
        {
            if(w[i] == 1)
                chromosomes.add(Boolean.TRUE);
            else
                chromosomes.add(Boolean.FALSE);
        }

        fitness =0.0;
    }

    /**
     * Generates an Individual with n random chromosomes.
     */
    static public Individual generateRandomIndividual(int n) {
        Vector<Boolean> chromo = new Vector<Boolean>();
        Random r = new Random();

        for(int i=0; i<n; i++)
        {
            if(r.nextFloat() < 0.5)
                chromo.add(Boolean.TRUE);
            else
                chromo.add(Boolean.FALSE);
        }

        return new Individual(chromo);
    }

    /**
     * Generate a random population of individuals.
     * @param chrom number of chromosomes per individual
     * @param m number of individuals
     * @return a Vector with all the population
     */
    static public Vector<Individual> generatePopulation(int chrom, int m) {
        Vector<Individual> population = new Vector<Individual>();
        
        for(int i=0; i<m; i++)
            population.add(generateRandomIndividual(chrom));

        return population;
    }

    /**
     * Returns an array with chromosomes coded with integers 1 and 0.
     */
    public int[] getWeight() {
        int[] w = new int[chromosomes.size()];

        for(int i=0; i<chromosomes.size(); i++)
        {
            if(chromosomes.get(i) == Boolean.TRUE)
                w[i] = 1;
            else
                w[i] = 0;
        }

        return w;
    }

    /**
     * Inverts the value of chromosome i
     */
    public void invertChromosome(int i) {
        chromosomes.set(i, !chromosomes.get(i));
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public double getFitness() {
        return fitness;
    }

    /**
     * Set the chromosome i at value v
     */
    public void setChromosome(int i,Boolean v) {
        chromosomes.set(i, v);
    }

    /**
     * Return the value of chromosome i
     */
    public boolean getChromosome(int i) {
        return chromosomes.get(i);
    }

    /**
     * Set the all chromosome with the values of w
     */
    public void setAllChromosome(int[] w) {
        chromosomes = new Vector<Boolean>();

        for(int i=0; i<w.length; i++)
        {
            if(w[i] == 1)
                chromosomes.add(Boolean.TRUE);
            else
                chromosomes.add(Boolean.FALSE);
        }
    }

    /**
     * Set the chromosomes as in toCopy
     */
    public void copyChromosomes(Individual toCopy) {
        this.setAllChromosome(toCopy.getWeight());
    }

    /**
     * Return the numer of chromosomes
     */
    public int size() {
        return chromosomes.size();
    }

    /**
     * Returns the number of 1s in the chromosomes vector
     */
    public int getDimensionNum() {
        int k = 0;

        for(Boolean b : chromosomes)
            if(b == Boolean.TRUE)
                k +=1;

        return k;
    }

    /**
     * Returns a string with chromosomes and the fitness value
     * Example 10110 0.123
     */
    public String toString() {
        String s = "";
                for(int i=0; i<chromosomes.size(); i++)
        {
            if(chromosomes.get(i) == Boolean.TRUE)
                s += "1";
            else
                s += "0";
        }

        return s + " "+fitness;
    }

    /**
     * Compares this and o
     * @return true if this and o are equal, false otherwise
     */
    public boolean compareChromosomes(Individual o) {
        for(int i=0; i< chromosomes.size(); i++)
        {
            if(chromosomes.get(i) != o.getChromosome(i))
                return false;
        }

        return true;
    }

    public int compareTo(Individual o) {
        if(this.fitness > o.fitness)
            return 1;
        else if(this.fitness < o.fitness)
            return -1;
        else return 0;
    }
}
