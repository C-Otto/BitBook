alter table TRANSACTIONS_INPUTS
    drop constraint FK12O65DNSM0TNHJWLEV43AXG96;
alter table TRANSACTIONS_INPUTS
    drop constraint UK_n75mrdl0tx30vmqe9lav2woct;
alter table TRANSACTIONS_INPUTS
    drop constraint FKoqxy5liig91fgty7yics5d88h;
ALTER TABLE TRANSACTIONS_INPUTS
    RENAME TO TRANSACTIONS_INPUTS_old;

alter table TRANSACTIONS_OUTPUTS
    drop constraint FKJKL8K6K4J4DUVGQKJJT9FIV9U;
alter table TRANSACTIONS_OUTPUTS
    drop constraint UK_fhihe527gh79g8r45u4lrifwx;
alter table TRANSACTIONS_OUTPUTS
    drop constraint FKa22i3836f16krq712jlwu10qa;
ALTER TABLE TRANSACTIONS_OUTPUTS
    RENAME TO TRANSACTIONS_OUTPUTS_old;

ALTER TABLE TRANSACTIONS
    RENAME TO TRANSACTIONS_old;

create table transactions
(
    chain        varchar(255) not null,
    hash         varchar(255) not null,
    block_height integer      not null,
    fees         bigint       not null,
    time         bigint       not null,
    primary key (chain, hash)
);

create table transactions_inputs
(
    transaction_jpa_dto_chain varchar(255) not null,
    transaction_jpa_dto_hash  varchar(255) not null,
    inputs_id                 bigint       not null
);
alter table transactions_inputs
    add constraint UK_n75mrdl0tx30vmqe9lav2woct unique (inputs_id);
alter table transactions_inputs
    add constraint FKoqxy5liig91fgty7yics5d88h foreign key (inputs_id) references inputs;
alter table transactions_inputs
    add constraint FKg3w9f0uygvmcp972uc3jaqiul foreign key (transaction_jpa_dto_chain, transaction_jpa_dto_hash) references transactions;

create table transactions_outputs
(
    transaction_jpa_dto_chain varchar(255) not null,
    transaction_jpa_dto_hash  varchar(255) not null,
    outputs_id                bigint       not null
);
alter table transactions_outputs
    add constraint UK_fhihe527gh79g8r45u4lrifwx unique (outputs_id);
alter table transactions_outputs
    add constraint FK7k2uw03l0rc8y2wdpm1756ahp foreign key (transaction_jpa_dto_chain, transaction_jpa_dto_hash) references transactions;
alter table transactions_outputs
    add constraint FKa22i3836f16krq712jlwu10qa foreign key (outputs_id) references outputs;

INSERT INTO transactions (chain, hash, block_height, fees, time)
SELECT 'BTC', hash, block_height, fees, time FROM transactions_old;

INSERT INTO transactions_inputs (transaction_jpa_dto_chain, transaction_jpa_dto_hash, inputs_id)
SELECT 'BTC', transaction_jpa_dto_hash, inputs_id FROM transactions_inputs_old;

INSERT INTO transactions_outputs (transaction_jpa_dto_chain, transaction_jpa_dto_hash, outputs_id)
SELECT 'BTC', transaction_jpa_dto_hash, outputs_id FROM transactions_outputs_old;

DROP TABLE transactions_inputs_old;
DROP TABLE transactions_outputs_old;
DROP TABLE transactions_old;