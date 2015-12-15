package com.company;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class PriceParser {

    private List<File> files;

    public PriceParser(List<File> files) {

        this.files = files;
    }

    public HashMap<String, HashMap<String, Double>> parseFiles() {
        HashMap<String, HashMap<String, Double>> prices = new HashMap<>(files.size());
        files.stream().forEach(file -> parseFile(file, prices));
        return prices;
    }

    private void parseFile(File file, HashMap<String, HashMap<String, Double>> prices) {
        try {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file)))) {
                String firstLine = br.readLine();
                br.readLine();

                HashMap<String, Double> eomPrice = new HashMap<>();
                String lastLine = br.readLine();
                String newLine;
                while (!(newLine = br.readLine()).startsWith("数据来源")) {
                    if (!lastLine.substring(0, 7).equals(newLine.substring(0, 7))) {
                        parseLine(file, eomPrice, lastLine);
                    }
                    lastLine = newLine;
                }
                parseLine(file, eomPrice, lastLine);

                StringTokenizer tokenizer = new StringTokenizer(firstLine);
                tokenizer.nextToken();
                prices.put(tokenizer.nextToken(), eomPrice);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseLine(File file, HashMap<String, Double> eomPrice, String lastLine) {
        String[] splits = lastLine.split(",");
        if (splits.length != 7)
            throw new RuntimeException("Unexpected file format in " + file
                    + System.lineSeparator() + "at " + lastLine);
        eomPrice.put(splits[0].substring(0, 7).replace("/", ""),
                Double.valueOf(splits[4]));
    }
}
