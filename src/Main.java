
import weka.gui.explorer.Explorer;

/*
 * java -jar OutliersDetection.jar
 *
 * Linux
 *  java -Xmx128m -classpath $CLASSPATH:weka.jar weka.gui.Main
 * Win32
 *  java -Xmx128m -classpath "%CLASSPATH%;weka.jar" weka.gui.Main
 */
public class Main {

    public static void main(String[] args) {
        //weka.gui.GUIChooser.main(null);
        weka.gui.explorer.Explorer.main(null);

        //DBOutliers dbo = new DBOutliers("//home//giu//OutliersDetection//1centroide5.arff");
        //double numero[] = {0.733036,0.413403,0.0,0.0,0.0};
        //Instance i = new Instance(1.0, numero);
    }

}
