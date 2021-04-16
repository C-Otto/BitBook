# Known Limitations
## Multisig Addresses
BitBook currently is unable to track a specific variant of multisig addresses
(used for escrows?).
Regular multisig addresses (addresses starting with `1...` or `3...`) are supported,
but those sometimes shown starting with `4...` or `m-...` are not. Help is appreciated!


Further information: https://bitcoin.stackexchange.com/q/37681/7214
### Example:
* Second output of `4c1df235ffd7642008989422aee5255e6312b4172b55d94e328fa99e99d727c7`
* [Blockcypher](https://live.blockcypher.com/btc/tx/4c1df235ffd7642008989422aee5255e6312b4172b55d94e328fa99e99d727c7/)
* [Blockchain.com](https://www.blockchain.com/btc/tx/4c1df235ffd7642008989422aee5255e6312b4172b55d94e328fa99e99d727c7)
* [Blockchair](https://blockchair.com/bitcoin/transaction/4c1df235ffd7642008989422aee5255e6312b4172b55d94e328fa99e99d727c7)

## Rate Limiting
BitBook needs to download price information, transaction hashes (for addresses) and transaction details from public APIs.
Most of these APIs do not allow many/frequent accesses without a purchased an API key.
As such, you may see error messages if a provider (API) failed.
If you need to download huge amounts of data (addresses with many transactions), BitBook might fail.
In this case it might help to try again later, possibly with a different IP address.

Also see [issue #5](https://github.com/C-Otto/BitBook/issues/5).

## Pagination
For addresses for which many transaction hashes are known, BitBook currently is unable to use most API's responses.
This implementation detail is addressed in [issue #6](https://github.com/C-Otto/BitBook/issues/6).