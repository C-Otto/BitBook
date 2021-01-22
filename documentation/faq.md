# Frequently Asked Questions (FAQ)

## How do I start BitBook?
Install Java 16 and run `./start.sh`.
Please have a look at [#3](https://github.com/C-Otto/BitBook/issues/3) for further information.

## How do I use BitBook?
Please have a look at the [example](example.md) and use the `help` command.

## Is there a graphical version?
No. I'd love to hear your ideas, and get your help.
See [#1](https://github.com/C-Otto/BitBook/issues/1).

## What kind of data does BitBook send/leak?
BitBook uses several APIs to request information about prices, transaction hashes, and transaction details.
As such, if you're concerned about your privacy and don't want to send information about your owned addresses to public
APIs, please don't use BitBook.
Please have a look at [technical.md](technical.md) for details and
[#4](https://github.com/C-Otto/BitBook/issues/4) for a possible solution.

## What can I do with the data once I'm done? Is there an export?
There isn't anything useful, yet. You can use the commands to have a look at the addresses
and transactions, and you can also keep the information up-to-date.
Please have a look at [#2](https://github.com/C-Otto/BitBook/issues/2) and leave your feedback!
What kind of export do you need?

## Which commands are available?
You can run the `help` command to get a list:

```
BitBook$ help
AVAILABLE COMMANDS

Address Commands
        get-balance-for-address: Get balance for address
        remove-address-description: Removes a description for the address
        set-address-description: Sets a description for the address

Built-In Commands
        clear: Clear the shell screen.
        help: Display help about available commands.
        script: Read and execute commands from a file.
        stacktrace: Display the full stacktrace of the last error.

Ownership Commands
        foreign, mark-address-as-foreign: Mark an address as foreign (not owned)
        get-balance: Get the total balance over all owned addresses
        get-neighbour-transactions: Get transactions connected to own addresses where source/target has unknown ownership
        list-owned-addresses: List all owned addresses
        mark-address-as-owned, owned: Mark an address as owened
        reset-ownership: Removes information about ownership for the given address

Quit Command
        exit, quit: Exit the shell.

Transactions Commands
        get-address-transactions: Get transactions for address
        get-transaction-details: Get data for a given transaction
```

## How should I interpret the shown prices/values?
While the coin information is accurate to the Satoshi, for fiat (Euro) values only a single price per
day is used. As such, the shown values are intended to be helpful, but not accurate enough for proper
accounting purposes. Please note that balances are always shown based on the *current* price, whereas
transaction-specific values are always computed based on the price at the date of the transaction.