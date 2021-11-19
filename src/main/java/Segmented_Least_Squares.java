import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

/**
 * created by hansikaweerasena
 * Date: 11/18/21
 **/
public class Segmented_Least_Squares {

    private static int C;
    private static int N;
    private static double[] lookupOPT;
    private static int[] index;
    private static double[][] Eij;
    private static double result;
    private static final int M = 1000;


    private static void writeToCSV(String filename, String key, String value) {

        String eol = System.getProperty("line.separator");

        try (Writer writer = new FileWriter(filename, true)) {
            writer.append(key)
                    .append(';')
                    .append(value)
                    .append(eol);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }


    private static ArrayList<Point2D> generatePoints(int noOfPoints) {

        ArrayList<Point2D> pointList = new ArrayList<>();
        for (int i = 0; i < noOfPoints; i++) {
            int x = ThreadLocalRandom.current().nextInt(1, M + 1);
            double y = ThreadLocalRandom.current().nextDouble(1, 1000000);
            pointList.add(new Point2D(x, y));
        }

        N = pointList.size();
        lookupOPT = new double[N + 1];
        index = new int[N + 1];
        Eij = new double[N + 1][N + 1];
        return pointList;
    }

    private static void computeOPT(List<Point2D> pointList) {
        computeEij(pointList);
        lookupOPT[0] = 0;
        for (int j = 1; j <= N; j++) {
            double min = Double.POSITIVE_INFINITY;
            int inx = 0;
            for (int i = 1; i <= j; i++) {
                double tmp = Eij[i][j] + lookupOPT[i - 1] + C;
                if (tmp < min) {
                    min = tmp;
                    inx = i;
                }
            }
            lookupOPT[j] = min;
            index[j] = inx;
        }
        result = lookupOPT[N];
        stInterval();
    }

    private static void computeEij(List<Point2D> pointList) {
        for (int j = 1; j <= N; j++) {
            for (int i = 1; i <= j; i++) {
                if (i == j) {
                    Eij[i][j] = Double.POSITIVE_INFINITY;
                } else {
                    Eij[i][j] = computeError(pointList, i, j);
                }
            }
        }
    }

    private static double computeError(List<Point2D> pointList, int i, int j) {
        double val = 0;
        int n = j - i + 1;
        double b = 0;
        double sumY = 0;

        for (int k = i; k < j; k++) {
            double Y = pointList.get(k).getY();
            sumY += Y;
        }

        b = sumY / n;

        for (int k = i; k <= j; k++) {
            double Y = pointList.get(k - 1).getY();
            double tmp = Math.pow(Y - b, 2);
            val += tmp * tmp;
        }

        return val;
    }

    private static void stInterval() {
        Stack<Integer> st = new Stack<>();
        for (int endIndex = N, startIndex = index[N]; endIndex > 0; endIndex = startIndex - 1, startIndex = index[endIndex]) {
            st.push(endIndex);
            st.push(startIndex);
        }
        printAll(st);
    }

    private static void printAll(Stack<Integer> st) {
        System.out.println("COST: " + result);
        System.out.println("No of intervals: " + st.size() / 2);
        int interval_no = 1;
        while (!st.isEmpty()) {
            int start = st.pop();
            int end = st.pop();
            System.out.println("Interval " + interval_no + ": " + " No of Points :" + (end - start + 1));
            interval_no++;
        }
        System.out.println("------------------------------------------------------");
    }

    private static void runExperiments() {

        //Simple test case
        List<Point2D> pointList = new ArrayList<>();

        pointList.add(new Point2D(1, 2));
        pointList.add(new Point2D(3, 6));
        pointList.add(new Point2D(2, 4));

        pointList.add(new Point2D(4, 12));
        pointList.add(new Point2D(5, 15));
        pointList.add(new Point2D(6, 18));

        N = pointList.size();
        lookupOPT = new double[N + 1];
        index = new int[N + 1];
        Eij = new double[N + 1][N + 1];
        computeOPT(pointList);

        for (int i = 0; i <= 5000; i += 100) {
            pointList = generatePoints(i);
            long startTime = System.currentTimeMillis();
            long beforeUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            computeOPT(pointList);
            long afterUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long actualMemUsed = afterUsedMem - beforeUsedMem;
            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime);
            lookupOPT = new double[0];
            index = new int[0];
            Eij = new double[0][0];
            writeToCSV("time.csv", String.valueOf(i), String.valueOf(duration));
            writeToCSV("memory.csv", String.valueOf(i), String.valueOf(actualMemUsed));
        }
    }

    public static void main(String[] args) {

        if (args.length > 0) {
            try {
                C = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                C = 1;
            }
        }

        runExperiments();
    }
}

class Point2D {
    private int X;
    private double Y;

    public Point2D(int x, double y) {
        X = x;
        Y = y;
    }

    public int getX() {
        return X;
    }

    public void setX(int x) {
        X = x;
    }

    public double getY() {
        return Y;
    }

    public void setY(double y) {
        Y = y;
    }
}
