package com.accounting.ledger.entity;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.pattern.StatusReply;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import akka.persistence.typed.scaladsl.Effect;
import com.accounting.ledger.commands.CreditCommand;
import com.accounting.ledger.commands.DebitCommand;
import com.accounting.ledger.commands.LedgerCommand;
import com.accounting.ledger.events.CreditedEvent;
import com.accounting.ledger.events.DebitedEvent;
import com.accounting.ledger.events.LedgerEvent;
import com.accounting.ledger.state.LedgerState;

public class LedgerEntity extends EventSourcedBehavior<LedgerCommand, LedgerEvent, LedgerState> {

    public static final EntityTypeKey<LedgerCommand> ENTITY_KEY =
            EntityTypeKey.create(LedgerCommand.class, "Ledger");

    public static void init(ActorSystem<?> system) {
        ClusterSharding.get(system)
                .init(
                        Entity.of(
                                ENTITY_KEY,
                                entityContext -> LedgerEntity.create(entityContext.getEntityId())));
    }

    public static Behavior<LedgerCommand> create(String entityId) {
        return Behaviors.setup(
                context -> new LedgerEntity(PersistenceId.of(ENTITY_KEY.name(), entityId)));
    }

    public LedgerEntity(PersistenceId persistenceId) {
        super(persistenceId);
    }

    @Override
    public LedgerState emptyState() {
        return new LedgerState(0);
    }

    @Override
    public CommandHandler<LedgerCommand, LedgerEvent, LedgerState> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(CreditCommand.class, (state, cmd) ->
                        Effect().persist(new CreditedEvent(cmd.getAmount()))
                                .thenReply(cmd.getReplyTo(), updatedState ->
                                        StatusReply.success("available amount is " + updatedState.getBalance())
                                )
                )
                .onCommand(DebitCommand.class, (state, cmd) ->
                        Effect().persist(new DebitedEvent(cmd.getAmount()))
                                .thenReply(cmd.getReplyTo(), updatedState ->
                                        StatusReply.success("available amount is " + updatedState.getBalance())
                                )
                )
                .build();
    }

    @Override
    public EventHandler<LedgerState, LedgerEvent> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
                .onEvent(CreditedEvent.class, (state, event) -> state.creditAmount(event.getAmount()))
                .onEvent(DebitedEvent.class, (state, event) -> state.debitAmount(event.getAmount()))
                .build();
    }
}
