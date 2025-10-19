# Database Schema Documentation

This document describes the database schema for the Autobank system, including all tables, relationships, and constraints.

## Database Overview

- **Database Type**: Microsoft SQL Server (Azure)
- **Primary Key Strategy**: UUID (UNIQUEIDENTIFIER)
- **Naming Convention**: Lowercase table and column names
- **Character Encoding**: UTF-8

## Entity Relationship Diagram

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ onlineuser  │    │ committee   │    │ receipt     │
├─────────────┤    ├─────────────┤    ├─────────────┤
│ id (PK)     │    │ id (PK)     │    │ id (PK)     │
│ email       │    │ name        │    │ amount      │
│ onlineid    │    └─────────────┘    │ committee_id│──┐
│ fullname    │                       │ name        │  │
│ isadmin     │    ┌─────────────┐    │ description │  │
│ lastupdated │    │ attachment  │    │ createdat   │  │
└─────────────┘    ├─────────────┤    │ onlineuser_id│──┘
       │           │ id (PK)     │    │ card_number │
       │           │ receipt_id  │──┐ │ account_num │
       │           │ name        │  │ └─────────────┘
       │           └─────────────┘  │        │
       │                            │        │
       │           ┌─────────────┐  │        │
       │           │receiptreview│  │        │
       │           ├─────────────┤  │        │
       │           │ id (PK)     │  │        │
       │           │ receipt_id  │──┘        │
       │           │ status      │           │
       │           │ comment     │           │
       │           │ createdat   │           │
       │           │ onlineuser_id│──────────┘
       │           └─────────────┘
       │
       │           ┌─────────────┐
       │           │economicreq  │
       │           ├─────────────┤
       │           │ id (PK)     │
       │           │ subject     │
       │           │ purpose     │
       │           │ date        │
       │           │ duration    │
       │           │ description │
       │           │ amount      │
       │           │ personcount │
       │           │ names       │
       │           │ paymentdesc │
       │           │ otherinfo   │
       │           │ createdat   │
       │           │ onlineuser_id│──────────┘
       │           └─────────────┘
       │                  │
       │           ┌─────────────┐
       │           │economicreq  │
       │           │   review    │
       │           ├─────────────┤
       │           │ id (PK)     │
       │           │ economicreq │──┘
       │           │   _id       │
       │           │ status      │
       │           │ comment     │
       │           │ createdat   │
       │           │ onlineuser_id│──────────┘
       │           └─────────────┘
       │
       │           ┌─────────────┐
       │           │economicreq  │
       │           │ attachment  │
       │           ├─────────────┤
       │           │ id (PK)     │
       │           │ economicreq │──┘
       │           │   _id       │
       │           │ name        │
       │           └─────────────┘
```

## Table Definitions

### onlineuser

Stores user information and authentication details.

```sql
CREATE TABLE onlineuser (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    email VARCHAR(55) NOT NULL,
    onlineid VARCHAR(55) NOT NULL,
    fullname VARCHAR(55) NOT NULL,
    isadmin BIT,
    lastupdated DATETIME
);
```

**Columns:**
- `id`: Unique identifier for the user (UUID)
- `email`: User's email address (max 55 chars)
- `onlineid`: User's Online ID/username (max 55 chars)
- `fullname`: User's full name (max 55 chars)
- `isadmin`: Boolean flag indicating admin privileges
- `lastupdated`: Timestamp of last user data update

**Indexes:**
- Primary key on `id`
- Consider adding unique index on `email` and `onlineid`

### committee

Stores committee/organization information.

```sql
CREATE TABLE committee (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    name VARCHAR(55) NOT NULL
);
```

**Columns:**
- `id`: Unique identifier for the committee (UUID)
- `name`: Committee name (max 55 chars)

**Indexes:**
- Primary key on `id`
- Consider adding unique index on `name`

### receipt

Main table for storing receipt information.

```sql
CREATE TABLE receipt (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    amount DECIMAL(10, 2) NOT NULL,
    committee_id UNIQUEIDENTIFIER NOT NULL REFERENCES committee (id),
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500) NOT NULL,
    createdat DATETIME NOT NULL,
    onlineuser_id UNIQUEIDENTIFIER NOT NULL REFERENCES onlineuser (id),
    card_number VARCHAR(16) NOT NULL,
    account_number VARCHAR(55) NOT NULL
);
```

**Columns:**
- `id`: Unique identifier for the receipt (UUID)
- `amount`: Receipt amount (decimal with 2 decimal places)
- `committee_id`: Foreign key to committee table
- `name`: Receipt name/title (max 255 chars)
- `description`: Receipt description (max 500 chars)
- `createdat`: Timestamp when receipt was created
- `onlineuser_id`: Foreign key to user who created the receipt
- `card_number`: Card number used for payment (max 16 chars)
- `account_number`: Account number (max 55 chars)

**Foreign Keys:**
- `committee_id` → `committee(id)`
- `onlineuser_id` → `onlineuser(id)`

**Indexes:**
- Primary key on `id`
- Index on `committee_id` for filtering
- Index on `onlineuser_id` for user receipts
- Index on `createdat` for sorting

### attachment

Stores file attachment information for receipts.

```sql
CREATE TABLE attachment (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    receipt_id UNIQUEIDENTIFIER NOT NULL REFERENCES receipt (id),
    name VARCHAR(255) NOT NULL
);
```

**Columns:**
- `id`: Unique identifier for the attachment (UUID)
- `receipt_id`: Foreign key to the associated receipt
- `name`: Filename of the attachment (max 255 chars)

**Foreign Keys:**
- `receipt_id` → `receipt(id)`

**Indexes:**
- Primary key on `id`
- Index on `receipt_id` for receipt attachments

### receiptreview

Stores review information for receipts (approval/denial).

```sql
CREATE TABLE receiptreview (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    receipt_id UNIQUEIDENTIFIER NOT NULL REFERENCES receipt (id),
    status VARCHAR(20) NOT NULL CHECK (status IN ('APPROVED', 'DENIED')),
    comment VARCHAR(500) NOT NULL,
    createdat DATETIME NOT NULL,
    onlineuser_id UNIQUEIDENTIFIER NOT NULL REFERENCES onlineuser (id)
);
```

**Columns:**
- `id`: Unique identifier for the review (UUID)
- `receipt_id`: Foreign key to the reviewed receipt
- `status`: Review status - either 'APPROVED' or 'DENIED'
- `comment`: Review comment/reason (max 500 chars)
- `createdat`: Timestamp when review was created
- `onlineuser_id`: Foreign key to the admin who created the review

**Foreign Keys:**
- `receipt_id` → `receipt(id)`
- `onlineuser_id` → `onlineuser(id)`

**Constraints:**
- `status` must be either 'APPROVED' or 'DENIED'

**Indexes:**
- Primary key on `id`
- Index on `receipt_id` for receipt reviews
- Index on `onlineuser_id` for reviewer history

### economicrequest

Stores economic request submissions.

```sql
CREATE TABLE economicrequest (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    subject VARCHAR(55) NOT NULL,
    purpose VARCHAR(500) NOT NULL,
    date DATE NOT NULL,
    duration VARCHAR(255) NOT NULL,
    description VARCHAR(500) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    personcount INT NOT NULL,
    names VARCHAR(500) NOT NULL,
    paymentdescription VARCHAR(255) NOT NULL,
    otherinformation VARCHAR(500) NOT NULL,
    createdat DATETIME NOT NULL,
    onlineuser_id UNIQUEIDENTIFIER NOT NULL REFERENCES onlineuser (id)
);
```

**Columns:**
- `id`: Unique identifier for the economic request (UUID)
- `subject`: Request subject/title (max 55 chars)
- `purpose`: Purpose of the request (max 500 chars)
- `date`: Date for the economic request
- `duration`: Duration information (max 255 chars)
- `description`: Detailed description (max 500 chars)
- `amount`: Requested amount (decimal with 2 decimal places)
- `personcount`: Number of people involved
- `names`: Names of people involved (max 500 chars)
- `paymentdescription`: Payment description (max 255 chars)
- `otherinformation`: Additional information (max 500 chars)
- `createdat`: Timestamp when request was created
- `onlineuser_id`: Foreign key to user who created the request

**Foreign Keys:**
- `onlineuser_id` → `onlineuser(id)`

**Indexes:**
- Primary key on `id`
- Index on `onlineuser_id` for user requests
- Index on `createdat` for sorting

### economicrequestreview

Stores review information for economic requests.

```sql
CREATE TABLE economicrequestreview (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    economicrequest_id UNIQUEIDENTIFIER NOT NULL REFERENCES economicrequest (id),
    status VARCHAR(20) NOT NULL CHECK (status IN ('APPROVED', 'DENIED')),
    comment VARCHAR(500) NOT NULL,
    createdat DATETIME NOT NULL,
    onlineuser_id UNIQUEIDENTIFIER NOT NULL REFERENCES onlineuser (id)
);
```

**Columns:**
- `id`: Unique identifier for the review (UUID)
- `economicrequest_id`: Foreign key to the reviewed economic request
- `status`: Review status - either 'APPROVED' or 'DENIED'
- `comment`: Review comment/reason (max 500 chars)
- `createdat`: Timestamp when review was created
- `onlineuser_id`: Foreign key to the admin who created the review

**Foreign Keys:**
- `economicrequest_id` → `economicrequest(id)`
- `onlineuser_id` → `onlineuser(id)`

**Constraints:**
- `status` must be either 'APPROVED' or 'DENIED'

### economicrequestattachment

Stores file attachment information for economic requests.

```sql
CREATE TABLE economicrequestattachment (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
    economicrequest_id UNIQUEIDENTIFIER NOT NULL REFERENCES economicrequest (id),
    name VARCHAR(255) NOT NULL
);
```

**Columns:**
- `id`: Unique identifier for the attachment (UUID)
- `economicrequest_id`: Foreign key to the associated economic request
- `name`: Filename of the attachment (max 255 chars)

**Foreign Keys:**
- `economicrequest_id` → `economicrequest(id)`

## Data Relationships

### One-to-Many Relationships

1. **onlineuser → receipt**: One user can create many receipts
2. **committee → receipt**: One committee can have many receipts
3. **receipt → attachment**: One receipt can have many attachments
4. **receipt → receiptreview**: One receipt can have many reviews
5. **onlineuser → receiptreview**: One admin can create many reviews
6. **onlineuser → economicrequest**: One user can create many economic requests
7. **economicrequest → economicrequestreview**: One request can have many reviews
8. **economicrequest → economicrequestattachment**: One request can have many attachments

### Business Rules

1. **Receipt Reviews**: A receipt can have multiple reviews, but typically only one final review
2. **Admin Privileges**: Only users with `isadmin = 1` can create reviews
3. **Committee Association**: All receipts must be associated with a committee
4. **Attachment Storage**: Actual file content is stored in Azure Blob Storage, only metadata in database
5. **UUID Generation**: All primary keys use SQL Server's `NEWID()` function for UUID generation

## Performance Considerations

### Recommended Indexes

```sql
-- User lookups
CREATE INDEX IX_onlineuser_email ON onlineuser(email);
CREATE INDEX IX_onlineuser_onlineid ON onlineuser(onlineid);

-- Receipt queries
CREATE INDEX IX_receipt_committee_id ON receipt(committee_id);
CREATE INDEX IX_receipt_onlineuser_id ON receipt(onlineuser_id);
CREATE INDEX IX_receipt_createdat ON receipt(createdat DESC);

-- Review queries
CREATE INDEX IX_receiptreview_receipt_id ON receiptreview(receipt_id);
CREATE INDEX IX_receiptreview_createdat ON receiptreview(createdat DESC);

-- Attachment queries
CREATE INDEX IX_attachment_receipt_id ON attachment(receipt_id);
CREATE INDEX IX_economicrequestattachment_economicrequest_id ON economicrequestattachment(economicrequest_id);

-- Economic request queries
CREATE INDEX IX_economicrequest_onlineuser_id ON economicrequest(onlineuser_id);
CREATE INDEX IX_economicrequest_createdat ON economicrequest(createdat DESC);
```

### Query Optimization

1. **Pagination**: Use `OFFSET` and `FETCH NEXT` for efficient pagination
2. **Filtering**: Indexes on commonly filtered columns (committee_id, status, createdat)
3. **Joins**: Proper foreign key indexes for efficient joins
4. **Search**: Consider full-text search for description fields if needed

## Data Migration Considerations

1. **UUID Compatibility**: Ensure UUID generation is consistent across environments
2. **Date Formats**: Use ISO 8601 format for date/datetime fields
3. **Character Limits**: Validate data length before insertion
4. **Foreign Key Constraints**: Ensure referential integrity during data migration
5. **Default Values**: Handle NULL values appropriately, especially for optional fields

## Security Considerations

1. **Sensitive Data**: Card numbers and account numbers should be encrypted or masked
2. **Audit Trail**: Consider adding audit columns (created_by, modified_by, modified_at)
3. **Soft Deletes**: Consider implementing soft deletes instead of hard deletes
4. **Data Retention**: Implement data retention policies for old records
