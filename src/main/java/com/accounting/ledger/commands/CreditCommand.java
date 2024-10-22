package com.accounting.ledger.commands;

import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreditCommand implements LedgerCommand {

    private int amount;
    private ActorRef<StatusReply<String>> replyTo;
}
