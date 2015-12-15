package com.company;

import com.company.enums.TransactionType;

import java.time.LocalDate;

public class TransactionRow implements Comparable<TransactionRow> {
    public final LocalDate transactionDate;
    public final Double transactionVolume;
    public final Double transactionAmount;
    public final Double transactionBalance;
    public final TransactionType transactionType;
    public final String securityName;
    public final String yearMonth;

    public TransactionRow() {
        this.transactionDate = null;
        this.transactionVolume = null;
        this.transactionAmount = null;
        this.transactionBalance = null;
        this.transactionType = null;
        this.securityName = null;
        this.yearMonth = null;
    }

    public TransactionRow(LocalDate transactionDate, Double transactionVolume, Double transactionAmount, Double transactionBalance, TransactionType transactionType, String securityName) {

        this.transactionDate = transactionDate;
        this.transactionVolume = transactionVolume;
        this.transactionAmount = transactionAmount;
        this.transactionBalance = transactionBalance;
        this.transactionType = transactionType;
        if (securityName.equals("880013"))
            securityName = "";
        this.securityName = securityName;

        Integer year = transactionDate.getYear();
        Integer month = transactionDate.getMonthValue();
        this.yearMonth = year.toString() + month.toString();
    }

    @Override
    public String toString() {
        return String.format("%1$s: %2$s, %3$s, %4$.2f, %5$.2f",
                transactionDate, securityName, transactionType, transactionVolume, transactionAmount);
    }

    @Override
    public int compareTo(TransactionRow other) {
        return transactionDate.compareTo(other.transactionDate);
    }
}
