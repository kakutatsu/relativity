package com.company;

import com.company.enums.TransactionType;
import com.sun.tools.javac.util.Assert;
import javafx.util.Pair;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class TransactionParser {

    public static final List<String> defaultHeader = Arrays.asList(
            "币种", "证券名称", "成交日期", "成交价格", "成交数量", "发生金额",
            "资金余额", "合同编号", "业务名称", "手续费", "印花税", "过户费",
            "结算费", "证券代码", "股东代码", "买卖标志");

    public final static HashMap<String, Double> eomPosition201311 = new HashMap<String, Double>() {{
        put("三维工程", 2500.0);
        put("中国国旅", 1300.0);
        put("海康威视", 1200.0);
        put("中国化学", 6200.0);
        put("广联达", 1000.0);
        put("信立泰", 800.0);
    }};

    private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
    public final static TransactionState eomState201311
            = new TransactionState(LocalDate.of(2013, 11, 30), 100.68, 49155.46, eomPosition201311);
    private final List<File> _files;

    public TransactionParser(List<File> files) {
        _files = files;
    }

    public ArrayList<TransactionState> parseFiles(LinkedList<Pair<LocalDate, Double>> transfers) {
        return parseFiles(transfers, false, false);
    }

    public ArrayList<TransactionState> parseFiles(LinkedList<Pair<LocalDate, Double>> transfers, boolean pringBankTransfers, boolean printTradeDetails) {
        final List<TransactionRow> allRows = new ArrayList<>(500);
        _files.forEach(file -> parseTransactionFile(file, allRows));
        Collections.sort(allRows);

        ArrayList<TransactionState> eomStates = new ArrayList<>(1000);
        Double accumulatedTransfers = 0.0;
        Double cash = eomState201311.cash;
        Double ttl = eomState201311.ttl;
        HashMap<String, Double> position = eomPosition201311;
        TransactionState previousState = eomState201311;
        for (TransactionRow row : allRows) {
            Double transfer = 0.0;
            Double lastPosition;
            boolean print = true;
            switch (row.transactionType) {
                case 赎回到帐:
                case 证券冻结:
                case 托管转出:
                case 担保物转入:
                case 融资借入:
                case 偿还融资负债本金:
                case 直接还款预划出: //???
                case 新股申购:
                case 申购配号:
                case 申购返款:
                case 融券借入:
                case 偿还融券负债:
                    print = false;
                    break;
                case 保证金产品赎回:
                case 保证金产品申购:
                    cash += row.transactionAmount;
                    ttl -= row.transactionAmount;
                    break;
                case 红股入账:
                    lastPosition = position.get(row.securityName);
                    if (lastPosition == null) {
                        throw new RuntimeException("Impossible");
                    } else {
                        position.put(row.securityName, lastPosition + row.transactionVolume);
                    }
                    break;
                case 证券买入://positive values
                case 担保品买入://positive values
                case 融资买入://positive values
                case 买券还券://positive values

                case 证券卖出: //negative values
                case 担保品卖出://negative values
                case 卖券还款://negative values
                case 融券卖出://negative values
                    lastPosition = position.get(row.securityName);
                    if (lastPosition == null) {
                        position.put(row.securityName, row.transactionVolume);
                    } else {
                        double newPosition = lastPosition + row.transactionVolume;
                        if (newPosition == 0.0)
                            position.remove(row.securityName);
                        else
                            position.put(row.securityName, newPosition);
                    }
                    cash += row.transactionAmount;
                    break;
                case 批量利息归本:
                case 利息归本:
                case 股息入帐:
                case 偿还融资利息:
                case 红利入账:
                case 股息红利差异扣税:
                case 偿还融券费用:
                case 红利差异税扣税:
                    cash += row.transactionAmount;
                    break;
                case 银行转存: //positive transactionAmount
                case 银行转证券: //positive transactionAmount
                case 证券转银行: //negative transactionAmount
                case 银行转取: //negative transactionAmount
                    transfer = row.transactionAmount;
                    accumulatedTransfers += row.transactionAmount;
                    if (!transfers.isEmpty()) {
                        Pair<LocalDate, Double> nextPermanentTransfer = transfers.getFirst();
                        if (row.transactionDate.equals(nextPermanentTransfer.getKey())
                                && row.transactionAmount.equals(nextPermanentTransfer.getValue())) {
                            cash += row.transactionAmount;
                            transfers.removeFirst();
                        }
                    }
                    break;
                default:
                    throw new RuntimeException(row.toString());
            }
            if (ttl < 0) {
                //throw new RuntimeException("Adjust your ttl");
            }
            //Add end of month positions
            if (previousState.date.getMonthValue() != row.transactionDate.getMonthValue())
                eomStates.add(previousState);
            TransactionState afterTrade = new TransactionState(previousState, row, cash, ttl, position, accumulatedTransfers);
            previousState = afterTrade;

            if (print) {
                if (pringBankTransfers)
                    System.out.println(String.format("%s: Transfer@ %.2f, Accumulated@ %.2f",
                            row.transactionDate, transfer, accumulatedTransfers));
                if (printTradeDetails) {
                    System.out.println(afterTrade.toString() + System.lineSeparator());
                }
            }
        }
        //Add last end-of-month
        if (eomStates.get(eomStates.size() - 1).date.getMonthValue()
                != previousState.date.getMonthValue())
            eomStates.add(previousState);
        return eomStates;
    }

    private static void parseTransactionFile(File file, List<TransactionRow> rows) {
        try {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file)))) {
                validateFileHeaders(file, br);

                String line;
                TransactionRow row = new TransactionRow();
                while ((line = br.readLine()) != null) {
                    if (line.isEmpty())
                        continue;
                    row = parseTransactionLine(line.trim(), row.transactionDate);
                    rows.add(row);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void validateFileHeaders(File file, BufferedReader br) throws IOException {
        br.readLine();
        br.readLine(); //skip first two lines.
        String header = br.readLine();
        boolean match = Arrays.stream(header.split("\\s", -1))
                .filter(s -> s != null && !s.isEmpty())
                .allMatch(x -> defaultHeader.contains(x));
        Assert.check(match,
                "The header in " + file + " is unexpected.");
    }

    private static TransactionRow parseTransactionLine(String line, LocalDate transactionDate) {
        StringTokenizer tokenizer;

        String firstPart = line.substring(0, 25);
        tokenizer = new StringTokenizer(firstPart, " ");
        tokenizer.nextToken(); //Currency
        String securityName = ""; // Won't exist if it's 批量利息归本, or 银行转取...
        if (tokenizer.hasMoreTokens())
            securityName = tokenizer.nextToken(); //880013


        String secondPart = line.substring(20);
        tokenizer = new StringTokenizer(secondPart, " ");

        String tDate = tokenizer.nextToken().trim();
        if (!tDate.equals("0")) //If s is 0, use last transactionDate
            transactionDate = LocalDate.parse(tDate.substring(0, 8), dateFormat);

        tokenizer.nextToken(); //transactionPrice;
        String transactionVolumnString = tokenizer.nextToken();
        Double transactionVolumn = 0.0;
        if (!transactionVolumnString.equals("---"))
            transactionVolumn = Double.valueOf(transactionVolumnString);
        Double transactionAmount = Double.valueOf(tokenizer.nextToken());
        Double transactionBalance = Double.valueOf(tokenizer.nextToken());
        String notSure = tokenizer.nextToken(); //合同编号 or 业务名称
        String transactionTypeString;
        if (((int) notSure.charAt(0)) > 127)
            transactionTypeString = notSure;
        else
            transactionTypeString = tokenizer.nextToken();

        TransactionType transactionType;
        if (transactionTypeString.contains("(")) {
            int split = transactionTypeString.indexOf("(");
            transactionType = TransactionType.valueOf(transactionTypeString.substring(0, split));
        } else
            transactionType = TransactionType.valueOf(transactionTypeString);

        return new TransactionRow(transactionDate, transactionVolumn, transactionAmount,
                transactionBalance, transactionType, securityName);
    }
}
