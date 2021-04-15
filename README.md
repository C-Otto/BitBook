# BitBook

### Motivation
I started tracking my coins with spreadsheets and later migrated to a simple form-based web application.
Despite a lot of effort, I was never sure if I missed anything, or if any information I added was incorrect.
Is there a Satoshi that slipped through the cracks? Did I swap numbers in some amount? What about fees? Do I still have
coins somewhere on an old phone, or in a forgotten test installation?

The purpose of BitBook is to track your own coins by organizing on-chain
transactions and their corresponding addresses.
If you are able to spend coins for an address, you can mark the address as *owned*.
In all other cases you can mark addresses as *foreign*.
BitBook helps you identify related addresses and transactions where the ownership status is still unknown,
so that you can add the missing information and, over time, create a complete picture of your owned coins.

### Example Run
![Example Run](documentation/bitbook.gif)

See [documentation/example.md](documentation/example.md) for an example run that demonstrates the
key features of BitBook.

### Important Note for custodial accounts
Addresses of custodial accounts where you rely on another entity to create
transactions on your behalf should not be considered owned.
As an example, if you transfer coins from your personal wallet to "your" account
on an exchange, assuming the exchange is in control of the corresponding addresses,
you should mark your account's addresses as foreign and track movement of the coins
in some other way.
If you transfer the coins back to your own wallet, you can continue using BitBook by
marking the address as owned.

### How do I start BitBook?
Please have a look at the [Frequently Asked Questions (FAQ)](documentation/faq.md).

### Further Information
* [Example](documentation/example.md)
* [Commands](documentation/commands.md)
* [Frequently Asked Questions (FAQ)](documentation/faq.md)
* [Feature Requests and Bug Reports](documentation/features_and_bugs.md)
* [Known Limitations](documentation/limitations.md)
* [Future Ideas](documentation/ideas.md)
* [lnd support](documentation/lnd.md)
* [Technical Aspects](documentation/technical.md)
* [Contributing to BitBook](documentation/contributing.md)
