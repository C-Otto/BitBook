# Frequently Asked Questions (FAQ)

## How do I start BitBook?
Install Java 17 and run `./start.sh`.
Note that startup might take a few seconds. It is finished as soon as you see the `BitBook₿ ` prompt.

Please have a look at [issue #3](https://github.com/C-Otto/BitBook/issues/3) for further information.

## How do I use BitBook?
Please have a look at the [example](example.md) and have a look at the available [commands](commands.md).

## Is there a graphical version?
No. I'd love to hear your ideas, and get your help.
See [issue #1](https://github.com/C-Otto/BitBook/issues/1).

## What kind of data does BitBook send/leak?
BitBook uses several APIs to request information about prices, transaction hashes, and transaction details.
As such, if you're concerned about your privacy and don't want to send information about your owned addresses to public
APIs, please don't use BitBook.
Please have a look at [technical.md](technical.md) for details and
[issue #4](https://github.com/C-Otto/BitBook/issues/4) for a possible solution.

## What can I do with the data once I'm done? Is there an export?
There isn't anything useful, yet. You can use the commands to have a look at the addresses
and transactions, and you can also keep the information up-to-date.
Please have a look at [issue #2](https://github.com/C-Otto/BitBook/issues/2) and leave your feedback!
What kind of export do you need?

## Which commands are available?
See [comands.md](commands.md).

## How should I interpret the shown prices/values?
While the coin information is accurate to the Satoshi, for fiat (Euro) values only a single price per
day is used. As such, the shown values are intended to be helpful, but not accurate enough for proper
accounting purposes. Please note that balances are always shown based on the *current* price, whereas
transaction-specific values are always computed based on the price at the date of the transaction.
