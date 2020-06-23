import com.beust.jcommander.JCommander;
import fullydynamic.FullyDynamicEdgeReservoirThreeNode;
import fullydynamic.FullyDynamicExhaustiveCountingThreeNode;
import fullydynamic.FullyDynamicSubgraphReservoirThreeNode;
import gnu.trove.map.hash.THashMap;
import incremental.IncrementalEdgeReservoirThreeNode;
import incremental.IncrementalExhaustiveCountingThreeNode;
import incremental.IncrementalSubgraphReservoirThreeNode;
import input.InputParameters;
import input.StreamEdge;
import input.StreamEdgeReader;
import slidingwindow.FixedSizeSlidingWindow;
import topkgraphpattern.Pattern;
import topkgraphpattern.TopkGraphPatterns;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

/*
 * The main class to run different algorithms
 * Created By: Anis Nasir
 * Created on: 18 Oct 2018
 * Updated on: 18 Oct 2018
 */

/**
 * @author Anis
 * 
 *         main method to compare different algorithm Input Parameter:
 *         simulatorType: integer directory: string (input directory) fileName:
 *         string (input file in the form of edge list) windowSize: integer (for
 *         sliding window) epsilon: parameter to calculate size of the subgraph
 *         reservoir delta: parameter to calculate the size of the subgraph
 *         reservoir Tk: paramreter to calculate the size of the subgraph
 *         reservoir k: integer (parameter for the top-k algorithm)
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
			double epsilonk = (4 + input.getEpsilon()) / (input.getEpsilon() * input.getEpsilon());
			double Tkk = Math.log(input.getTk() / input.getDelta());
			int size = (int) (Tkk * epsilonk);
			System.out.println("size of the reservoir: " + size);
			topkGraphPattern = new FullyDynamicSubgraphReservoirThreeNode(size, input.getK());
		} else if (input.getSimulatorType() == 1) {
			double epsilonk = (4 + input.getEpsilon()) / (input.getEpsilon() * input.getEpsilon());
			double Tkk = Math.log(input.getTk() / input.getDelta());
			int size = (int) (Tkk * epsilonk);
			System.out.println("size of the reservoir: " + size);
			// int size = 1270176; //this one is the max from youtube dataset
			// int size = 988471; //this one is the max from patent dataset
			topkGraphPattern = new FullyDynamicEdgeReservoirThreeNode(size, input.getK());
		} else if (input.getSimulatorType() == 2) {
			topkGraphPattern = new FullyDynamicExhaustiveCountingThreeNode();
		} else if (input.getSimulatorType() == 3) {
			double epsilonk = (4 + input.getEpsilon()) / (input.getEpsilon() * input.getEpsilon());
			double Tkk = Math.log(input.getTk() / input.getDelta());
			int size = (int) (Tkk * epsilonk);
			System.out.println("size of the reservoir: " + size);
			topkGraphPattern = new IncrementalSubgraphReservoirThreeNode(size, input.getK());
		} else if (input.getSimulatorType() == 4) {
			double epsilonk = (4 + input.getEpsilon()) / (input.getEpsilon() * input.getEpsilon());
			double Tkk = Math.log(input.getTk() / input.getDelta());
			int size = (int) (Tkk * epsilonk);
			System.out.println("size of the reservoir: " + size);
			// int size = 988471; //this one is the max from patent dataset
			topkGraphPattern = new IncrementalEdgeReservoirThreeNode(size, input.getK());
		} else if (input.getSimulatorType() == 5) {
			topkGraphPattern = new IncrementalExhaustiveCountingThreeNode();
		}

		/*
		 * read from the edge list each line in the file represents a tuple of the form
		 * <source-id,source-label,dest-id,dest-label,edge-label>
		 */

		System.out.println("edges(k)\t\tsecs(s)\t\tpatterns(#)\t\treservoir-size(curr)");

		long edgeCount = 1;
		long PRINT_AFTER = 100000;
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
			outFileName = outFileName + "_fully-dynamic-subgraph-reservoir.log";
		else if (input.getSimulatorType() == 1)
			outFileName = outFileName + "_fully-dynamic-trieste-reservoir.log";
		else if (input.getSimulatorType() == 2)
			outFileName = outFileName + "_fully-dynamic-exhaustive-counting.log";
		else if (input.getSimulatorType() == 3)
			outFileName = outFileName + "_incremental-subgraph-reservoir.log";
		else if (input.getSimulatorType() == 4)
			outFileName = outFileName + "_incremental-trieste-reservoir.log";
		else if (input.getSimulatorType() == 5)
			outFileName = outFileName + "_incremental-exhaustive-counting.log";

		BufferedWriter bw = null;
		FileWriter fw = null;

		fw = new FileWriter(outFileName);
		bw = new BufferedWriter(fw);

		topkGraphPattern.correctEstimates();
		printMap(topkGraphPattern.getFrequentPatterns(), bw);
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
		return simulatorType == 0 || simulatorType == 1 || simulatorType == 2 ;
	}
}
