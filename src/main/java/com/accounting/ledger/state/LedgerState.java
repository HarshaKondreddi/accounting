package com.accounting.ledger.state;

import com.accounting.ledger.serializer.CborSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LedgerState implements CborSerializable {

    private Integer balance;

    public LedgerState creditAmount(int amount) {
        this.balance += amount;
        return this;
    }

    public LedgerState debitAmount(int amount) {
        this.balance -= amount;
        return this;
    }
}
