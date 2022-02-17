ALTER TABLE address_transactions_jpa_dto_transaction_hashes DROP CONSTRAINT FKnplwypo3inm8ahq6a9mi5eagn;
ALTER TABLE address_transactions_jpa_dto_transaction_hashes RENAME TO address_transactions_jpa_dto_transaction_hashes_old;
ALTER TABLE address_transactions RENAME TO address_transactions_old;
CREATE TABLE address_transactions
(
    address VARCHAR(255) NOT NULL,
    last_checked_at_blockheight INTEGER NOT NULL,
    chain VARCHAR(255) NOT NULL,
    PRIMARY KEY (address, chain)
);
CREATE TABLE address_transactions_jpa_dto_transaction_hashes
(
    address_transactions_jpa_dto_address VARCHAR(255) NOT NULL,
    address_transactions_jpa_dto_chain VARCHAR(255) NOT NULL,
    transaction_hashes VARCHAR(255)
);
ALTER TABLE address_transactions_jpa_dto_transaction_hashes ADD CONSTRAINT FK1icf40298o388rwlrvsfav8to FOREIGN KEY (address_transactions_jpa_dto_address, address_transactions_jpa_dto_chain) REFERENCES address_transactions;
INSERT INTO address_transactions (address, last_checked_at_blockheight, chain) SELECT address, last_checked_at_blockheight, 'BTC' FROM address_transactions_old;
INSERT INTO address_transactions_jpa_dto_transaction_hashes (address_transactions_jpa_dto_address, address_transactions_jpa_dto_chain, transaction_hashes) SELECT address_transactions_jpa_dto_address, 'BTC', transaction_hashes FROM address_transactions_jpa_dto_transaction_hashes_old;
DROP TABLE address_transactions_old;
DROP TABLE address_transactions_jpa_dto_transaction_hashes_old;