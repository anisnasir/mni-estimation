import java.io.BufferedReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import com.beust.jcommander.JCommander;
import fullydynamic.FullyDynamicSubgraphReservoirFourNode;
import fullydynamic.FullyDynamicExhaustiveCountingFourNode;
import gnu.trove.map.hash.THashMap;
import fullydynamic.FullyDynamicEdgeReservoirFourNode;

import java.io.BufferedWriter;
import java.io.FileWriter;

import incremental.IncrementalExhaustiveCountingFourNode;
import incremental.IncrementalSubgraphReservoirFourNode;
import incremental.IncrementalEdgeReservoirFourNode;
import input.InputParameters;
import input.StreamEdge;
import input.StreamEdgeReader;
import slidingwindow.FixedSizeSlidingWindow;
import struct.NodeMap;
import topkgraphpattern.Pattern;
import topkgraphpattern.TopkGraphPatterns;
import utility.EdgeHandler;

/*
 * The main class to run different algorithms
 * Created By: Anis Nasir
 * Created on: 18 Oct 2018
 * Updated on: 18 Oct 2018
 */

/**
 * @author Anis
 * <p>
 * main method to compare different algorithm Input Parameter:
 * simulatorType: integer directory: string (input directory) fileName:
 * string (input file in the form of edge list) windowSize: integer (for
 * sliding window) epsilon: parameter to calculate size of the subgraph
 * reservoir delta: parameter to calculate the size of the subgraph
 * reservoir Tk: paramreter to calculate the size of the subgraph
 * reservoir k: integer (parameter for the top-k algorithm)
 */

public class main {
    public static void main(String args[]) throws IOException {
        InputParameters input = new InputParameters();
        JCommander.newBuilder().addObject(input).build().parse(args);

        System.out.println("simulator type: " + input.getSimulatorType() + " window size: " + input.getWindowSize() + " epsilon: " + input.getEpsilon()
                + " delta: " + input.getDelta() + " Tk: " + input.getTk() + "k: " + input.getK());

        String sep = ",";
        String inFileName = input.getDirectory() + input.getInputFileName();

        // input file reader
        BufferedReader in = null;

        try {
            InputStream rawin = new FileInputStream(inFileName);
            if (inFileName.endsWith(".gz"))
                rawin = new GZIPInputStream(rawin);
            in = new BufferedReader(new InputStreamReader(rawin));
        } catch (FileNotFoundException e) {
            System.err.println("File not found");
            e.printStackTrace();
            System.exit(1);
        }

        StreamEdgeReader reader = new StreamEdgeReader(in, sep);
        StreamEdge edge = reader.nextItem();
        FixedSizeSlidingWindow sw = new FixedSizeSlidingWindow(input.getWindowSize());

        // declare object of the algorithm interface
        TopkGraphPatterns topkGraphPattern = null;
        long startTime = System.currentTimeMillis();

        if (input.getSimulatorType() == 0) {
            topkGraphPattern = new IncrementalExhaustiveCountingFourNode();
        } else if (input.getSimulatorType() == 1) {
            //double epsilonk = (4 + epsilon) / (epsilon * epsilon);
            ///double Tkk = Math.log(Tk / delta);
            //int size = (int) (Tkk * epsilonk);
            //size = 132103;
            double M = (4 * (1 + Math.log(1 / input.getDelta()))) / (input.getEpsilon() * input.getEpsilon());
            int size = (int) Math.round(M);
            System.out.println(size);
            System.out.println("size of the reservoir: " + size);
            topkGraphPattern = new IncrementalSubgraphReservoirFourNode(size, input.getK());
        } else if (input.getSimulatorType() == 2) {
            double epsilonk = (4 + input.getEpsilon()) / (input.getEpsilon() * input.getEpsilon());
            double Tkk = Math.log(input.getTk() / input.getDelta());
            int size = (int) (Tkk * epsilonk);
            size = 349932;
            System.out.println("size of the reservoir: " + size);
            topkGraphPattern = new IncrementalEdgeReservoirFourNode(size, input.getK());
        } else if (input.getSimulatorType() == 3) {
            topkGraphPattern = new FullyDynamicExhaustiveCountingFourNode();
        } else if (input.getSimulatorType() == 4) {
            double epsilonk = (4 + input.getEpsilon()) / (input.getEpsilon() * input.getEpsilon());
            double Tkk = Math.log(input.getTk() / input.getDelta());
            int size = (int) (Tkk * epsilonk);
            System.out.println("size of the reservoir: " + size);
            topkGraphPattern = new FullyDynamicSubgraphReservoirFourNode(size, input.getK());
        } else if (input.getSimulatorType() == 5) {
            double epsilonk = (4 + input.getEpsilon()) / (input.getEpsilon() * input.getEpsilon());
            double Tkk = Math.log(input.getTk() / input.getDelta());
            int size = (int) (Tkk * epsilonk);
            System.out.println("size of the reservoir: " + size);
            topkGraphPattern = new FullyDynamicEdgeReservoirFourNode(size, input.getK());
        } else if (input.getSimulatorType() == 6) {
            int edgeCount = 0;
            final int PRINT_AFTER = 100000;
            EdgeHandler utility = new EdgeHandler();
            NodeMap nodeMap = new NodeMap();
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            while (edge != null) {
                int src = edge.getSource();
                int dst = edge.getDestination();

                if (src < min) {
                    min = src;
                }
                if (dst < min) {
                    min = dst;
                }
                if (src > max) {
                    max = src;
                }
                if (dst > max) {
                    max = dst;
                }
                utility.handleEdgeAddition(edge, nodeMap);

                edge = reader.nextItem();
                edgeCount++;

                if (edgeCount % PRINT_AFTER == 0) {
                    System.out.println(String.format("%dM\t\t%d", (edgeCount / PRINT_AFTER),
                            ((System.currentTimeMillis() - startTime) / 1000)));
                }

            }
            System.out.println(min + " " + max);
            System.exit(1);
        }

        /*
         * read from the edge list each line in the file represents a tuple of the form
         * <source-id,source-label,dest-id,dest-label,edge-label>
         */

        System.out.println("edges(k)\t\tsecs(s)\t\tpatterns(#)\t\treservoir-size(curr)");

        long edgeCount = 1;
        long PRINT_AFTER = 100000;
        long PRODUCE_SNAPSHOT_AFTER = 1_000_000;
        int index = 0;
        while (edge != null) {
            topkGraphPattern.addEdge(edge);
            // System.out.println("+ " + edge);

            // slide the window and get the last item if the window is full
            if (isFullyDynamicAlgorithm(input.getSimulatorType())) {
                Optional<StreamEdge> oldestEdge = sw.add(edge);
                if (oldestEdge.isPresent()) {
                    // System.out.println("- " + oldestEdge);
                    topkGraphPattern.removeEdge(oldestEdge.get());
                }

            }
            edge = reader.nextItem();
            edgeCount++;

            if (edgeCount % PRINT_AFTER == 0) {
                // System.out.println(String.format("%d", ((System.currentTimeMillis() -
                // startTime)/1000)));
                System.out.println(String.format("%d\t\t%d\t\t%d\t\t%d", (edgeCount / 1000),
                        ((System.currentTimeMillis() - startTime) / 1000),
                        topkGraphPattern.getFrequentPatterns().size(), topkGraphPattern.getCurrentReservoirSize()));
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("execution time: " + (endTime - startTime) / (double) 1000 + " secs.");

        // create the output file name
        String outFileName = input.getOutputDirectory() + "/output_" + input.getInputFileName() + "_" + input.getWindowSize() + "_" + input.getEpsilon() + "_" + input.getDelta() + "_"
                + input.getTk() + "_" + input.getK();

        if (input.getSimulatorType() == 0)
            outFileName = outFileName + "_incremental-exhaustive-four-node.log";
        else if (input.getSimulatorType() == 1)
            outFileName = outFileName + "_incremental-subgraph-reservoir-final-four-node.log";
        else if (input.getSimulatorType() == 2)
            outFileName = outFileName + "_incremental-edge-reservoir-final-four-node.log";
        else if (input.getSimulatorType() == 3)
            outFileName = outFileName + "_fully-dynamic-exhaustive-four-node.log";
        else if (input.getSimulatorType() == 4)
            outFileName = outFileName + "_fully-dynamic-subgraph-reservoir-final-four-node-reservoir.log";
        else if (input.getSimulatorType() == 5)
            outFileName = outFileName + "_fully-dynamic-edge-reservoir-final-four-node-reservoir.log";

        BufferedWriter bw = null;
        FileWriter fw = null;

        fw = new FileWriter(outFileName);
        bw = new BufferedWriter(fw);

        THashMap<Pattern, Long> correctEstimates = topkGraphPattern.correctEstimates();
        printMap(correctEstimates, bw);
        bw.flush();
        bw.close();
        System.out.println(topkGraphPattern.getNumberofSubgraphs());

    }

    public static void printMap(THashMap<Pattern, Long> mp, BufferedWriter bw) throws IOException {
        Iterator<Entry<Pattern, Long>> it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Pattern, Long> pair = it.next();
            bw.write(pair.getKey() + "\t" + pair.getValue() + "\n");
        }
    }

    private static boolean isFullyDynamicAlgorithm(int simulatorType) {
        return simulatorType == 3 || simulatorType == 4 || simulatorType == 5 || simulatorType == 6;
    }
}
