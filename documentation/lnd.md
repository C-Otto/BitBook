# lnd Support
BitBook has basic support for [lnd (lightning network daemon)](https://github.com/lightningnetwork/lnd) so that you can
add ownership information and helpful descriptions for transactions/addresses managed by your lnd instance without
entering these manually.

This is implemented by parsing JSON files generated by the `lncli` command line tool. As such, this involves a bit of
manual effort, but it also keeps things simple. By copying these files youc an easily have BitBook and lnd run on
different computers. Furthermore, by not giving BitBook access to lnd, there's no risk of undesired side effects.

### Unspent Outputs
Using `lncli listunspent` you can get a list of all unspent outputs managed by lnd.
As such, BitBook should mark these addresses as owned (with the description "lnd").
You can do this using the command `lnd-add-from-unspent-outputs`:

1. first create the JSON file using lnd: `$ lncli listunspent > lnd-unspent.json`
2. transfer the JSON file to the host where you are running BitBook: `$ scp server:/home/lnd/lnd-unspent.json /tmp/`
3. start BitBook

Then you can use the command as follows:
```
BitBook$ lnd-add-from-unspent-outputs /tmp/lnd-unspent.json
Marked 6 addresses as owned by lnd
```

### Sweeps
After a channel is closed, your funds (on the "local" side of the channel) are sent to an address that is not derived
from your lnd wallet seed. To avoid loss of funds, lnd automatically "sweeps" those funds to another address. As such,
there may be several sweep transactions that transfer funds from one address to another.

BitBook offers the command `lnd-add-from-sweeps` which parses lnd sweep information and does the following for each
transaction:

 - sets the transaction description to "lnd sweep transaction"
 - sets the source and target addresses descriptions to "lnd"
 - marks both addresses as owned

To run the command:
1. first create the JSON file using lnd: `$ lncli wallet listsweeps > lnd-sweeps.json`
2. transfer the JSON file to the host where you are running BitBook: `$ scp server:/home/lnd/lnd-sweeps.json /tmp/`
3. start BitBook
   
Then you can use the command as follows:
```
BitBook$ lnd-add-from-sweeps /tmp/lnd-sweeps.json
Added information for 86 sweep transactions
```