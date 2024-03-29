rootProject.name = "bitbook"
include("cli")
include("cli:base")
include("cli:lnd")
include("cli:ownership")
include("backend")
include("backend:price")
include("backend:blockheight")
include("backend:address-transactions")
include("backend:transaction")
include("backend:models")
include("backend:provider:all")
include("backend:provider:base")
include("backend:provider:bitaps")
include("backend:provider:bitcoind")
include("backend:provider:blockchaininfo")
include("backend:provider:blockchair")
include("backend:provider:blockcypher")
include("backend:provider:blockstreaminfo")
include("backend:provider:btccom")
include("backend:provider:electrs")
include("backend:provider:fullstackcash")
include("backend:provider:mempoolspace")
include("backend:request")
include("backend:request:models")
include("lnd")
include("ownership")

dependencyResolutionManagement {
    includeBuild("gradle/meta-plugins")
    repositories {
        mavenCentral()
    }
}
