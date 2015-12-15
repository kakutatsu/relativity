package com.company.enums;

public enum TransactionType {

    保证金产品赎回,
    赎回到帐,
    保证金产品申购,
    股息入帐,

    利息归本,   //????
    批量利息归本,//????
    红利入账,
    红股入账,
    股息红利差异扣税,


    证券买入,
    证券卖出,
    担保品买入,
    担保品卖出,
    融资买入,
    融资借入,
    融券卖出,
    融券借入,

    买券还券,   //WHEN YOU HAVE SHORT SALE BALANCE
    卖券还款,   //WHEN YOU HAVE MARGINAL_TRADE BALANCE
    偿还融券负债,
    偿还融资负债本金,

    偿还融资利息,
    偿还融券费用,


    证券冻结,
    托管转出,
    担保物转入,
    银行转存,
    银行转证券,
    银行转取,
    证券转银行,
    直接还款预划出, //??????

    新股申购,
    申购配号,
    申购返款,
    红利差异税扣税
}
