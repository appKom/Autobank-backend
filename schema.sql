CREATE TABLE onlineuser
(
    id       UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    email    VARCHAR(55) NOT NULL,
    onlineid VARCHAR(55) NOT NULL,
    fullname VARCHAR(55) NOT NULL,
    isadmin  BIT         ,
    lastupdated DATETIME   ,
);

CREATE TABLE committee
(
    id   UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    name VARCHAR(55) NOT NULL
);

CREATE TABLE receipt
(
    id            UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    amount        DECIMAL(10, 2) NOT NULL,
    committee_id  UNIQUEIDENTIFIER             NOT NULL references committee (id),
    name          VARCHAR(255)   NOT NULL,
    description   VARCHAR(500)   NOT NULL,
    createdat     DATETIME       NOT NULL,
    onlineuser_id UNIQUEIDENTIFIER             NOT NULL references onlineuser (id),
    card_number VARCHAR(16) NOT NULL,
    account_number VARCHAR(55) NOT NULL
);

CREATE TABLE attachment
(
    id        UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    receipt_id UNIQUEIDENTIFIER           NOT NULL references receipt (id),
    name       VARCHAR(255) NOT NULL
);


CREATE TABLE economicrequest
(
    id                UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    subject            VARCHAR(55)    NOT NULL,
    purpose            VARCHAR(500)   NOT NULL,
    date               DATE           NOT NULL,
    duration           VARCHAR(255)   NOT NULL,
    description        VARCHAR(500)   NOT NULL,
    amount             DECIMAL(10, 2) NOT NULL,
    personcount        INT            NOT NULL,
    names              VARCHAR(500)   NOT NULL,
    paymentdescription varchar(255)   NOT NULL,
    otherinformation   VARCHAR(500)   NOT NULL,
    createdat          DATETIME       NOT NULL,
    onlineuser_id      UNIQUEIDENTIFIER             NOT NULL references onlineuser (id)
);

CREATE TABLE receiptreview
(
    id            UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    receipt_id    UNIQUEIDENTIFIER          NOT NULL references receipt (id),
    status        VARCHAR(20)  NOT NULL CHECK (status IN ('APPROVED', 'DENIED')),
    comment       VARCHAR(500) NOT NULL,
    createdat     DATETIME     NOT NULL,
    onlineuser_id UNIQUEIDENTIFIER           NOT NULL references onlineuser (id)
);

CREATE TABLE economicrequestreview
(
    id           UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    economicrequest_id    UNIQUEIDENTIFIER          NOT NULL references economicrequest (id),
    status        VARCHAR(20)  NOT NULL CHECK (status IN ('APPROVED', 'DENIED')),
    comment       VARCHAR(500) NOT NULL,
    createdat     DATETIME     NOT NULL,
    onlineuser_id UNIQUEIDENTIFIER           NOT NULL references onlineuser (id)
);

CREATE TABLE economicrequestattachment
(
    id                UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    economicrequest_id UNIQUEIDENTIFIER           NOT NULL references economicrequest (id),
    name              VARCHAR(255) NOT NULL
);