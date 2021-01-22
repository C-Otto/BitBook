# Technical Aspects
In addition to tracking my coins, I started this project to experiment with several
tools and techniques. As such, BitBook is based on recent versions of Java and Gradle,
and incorporates several tools like Errorprone that aren't strictly necessary.

I appreciate constructive criticisms about the choice of frameworks, implementation 
details, and just about anything else. **Please help me learn and improve!
Please create issues, provide pull requests, send mails, reach out via Twitter, ...!**

### Database
BitBook creates and uses a single-file H2 database. So far I haven't seen the need to
use a "proper" database.
Furthermore, this simplifies the setup and makes backups easier to do.
Similarly, there is no support for database migrations (Flyway, Liquibase), yet. 

### Java 16
At the time of writing this, Java 16 is the current Java version.
I picked this version mostly in order to experiment with tool support, whereas features like
text blocks are nice additions. From a technical point of view, Java 11 support shouldn't be too hard to do.
Due to limitations in tools like Nullaway, I don't use records. Hopefully this changes soon!

### Gradle 7
Gradle 6 does not support Java 16, and Gradle 7 is not released, yet. As such, BitBook
uses the most current release candidate which is Gradle 7 RC2. The project is split into
several subprojects, making use of test fixtures and different source sets for tests and
integration tests. The build can be parallelized on suitable hardware, and by using build
caches incremental builds should be rather fast.

### Requests
BitBook uses several APIs to download address/transaction information.
In order to allow for several APIs and deal with failures while providing a good user experience,
there's quite a lot of code that helps accomplish this.

#### APIs and Providers
A provider is responsible to provide the requested information, which usually means sending HTTP (REST) requests to
some API and returning the parsed responses as Java objects.
We use Feign to implement the actual HTTP clients, and Jackson for deserialization.
Additionally, we use resilience4j to add circuit-breakers and rate-limiters.

Currently, the following providers are implemented:
```
 A: transaction hashes linked to address
 T: transaction details (inputs/outputs)
 H: current block height of chain
 ```

 * kraken.com: Euro prices
 * bitaps.com: ATH
 * blockchain.info: TH
 * blockchair.com: AT
 * blockcypher.com: AT
 * blockstream.info: A
 * btc.com: AT
 * mempool.space: A
 * smartbit.com.au: AT
 * sochain: A

#### Requests and Queue
For each type of result (e.g. transaction details), a `PrioritizingProvider` implementation
is responsible for transforming requests into results. Each request has a priority (at the moment either 'standard'
or 'lowest'), so that higher-priority requests are served earlier than others.
The code automatically merges duplicates in the queue, attaches new requests to in-flight requests, and picks
providers (APIs) based on their recent performance/failure rates.

Most "visible" requests also cause "invisible" requests that are served in the background (using the 'lowest' priority).
The results are persisted in the database, so that future requests can be served much faster.
As an example, when requesting details about a transaction, the price at time of the transaction is requested in the
background.

The request classes make use of `CompletableFuture` and `Consumer<R>` to forward request results to the code that
requested the information (to show it to the user) or should do something with it (persist results to database).

As there may be several requests for the same piece of information (which may be merged/re-used/...), the code
responsible for the queue organization and result forwarding is rather complex.
To avoid concurrent database modifications, the classes are tweaked so that results are returned sequentially, i.e.
NOT in parallel.