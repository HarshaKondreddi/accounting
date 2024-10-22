# Accounting Application

This project is designed to spin up a simple accounting application leveraging Akka HTTP, Akka Persistence with Cassandra, and Event sourcing behavior.

## Features

- **Akka HTTP**: A toolkit for building RESTful services in Scala.
- **Akka Persistence with Cassandra**: Utilizes Cassandra for event sourcing and persistence.
- **Event Sourcing Behavior**: Implements event sourcing to manage state changes.

## Prerequisites

Ensure you have the following installed on your machine:

- **Scala**: Version 2.13
- **Java**: Version 11
- **Cassandra**: Running instance required

## Setup Instructions

1. **Clone the Repository**
2. Run the application from the file `LedgerServer`
3. Use the following APIs to test
   - CreditAmount in an accountNumber - http://localhost:8081/credit?accountNumber={accountNumber}&amount={amount}
   - DeditAmount in an accountNumber  - http://localhost:8081/debit?accountNumber={accountNumber}&amount={amount}
