create table ADDRESS_TRANSACTIONS
(
    ADDRESS                     VARCHAR(255) not null
        primary key,
    LAST_CHECKED_AT_BLOCKHEIGHT INTEGER      not null
);

create table ADDRESS_OWNERSHIP
(
    ADDRESS          VARCHAR(255) not null
        primary key,
    OWNERSHIP_STATUS VARCHAR(255)
);

create table ADDRESS_TRANSACTIONS_JPA_DTO_TRANSACTION_HASHES
(
    ADDRESS_TRANSACTIONS_JPA_DTO_ADDRESS VARCHAR(255) not null,
    TRANSACTION_HASHES                   VARCHAR(255),
    constraint FKNPLWYPO3INM8AHQ6A9MI5EAGN
        foreign key (ADDRESS_TRANSACTIONS_JPA_DTO_ADDRESS) references ADDRESS_TRANSACTIONS (ADDRESS)
);

create table ADDRESS_WITH_DESCRIPTION
(
    ADDRESS     VARCHAR(255) not null
        primary key,
    DESCRIPTION VARCHAR(255)
);

create table INPUTS
(
    ID             BIGINT not null
        primary key,
    SOURCE_ADDRESS VARCHAR(255),
    VALUE          BIGINT not null
);

create index IDXIOOJE4XO187QKRDTQ6OKNYEOW
    on INPUTS (SOURCE_ADDRESS);

create table OUTPUTS
(
    ID             BIGINT not null
        primary key,
    TARGET_ADDRESS VARCHAR(255),
    VALUE          BIGINT not null
);

create index IDXKK3XIYIDI8HGAAX5B5C4RN98K
    on OUTPUTS (TARGET_ADDRESS);

create table PRICES
(
    DATE  DATE not null
        primary key,
    PRICE DECIMAL(16, 8)
);

create table TRANSACTIONS
(
    HASH         VARCHAR(255) not null
        primary key,
    BLOCK_HEIGHT INTEGER      not null,
    FEES         BIGINT       not null,
    TIME         BIGINT       not null
);

create table TRANSACTIONS_INPUTS
(
    TRANSACTION_JPA_DTO_HASH VARCHAR(255) not null,
    INPUTS_ID                BIGINT       not null
        constraint UK_N75MRDL0TX30VMQE9LAV2WOCT
            unique,
    constraint FK12O65DNSM0TNHJWLEV43AXG96
        foreign key (TRANSACTION_JPA_DTO_HASH) references TRANSACTIONS (HASH),
    constraint FKOQXY5LIIG91FGTY7YICS5D88H
        foreign key (INPUTS_ID) references INPUTS (ID)
);

create table TRANSACTIONS_OUTPUTS
(
    TRANSACTION_JPA_DTO_HASH VARCHAR(255) not null,
    OUTPUTS_ID               BIGINT       not null
        constraint UK_FHIHE527GH79G8R45U4LRIFWX
            unique,
    constraint FKA22I3836F16KRQ712JLWU10QA
        foreign key (OUTPUTS_ID) references OUTPUTS (ID),
    constraint FKJKL8K6K4J4DUVGQKJJT9FIV9U
        foreign key (TRANSACTION_JPA_DTO_HASH) references TRANSACTIONS (HASH)
);

create table TRANSACTION_WITH_DESCRIPTION
(
    TRANSACTION_HASH VARCHAR(255) not null
        primary key,
    DESCRIPTION      VARCHAR(255)
);

