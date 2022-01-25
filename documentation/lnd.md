# lnd Support
BitBook has support for [lnd (lightning network daemon)](https://github.com/lightningnetwork/lnd) so that you can
add ownership information and helpful descriptions for transactions/addresses managed by your `lnd` instance without
entering these manually.

For this, BitBook parses JSON files you can generate using lnd's `lncli` command line tool. This has the advantage of
keeping things simple. By copying these files you can easily use BitBook and `lnd` on different computers. Furthermore,
by not giving BitBook access to lnd, there's no risk of undesired side effects.

### Creating JSON files
Before you can use any of the command listed below, you need to create and transfer the necessary JSON input files:

1. first create the JSON file using lnd: `$ lncli COMMAND > lnd-COMMAND.json` (where `COMMAND` is described below)
2. transfer the JSON file to the host where you are running BitBook: `$ scp server:/home/lnd/lnd-COMMAND.json /tmp/`

### Unspent Outputs
Using `lncli listunspent` you can get a list of all unspent outputs managed by `lnd`.
BitBook marks these addresses as owned and sets the description to "lnd".

First, create and transfer the JSON file obtained by `lncli listunspent` (see [above](#creating-json-files)).
Then, run the command `lnd-add-from-unspent-outputs` as follows:
```
BitBook₿ lnd-add-from-unspent-outputs /tmp/lnd-listunspent.json
Marked 6 addresses as owned by lnd
```

### Sweeps
After a channel is closed, your funds (on the "local" side of the channel) are sent to an address that is not derived
from your `lnd` wallet seed. To avoid loss of funds, `lnd` automatically "sweeps" those funds to another address. As
such, there may be several sweep transactions that transfer funds from one address to another.

BitBook offers the command `lnd-add-from-sweeps` which parses `lnd` sweep information and, for each transaction:

 - sets the transaction description to "lnd sweep transaction"
 - sets the source and target addresses descriptions to "lnd"
 - marks both addresses as owned

First, create and transfer the JSON file obtained by `lncli wallet listsweeps` (see [above](#creating-json-files)).
Then, run the command `lnd-add-from-sweeps` as follows:
```
BitBook₿ lnd-add-from-sweeps /tmp/lnd-wallet-listsweeps.json
Added information for 86 sweep transactions
```

### Channels
`lnd` stores information about channels, including references to the opening transaction and whether you or the remote
node opened the channel.

BitBook uses this information and

- marks the channel address as owned if you opened the channel, or as foreign if the remote node opened the channel
- sets the channel address description to "Lightning-Channel with {pubkey}" where `{pubkey}` is the remote node's public
  key
- sets the transaction description to "Opening Channel with {pubkey} ({type})" where `{type}` is "local" or "remote"

First, create and transfer the JSON file obtained by `lncli listchannels` (see [above](#creating-json-files)).
Then, run the command `lnd-add-from-channels` as follows:
```
BitBook₿ lnd-add-from-channels /tmp/lnd-listchannels.json
Added information for 67 channels
```

Notes:

- If you are also the owner of the remote node of one of the channels, setting ownership of the address belonging to the
  remote node as *foreign* may be wrong. To avoid this, for addresses already marked as *owned* the ownership is not
  changed.
- If a transaction opens more than one channel, only one of these is mentioned in the description.

### Closed Channels
In addition to open channels, `lnd` also stores information about closed channels. This includes references to both the
opening and closing transactions, and information about the amount returned to your own wallet.

BitBook makes use of this information and

- marks the channel address as owned if you opened the channel, or as foreign if the remote node opened the channel 
- sets the channel address description to "Lightning-Channel with {pubkey}" where `{pubkey}` is the remote node's public
  key
- sets the *closing* transaction description to "Closing Channel with {pubkey} ({type})" where `{type}` is one out of
  "cooperative", "cooperative local", "cooperative remote", "force local", or "force remote"
- sets the *opening* transaction description to "Opening Channel with {pubkey} ({type})" where `{type}` is
      "local", "remote", or "unknown"
- for outputs that receive funds marks their addresses as owned
- for cooperative closes, mark all other (unowned) outputs as foreign
- handles sweep transactions mentioned in HTLC resolutions (see [Sweeps](#sweeps))

First, create and transfer the JSON file obtained by `lncli closedchannels` (see [above](#creating-json-files)).
Then, run the command `lnd-add-from-closed-channels` as follows:
```
BitBook₿ lnd-add-from-closed-channels /tmp/lnd-closedchannels.json
Added information for 99 closed channels
```
  
Notes:

- If you are also the owner of the remote node of one of the channels, setting ownership of the address belonging to the
  remote node as *foreign* may be wrong. To avoid this, for addresses already marked as *owned* the ownership is not
  changed.
- If a transaction opens more than one channel, only one of these is mentioned in the description.
- Currently, BitBook is unable to determine all necessary ownership information for addresses that result out of
  unsettled HTLC transactions. See [issue #90](https://github.com/C-Otto/BitBook/issues/89).

### On-Chain Transactions
`lnd` keeps a record of all on-chain transactions, which includes regular transactions from/to `lnd`'s wallet.
BitBook is able to distinguish the following transaction types:

1. **Funding Transactions**
   
   For transactions that send funds to `lnd`'s wallet, the target address is marked as owned, and
   the description "lnd" is set for the address.
   This is only done if no label is set for the transaction (in `lnd`).
   
2. **Spending Transactions**

   For transactions that send funds from `lnd`'s wallet, the source and change addresses are marked as owned, and
   the description "lnd" is set for the addresses.
   This is only done if all addresses are already marked as owned and if no label is set for the transaction (in `lnd`).

3. **Channel Opening Transactions**
   
   For transactions where one of the outputs is a lightning channel that is already marked as owned, the other
   output addresses are also marked as owned if:

   - all inputs are owned and have the description "lnd"
   - the channel address has a description as set by BitBook
   - the amount referenced in the transaction matches the channel balance
  
   *Important:* To satisfy the first two criteria, you should run the other `lnd` related commands first.

    BitBook also updates the initiator in the transaction description to "local", if it was set to "unknown".

4. **Sweep Transactions**
   
   For transactions that only transfer funds from one address managed by `lnd` to another address managed by `lnd`
   the changes as described in [Sweeps](#sweeps) are applied.
   
5. **pool Account Creation Transactions**

   If you use `lnd` with [pool](https://github.com/lightninglabs/pool), BitBook can read information about transactions
   that create, update, or close a `pool` account. In all cases, all addresses are marked as owned, and the address
   descriptions are set to "lnd" or "pool account {pool-id}". The transaction description is set to
   "Creating pool account {account-id}", "Closing pool account {account-id}", or
   "Deposit into pool account {account-id}".

First, create and transfer the JSON file obtained by `lncli listchaintxns` (see [above](#creating-json-files)).
Then, run the command `lnd-add-from-onchain-transactions` as follows:
```
BitBook₿ lnd-add-from-onchain-transactions /tmp/lnd-listchaintxns.json
Added information from 671 transactions
```


### pool leases

In addition to the `lnd` commands above, BitBook can also add information obtained from channel leases created using
[pool](https://github.com/lightninglabs/pool). For these channels the transaction description is set to
"Opening Channel with {pubkey}", the channel address is marked as owned, and the channel address description is set to
"Lightning-Channel with {pubkey}".

Based on the information provided by `pool` BitBook is also able to identify the change address, which is marked as
owned. If the pool account id is already set for an input address, this description is also used for the change address.
Otherwise, BitBook sets the description "pool account" for the output.

You first need to create the JSON file using `pool auction leases > pool-auction-leases.json`.
Then, after transferring the file to the computer running BitBook, you can use the `pool-add-from-leases` command:

```
BitBook₿ pool-add-from-leases /tmp/pool-auction-leases.json
Added information for 12 leases
```

Note: Currently only "ask" orders are supported. If you can provide examples for "bid" orders, please share them
(see [Contributing](contributing.md))!
