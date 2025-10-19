# API Routes Documentation

This document provides comprehensive documentation for all API endpoints in the Autobank system.

## Authentication

All API endpoints require authentication via Bearer token in the Authorization header:

```
Authorization: Bearer <access_token>
```

**Note**: In development mode (`environment=dev`), authentication is disabled for easier testing.

## Base URL

All endpoints are prefixed with the base URL: `http://localhost:8080` (development)

---

## Authentication Endpoints

### Get Current User
**Endpoint**: `GET /api/auth/getuser`

**Description**: Retrieves information about the currently authenticated user.

**Headers**:
```
Authorization: Bearer <access_token>
```

**Response**:
```json
{
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "fullname": "John Doe",
    "onlineid": "johndoe",
    "isadmin": false
  },
  "committees": [
    {
      "id": "uuid",
      "name": "Committee Name"
    }
  ]
}
```

**Status Codes**:
- `200 OK`: Success
- `400 Bad Request`: Error occurred

---

## Receipt Endpoints

### Create Receipt
**Endpoint**: `POST /api/receipt/create`

**Description**: Creates a new receipt with optional attachments.

**Headers**:
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "receipt": {
    "amount": 150.50,
    "description": "Office supplies for committee meeting",
    "name": "Office Supplies Receipt",
    "committee_id": "uuid"
  },
  "attachments": [
    "base64_encoded_file_1",
    "base64_encoded_file_2"
  ],
  "receiptPaymentInformation": {
    "cardnumber": "1234567890123456",
    "accountnumber": "12345678901",
    "usedOnlineCard": true
  }
}
```

**Response**:
```json
{
  "id": "uuid",
  "amount": 150.50,
  "name": "Office Supplies Receipt",
  "description": "Office supplies for committee meeting",
  "committee": {
    "id": "uuid",
    "name": "Committee Name"
  },
  "user": {
    "id": "uuid",
    "fullname": "John Doe"
  },
  "createdat": "2023-12-01T10:30:00",
  "attachments": [
    {
      "id": "uuid",
      "name": "receipt_image.jpg"
    }
  ]
}
```

**Status Codes**:
- `200 OK`: Receipt created successfully
- `400 Bad Request`: Invalid request data

### Get All User Receipts
**Endpoint**: `GET /api/receipt/getall`

**Description**: Retrieves a paginated list of receipts for the authenticated user.

**Headers**:
```
Authorization: Bearer <access_token>
```

**Query Parameters**:
- `page` (optional, default: 0): Page number for pagination
- `count` (optional, default: 10): Number of items per page
- `status` (optional): Filter by receipt status
- `committee` (optional): Filter by committee name
- `search` (optional): Search term for receipt name/description
- `sortOrder` (optional): Sort order (`ASC` or `DESC`)
- `sortField` (optional): Field to sort by

**Example Request**:
```
GET /api/receipt/getall?page=0&count=20&status=PENDING&committee=IT&sortOrder=DESC&sortField=createdat
```

**Response**:
```json
{
  "receipts": [
    {
      "id": "uuid",
      "amount": 150.50,
      "name": "Office Supplies Receipt",
      "description": "Office supplies for committee meeting",
      "committee": {
        "id": "uuid",
        "name": "IT Committee"
      },
      "createdat": "2023-12-01T10:30:00",
      "status": "PENDING"
    }
  ],
  "totalCount": 45,
  "currentPage": 0,
  "totalPages": 3
}
```

**Status Codes**:
- `200 OK`: Success
- `400 Bad Request`: Invalid query parameters

### Get Specific Receipt
**Endpoint**: `GET /api/receipt/get/{id}`

**Description**: Retrieves detailed information about a specific receipt.

**Headers**:
```
Authorization: Bearer <access_token>
```

**Path Parameters**:
- `id`: Receipt UUID

**Response**:
```json
{
  "id": "uuid",
  "amount": 150.50,
  "name": "Office Supplies Receipt",
  "description": "Office supplies for committee meeting",
  "committee": {
    "id": "uuid",
    "name": "IT Committee"
  },
  "user": {
    "id": "uuid",
    "fullname": "John Doe",
    "email": "john@example.com"
  },
  "createdat": "2023-12-01T10:30:00",
  "card_number": "****1234",
  "account_number": "****8901",
  "attachments": [
    {
      "id": "uuid",
      "name": "receipt_image.jpg"
    }
  ],
  "reviews": [
    {
      "id": "uuid",
      "status": "APPROVED",
      "comment": "Receipt approved",
      "createdat": "2023-12-02T09:15:00",
      "reviewer": {
        "fullname": "Admin User"
      }
    }
  ]
}
```

**Status Codes**:
- `200 OK`: Success
- `400 Bad Request`: Receipt not found or access denied

---

## Admin Receipt Endpoints

**Note**: All admin endpoints require admin privileges (membership in the admin committee).

### Get All Receipts (Admin)
**Endpoint**: `GET /api/admin/receipt/all`

**Description**: Retrieves all receipts in the system (admin view).

**Headers**:
```
Authorization: Bearer <access_token>
```

**Query Parameters**: Same as user receipt list endpoint

**Response**: Same structure as user receipt list

**Status Codes**:
- `200 OK`: Success
- `403 Forbidden`: User is not an admin
- `400 Bad Request`: Invalid query parameters

### Get Specific Receipt (Admin)
**Endpoint**: `GET /api/admin/receipt/get/{id}`

**Description**: Retrieves detailed information about any receipt (admin view).

**Headers**:
```
Authorization: Bearer <access_token>
```

**Path Parameters**:
- `id`: Receipt UUID

**Response**: Same structure as user receipt detail

**Status Codes**:
- `200 OK`: Success
- `403 Forbidden`: User is not an admin
- `400 Bad Request`: Receipt not found

### Review Receipt
**Endpoint**: `POST /api/admin/receipt/review`

**Description**: Creates a review (approval or denial) for a receipt.

**Headers**:
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "receipt_id": "uuid",
  "status": "APPROVED",
  "comment": "Receipt approved. All documentation is in order."
}
```

**Fields**:
- `receipt_id`: UUID of the receipt to review
- `status`: Either `"APPROVED"` or `"DENIED"`
- `comment`: Review comment (required)

**Response**:
```json
{
  "id": "uuid",
  "receipt_id": "uuid",
  "status": "APPROVED",
  "comment": "Receipt approved. All documentation is in order.",
  "createdat": "2023-12-02T09:15:00",
  "reviewer": {
    "id": "uuid",
    "fullname": "Admin User"
  }
}
```

**Status Codes**:
- `200 OK`: Review created successfully
- `403 Forbidden`: User is not an admin
- `400 Bad Request`: Invalid request data

---

## Committee Endpoints

### Get All Committees
**Endpoint**: `GET /api/committee/all`

**Description**: Retrieves a list of all committees in the system.

**Headers**:
```
Authorization: Bearer <access_token>
```

**Response**:
```json
[
  {
    "id": "uuid",
    "name": "IT Committee"
  },
  {
    "id": "uuid",
    "name": "Finance Committee"
  }
]
```

**Status Codes**:
- `200 OK`: Success
- `400 Bad Request`: Error occurred

### Get User and Committees
**Endpoint**: `GET /api/committee/user`

**Description**: Retrieves user information along with their associated committees.

**Headers**:
```
Authorization: Bearer <access_token>
```

**Response**:
```json
{
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "fullname": "John Doe",
    "onlineid": "johndoe",
    "isadmin": false
  },
  "committees": [
    {
      "id": "uuid",
      "name": "IT Committee"
    }
  ]
}
```

**Status Codes**:
- `200 OK`: Success
- `400 Bad Request`: Error occurred

---

## Economic Request Endpoints

**Note**: Economic request functionality is partially implemented.

### Get Economic Request
**Endpoint**: `GET /api/economicrequest/get/{id}` (Currently disabled)

**Description**: Would retrieve a specific economic request by ID.

**Headers**:
```
Authorization: Bearer <access_token>
```

**Path Parameters**:
- `id`: Economic request ID

**Response**: Economic request details (structure TBD)

---

## Error Responses

All endpoints may return the following error responses:

### 400 Bad Request
```json
{
  "error": "Bad Request",
  "message": "Invalid request data"
}
```

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Authentication required"
}
```

### 403 Forbidden
```json
{
  "error": "Forbidden",
  "message": "Admin privileges required"
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

---

## Rate Limiting

Currently, no rate limiting is implemented. This may be added in future versions.

## Versioning

The API is currently unversioned. Future versions may include version prefixes (e.g., `/api/v1/`).
