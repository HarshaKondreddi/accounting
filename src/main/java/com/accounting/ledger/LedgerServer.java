package com.accounting.ledger;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import com.accounting.ledger.entity.LedgerEntity;
import com.accounting.ledger.service.LedgerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

public class LedgerServer extends AllDirectives {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final LedgerService ledgerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LedgerServer(ActorSystem<?> system) {
        this.ledgerService = new LedgerService(system);
    }

    public static void main(String[] args) {
        Behavior<Void> rootBehavior = Behaviors.setup(context -> {
            new LedgerServer(context.getSystem()).startServer(context.getSystem());
            return Behaviors.empty();
        });
        ActorSystem<Void> system = ActorSystem.create(rootBehavior, "LedgerSystem");
        ClusterSharding.get(system).init(
                Entity.of(LedgerEntity.ENTITY_KEY, ctx -> LedgerEntity.create(ctx.getEntityId()))
        );
    }

    private void startServer(ActorSystem<?> system) {
        final Http http = Http.get(system);
        final CompletionStage<ServerBinding> binding = http.newServerAt("localhost", 8081).bind(createRoute());

        binding.thenAccept(bindingResult ->
                logger.info("Server online at http://localhost:8080/")
        ).exceptionally(ex -> {
            logger.error("Failed to bind HTTP endpoint, terminating system", ex);
            system.terminate();
            return null;
        });
    }

    private Route createRoute() {
        return concat(
                path("credit", () ->
                        get(() ->
                                parameter("accountNumber", accountNumberStr ->
                                        parameter("amount", amountStr -> {
                                            try {
                                                int accountNumber = Integer.parseInt(accountNumberStr);
                                                int amount = Integer.parseInt(amountStr);
                                                return onSuccess(ledgerService.creditAmount(accountNumber, amount), result ->
                                                        complete(StatusCodes.OK, result)
                                                );
                                            } catch (NumberFormatException e) {
                                                return complete(StatusCodes.BAD_REQUEST, "Invalid number format");
                                            }
                                        })
                                )
                        )
                ),
                path("debit", () ->
                        get(() ->
                                parameter("accountNumber", accountNumberStr ->
                                        parameter("amount", amountStr -> {
                                            try {
                                                int accountNumber = Integer.parseInt(accountNumberStr);
                                                int amount = Integer.parseInt(amountStr);
                                                return onSuccess(ledgerService.debitAmount(accountNumber, amount), result ->
                                                        complete(StatusCodes.OK, result)
                                                );
                                            } catch (NumberFormatException e) {
                                                return complete(StatusCodes.BAD_REQUEST, "Invalid number format");
                                            }
                                        })
                                )
                        )
                )
        );
    }
}
