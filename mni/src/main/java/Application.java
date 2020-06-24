import input.StreamEdge;
import input.StreamEdgeReader;
import struct.NodeMap;
import utility.EdgeHandler;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

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

public class Application {
    private static final String sep = ",";
    public static void main(String args[]) throws IOException {
        String inFileName = "/Users/anasir/Datasets/patents/patent-graph-stream.txt";

        NodeMap nodeMap = new NodeMap();
        EdgeHandler edgeHandler = new EdgeHandler();
        BufferedReader in = getBufferedReader(inFileName);

        readAndProcess(nodeMap, edgeHandler, in);
        in.close();


    }

    private static void readAndProcess(NodeMap nodeMap, EdgeHandler edgeHandler, BufferedReader in) throws IOException {
        StreamEdgeReader reader = new StreamEdgeReader(in, sep);
        StreamEdge edge = reader.nextItem();
        int edgeCount = 0;
        while (edge != null) {
            edgeCount++;
            if (edgeCount % 1000000 == 0) {
                System.out.println((edgeCount / 1000000) + "M edges read");
            }
            //System.out.println(edge);
            edgeHandler.handleEdgeAddition(edge, nodeMap);
            edge = reader.nextItem();
        }
    }

    private static BufferedReader getBufferedReader(String inFileName) throws IOException {
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
        return in;
    }
}
