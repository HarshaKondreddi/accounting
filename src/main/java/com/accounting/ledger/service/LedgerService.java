package com.accounting.ledger.service;

import akka.actor.typed.ActorSystem;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import com.accounting.ledger.commands.CreditCommand;
import com.accounting.ledger.commands.DebitCommand;
import com.accounting.ledger.entity.LedgerEntity;
import lombok.var;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class LedgerService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Duration timeout;
    private final ClusterSharding sharding;

    public LedgerService(ActorSystem<?> system) {
        timeout = Duration.ofSeconds(15);
        sharding = ClusterSharding.get(system);
    }

    public CompletionStage<String> creditAmount(int accountNumber, int amount) {
        logger.info("credit amount {} to account {}", amount, accountNumber);;
        CompletionStage<String> reply =
                sharding.entityRefFor(LedgerEntity.ENTITY_KEY, String.valueOf(accountNumber)).askWithStatus(
                        replyTo -> new CreditCommand(amount, replyTo), timeout);
        return reply;
    }

    public CompletionStage<String> debitAmount(int accountNumber, int amount) {
        logger.info("debit amount {} to account {}", amount, accountNumber);;
        CompletionStage<String> reply =
                sharding.entityRefFor(LedgerEntity.ENTITY_KEY, String.valueOf(accountNumber)).askWithStatus(
                        replyTo -> new DebitCommand(amount, replyTo), timeout);
        return reply;
    }
}
