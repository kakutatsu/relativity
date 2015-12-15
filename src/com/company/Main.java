package com.company;

import com.sun.tools.javac.util.Assert;
import javafx.util.Pair;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws Exception {

        File pricePath = new File(Paths.get(System.getProperty("user.dir"),
                "data/price").toString());
        File[] prices = pricePath.listFiles();
        if (prices == null)
            throw new RuntimeException("Files are empty");

        HashMap<String, HashMap<String, Double>> eomPrices = new PriceParser(
                Arrays.asList(prices)).parseFiles();

        File transactionPath = new File(Paths.get(System.getProperty("user.dir"),
                "data/transaction").toString());
        File[] transactions = transactionPath.listFiles();
        if (transactions == null)
            throw new RuntimeException("Files are empty");

        TransactionParser parser = new TransactionParser(
                Arrays.asList(transactions)
                        .stream()
                        .filter(x -> x.getName().startsWith("资金流水"))
                        .collect(Collectors.toList()));

        LinkedList<Pair<LocalDate, Double>> transfers = new LinkedList<>();
        transfers.add(new Pair<>(LocalDate.of(2014, 1, 15), -100.0));
        transfers.add(new Pair<>(LocalDate.of(2014, 1, 16), 10000.0));
        transfers.add(new Pair<>(LocalDate.of(2014, 10, 13), -400.0));
        transfers.add(new Pair<>(LocalDate.of(2015, 5, 8), -200500.0));
        transfers.add(new Pair<>(LocalDate.of(2015, 5, 11), -500.0));

        ArrayList<TransactionState> eomPositions = parser.parseFiles(transfers, false, false);
        Assert.check(transfers.isEmpty());
        calculateEOMBalance(eomPrices, eomPositions);
    }

    private static void calculateEOMBalance(HashMap<String, HashMap<String, Double>> eomPrices, ArrayList<TransactionState> eomPositions) {
        eomPositions.forEach(eomPosition -> {
            System.out.print(eomPosition.yearMonth() + ":");
            final double[] totalAsset = new double[]{0.0};
            eomPosition.position.forEach((stock, sPosition) -> {
                Double price = 0.0;
                try {
                    price = eomPrices.get(stock).get(eomPosition.yearMonth());
                    totalAsset[0] += price * sPosition;
                } catch (Exception e) {
                    System.out.println("have problems finding price for " + stock + " at" + eomPosition.yearMonth());
                }
                //System.out.print(stock + "@" + price + "of" + sPosition + ", ");
            });
            System.out.println("transfer@ " + eomPosition.accumulatedTransfer + " cash@ " + eomPosition.cash + " ttl@ " + eomPosition.ttl);
            System.out.println("total@ " + (totalAsset[0] + eomPosition.cash + eomPosition.ttl));
            System.out.print(" position [" + eomPosition.position.toString() + "]");
            System.out.println();
        });
    }
}