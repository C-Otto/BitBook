# Commands

## Help
The `help` command shows a list of available commands.
You can also use `help xxx` to get more information about command `xxx`.

## Get Transaction Details
With `get-transaction-details <hash>` you can request information about a specific transaction.
The output includes the block height of the block that includes the transaction, and a time
when this transaction was first seen (which may be identical to the time the block was mined).

The fiat value shown next to the coin amount is based on the exchange rate at the time shown above.
The fiat exchange rate is only requested once per day, which is why the shown value may be imprecise.

The inputs and outputs are aggregated per address.
The symbol to the right of the address shows the ownership status (`?` if not set).

The output also shows descriptions for the addresses and transaction itself, if set.

The amount shown as "Contribution" gives the sum of all coins that this transaction
 - takes out of your owned addresses (negative), and 
 - puts back into your owned addresses (positive).

```
Transaction:    da30fbe98d0e21968ec73a995a45291b1795e3006c0dcb432bc5f351b140573f
Description:    This is the transaction description
Block:          677171 (2021-03-31T16:23:14)
Fees:              0.00023186 [        11.61€]
Contribution:      0.124      [     6,210.66€] 
Inputs:
           3AheoYDgWj3PLGA2XcmuZvyCzbsngz7sXd ?    0.12319446 [     6,170.32€]                     
           39Kyuxzgs4jm61MYgJYeJNHfUq5RanKBHf ✗    0.0010374  [        51.96€]                     
Outputs:
           35PWdG8CHar1dUj9RrYBneCyQcN6kzXqFS ✓    0.124      [     6,210.66€] Address Description
```

Note that BitBook only shows confirmed transactions with a confirmation depth of at least 6 confirmations.
This is done to avoid having outdated information in the case of chain reorganizations.

## Get Address Transactions
Using the `get-address-transactions` command you get information about all transactions connected to the address:

```
BitBook₿ get-address-transactions 3AheoYDgWj3PLGA2XcmuZvyCzbsngz7sXd
Address: 3AheoYDgWj3PLGA2XcmuZvyCzbsngz7sXd ?
Description: 
Transaction hashes (2):
3dfc4275b1b16be3009f087442f176f1acd89c71e996b3a6aec469e373dc65ca:    0.12319446 [     6,170.32€] (block height 677169, 2021-03-31T16:12:03)
da30fbe98d0e21968ec73a995a45291b1795e3006c0dcb432bc5f351b140573f:   -0.12319446 [    -6,170.32€] (block height 677171, 2021-03-31T16:23:15) Some Description
```

The value shown next to each transaction tells you the effect of the transaction to the address.
In the example above, you can see that the transaction `da30...` removed `0.123...` coins out of the address `3Ahe...`.

The fiat value shown is determined based on the exchange rate at the day of the transaction (updated once a day,
see [Get Transaction Details](#get-transaction-details)).
As such, if there is one transaction depositing coins into an address, and another transaction taking the same amount
out again, the fiat value shown for these two transactions may vary a lot if the fiat exchange rate differs a lot. 

The block height and timestamp shown for each transaction is identical to the information in
[get-transaction-details](#get-transaction-details).

By default, the transactions are sorted by the absolute amount, so that transactions having the most impact on the
address are shown at the bottom (see [Sort Order](#sort-order) if you want to change the sort order).

Note that the underlying network requests may be slow for addresses with many transactions.
The list of associated transactions is persisted, so it will not be downloaded more than once.
However, as new transactions may be added to the blockchain, BitBook needs to update the list.
This update is performed according to some heuristics taking into account how enough blocks have been added to the
blockchain since the last update.
For example, for addresses with an empty balance fewer updates are required.
Similarly, more frequent updates are required for addresses with many transactions, or if a transactions
associated with the address is recent.

**This means that an update request may be necessary every couple of hours!**

It may happen that information for one transaction or even several transactions cannot be retrieved.
In this case an error message is shown at the end of the list.

In the background BitBook automatically fetches transaction and price details for the hashes listed in the output.

## Ownership Commands
You can mark addresses as *owned* or *foreign*.
The information is included in the output (`✓` for owned addresses, `✗` for foreign addresses).
Furthermore, commands like `get-balance` and `get-owned-addresses` show you (aggregated) information about all owned
addresses.
Most importantly, the command [`get-neighbour-transactions`](#get-neighbour-transactions) makes use of this information
(see below).

To mark an address as owned, you can use `mark-address-as-owned` or the shorthand version `owned`:

```
BitBook₿ mark-address-as-owned 35PWdG8CHar1dUj9RrYBneCyQcN6kzXqFS
OK
```

Similarly, you can use `mark-address-as-foreign` and the shorthand version `foreign`.

Both commands accept an optional second argument to set the description for the address.

As soon as you mark an address as owned, BitBook automatically fetches the list of associated transaction hashes and the
details for these transactions in the background. Furthermore, exchange rates are requested for the dates of the
transactions.

You can reset ownership information for an address using `reset-ownership <address>`.

### Get Owned Addresses
The command `get-owned-addresses` can be used to list the addresses you marked as `owned`.
The output includes the current balance with the current fiat value, and the addresses' descriptions (if set).
By default, the output is sorted by value, so that the addresses with the highest number of coins are shown at the
bottom of the list (see [Sort Order](#sort-order) if you want to change the sort order).

```
BitBook₿ get-owned-addresses
35PWdG8CHar1dUj9RrYBneCyQcN6kzXqFS    0          [         0.00€]                     
36WvZoFtn8ng6V8RyfB76dF73rJD6FLz9a    0.0238648  [     1,127.22€] Some Description
```

Note that the list of transaction hashes associated with each owned address is updated as part of this computation,
which might cause a delay (see [Get Address Transactions](#get-address-transactions)).
Furthermore, the list only includes addresses where at least one transaction is known.

### Get Balance
The commands `get-balance` and `get-balance-for-address` can be used to get the balance, either aggregated over all
owned addresses, or for a single (possibly foreign) address.

```
BitBook₿ get-balance-for-address 36WvZoFtn8ng6V8RyfB76dF73rJD6FLz9a
   0.0238648  [     1,196.56€]
```

The exchange rate used for the computation is only requested once per day.

Note that, in order to compute the balance, all transactions associated with the address(es) need to be known.
As such, downloading the necessary information (if not already known) might take a while.
If the list of associated transactions for the/an address is outdated (see [Get Transaction Details](#get-transaction-details)),
the list is updated as part of this computation.

### Get My Transactions
The command `get-my-transactions` shows all transactions that are linked to (at least) one of your owned addresses.
In addition to the transaction details, the output shows the contribution of each transaction to your balance.
As such, transactions from one of your owned addresses to another (or the same) owned address have a negative
contribution, which is the transaction fee.

By default, the output is sorted by the absolute value of the contribution, so that the transactions having the most
impact on your balance are shown at the bottom of the list (see [Sort Order](#sort-order) if you want to change the
sort order).

```
BitBook₿ get-my-transactions
da30fbe98d0e21968ec73a995a45291b1795e3006c0dcb432bc5f351b140573f:   -0.12319446 [    -6,170.32€] (block height 677171, 2021-03-31T16:23:15) Some Description
```

### Get Neighbour Transactions
The command `get-neighbour-transactions` provides a rather straightforward output, but under the hood is the most
complex command in BitBook.
The command can be used to identify transactions that somehow are linked to an address you own, where funds come
from an address with unknown ownership, or where funds are sent to an address with unknown ownership.

#### Example: Owning an Output
A simple case is shown in the [example](example.md):

```
BitBook₿ get-transaction-details da30fbe98d0e21968ec73a995a45291b1795e3006c0dcb432bc5f351b140573f
Transaction:    da30fbe98d0e21968ec73a995a45291b1795e3006c0dcb432bc5f351b140573f
Description:    
Block:          677171 (2021-03-31T16:23:14)
Fees:              0.00023186 [        11.61€]
Contribution:      0.124      [     6,210.66€]
Inputs:
           3AheoYDgWj3PLGA2XcmuZvyCzbsngz7sXd ?    0.12319446 [     6,170.32€]                     
           39Kyuxzgs4jm61MYgJYeJNHfUq5RanKBHf ?    0.0010374  [        51.96€]                     
Outputs:
           35PWdG8CHar1dUj9RrYBneCyQcN6kzXqFS ✓    0.124      [     6,210.66€]       
```

In this example, you receive funds to address `35PWd...` from two addresses, both of which have unknown ownership.
As such, the transaction `da30...` is included in the output of `get-neighbour-transactions`, so that you can add the
missing ownership information. If you only set the first address `3Ahe...` as foreign (or owned), BitBook can only
determine that 0.12319446 out of your 0.124 coins have been accounted for. As such, `get-neighbour-transactions` would
still list the transaction (albeit with the "unknown" amount reduced by 0.12319446 coins, from 0.124 coins to just
0.00080554 coins).

If you also mark the second address as foreign (or owned), BitBook can determine that *all* the funds sent to your address
`35Pwd...` have been accounted for.

#### Marking a subset of inputs

For transactions where a subset of the inputs suffices to pay the amount you receive, you only need to mark this subset
as foreign (or owned), i.e. you can leave the rest as "unknown".
You also don't need to specify ownership information for outputs you don't own, which could be the change address used
by the original sender, or possibly a long list of other recipients.

#### Owning an Input

If you own an input of a transaction, you need to specify ownership information of all addresses linked to the
transaction in order to remove the transaction from `get-neighbour-transactions`.
This is due to the fact that BitBook is designed to help you track your coins.

#### Command Output

```
BitBook₿ get-neighbour-transactions
f74b5dd425497eaabbee8562cf9b41b1f99ba7209d199ca0ac9c0aee4b4804c5:   -0.1238648  [    -6,203.89€] (block 677175, 2021-03-31T17:06:31)          Description
```

The command output is a list of transactions, similar to what you see in [Get Address Transactions](#get-address-transactions).
The coin amount indicates the change from/to unknown addresses caused by the transaction, where transactions with
a value of zero are not included in the output.

This information is aggregated over all of your owned addresses, so that transactions between your owned addresses do
not show up (fees are considered to be sent to a foreign address).

By default, the list is sorted by absolute values, so that the transactions that have the most impact in your owned
addresses are shown at the bottom (see [Sort Order](#sort-order) if you want to change the sort order).

**As described in the [example](example.md), you could go through the list of
transactions (using [Get Transaction Details](#get-transaction-details)) and add ownership information until the
returned list is empty.**

The fiat value shown next to the coin amount is based on the exchange rate at the day of the corresponding transaction.
Descriptions set for transactions are included in the output.

Note that the list of transaction hashes associated with each owned address is updated as part of this computation,
which might cause a delay (see [Get Address Transactions](#get-address-transactions)).

## Descriptions
You can set textual descriptions to addresses and transactions so that this additional information is shown in the
commands' outputs in addition to the address/transaction hash.

**Note that you have to use quotes (`"` or `'`) if the description includes a space!**

```
BitBook₿ set-address-description 1ET8va8cJNGGLtG7pwRq79EeE7qNb7ofCS "Pete Peterson"
OK
BitBook₿ set-transaction-description f74b5dd425497eaabbee8562cf9b41b1f99ba7209d199ca0ac9c0aee4b4804c5 "Stuff from Pete"
OK
```

You can also remove descriptions using `remove-address-description` and `remove-transaction-description`.

## Tab Completion
All commands taking address or transaction hashes as arguments support tab completion.
For addresses, completion works for addresses that are either "known" (for example, because the details have been
received using `get-address-transactions`), or if the address is listed as an input or output of a known transaction.

Transaction hashes are completed for known transactions (for example, received using `get-transaction-details`), and
for hashes associated with an address (for example, because it is listed in the output of a `get-address-transactions`
invocation).

As such, if you see an address/transaction on screen, you can always complete it using tab completion.
You may also use copy-paste. Invalid characters (like `:` or spaces) are automatically ignored.

In addition to this, you can also use the description to complete the command:

```
BitBook₿ get-address-transactions Pete<tab>
```

This completes to
```
BitBook₿ get-address-transactions 1ET8va8cJNGGLtG7pwRq79EeE7qNb7ofCS Pete Peterson
```

*Note that this is implemented using a hack and might cause a messed up display. See [technical.md](technical.md).*

The list of completion candidates is adapted to the needs of the specific commands.
As an example, when using `remove-transaction-description`, you are only shown transaction hashes for which a
description is known.

## lnd Support
Please have a look at [lnd.md](lnd.md).

## Sort Order
Using the command `set-transaction-sort-order` you can influence how lists of transactions are ordered.
By default, transactions are sorted by their absolute coin value (i.e., -2 coins and 2 coins are treated as identical),
followed by the date, followed by the transaction hash.
As an example, you can use `set-transaction-sort-order BY_DATE_THEN_COINS_ABSOLUTE_THEN_HASH` to sort by date
(and then by absolute coin value if two transactions have the same date).
Please use [tab completion](#tab-completion) (type `set-transaction-sort-order` and press TAB twice) to see all
available sort orders.