# ETS Application API Documentation

## Overview
This document provides comprehensive documentation for the ETS (Electronic Transport System) application's REST APIs. The application is a cab/vehicle booking system that allows users to schedule rides, manage drivers, and vehicles.

## Base URL
```
http://localhost:8081
```

## Authentication
The API uses a simple authentication mechanism. User login is handled through the `/users/etsLogin` endpoint.

## API Endpoints

### Scheduling and Booking

#### Get Cab Options
```http
POST /schedule/etsCab1
```
Provides available cab options based on pickup and drop locations with pricing.

**Request Parameters**
| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| pickUpLocation | String | Yes | Pickup location |
| dropLocation | String | Yes | Drop location |
| time | String | Yes | Pickup time in HH:mm format |
| returnTime | String | No | Return time in HH:mm format |
| shiftTime | String | Yes | Shift time preference |
| dates | List<LocalDate> | Yes | List of dates for scheduling |

**Response**
```json
{
  "pickUpLocation": "string",
  "dropLocation": "string",
  "time": "string",
  "returnTime": "string",
  "shiftTime": "string",
  "destinationState": "string",
  "destinationCity": "string",
  "sourceState": "string",
  "sourceCity": "string",
  "hatchback": "number",
  "sedan": "number",
  "suv": "number",
  "distace": "number",
  "dates": ["yyyy-MM-dd"]
}
```

**Error Responses**
- Invalid time format: 400 Bad Request
- Booking less than 3 hours in advance: 400 Bad Request

#### Calculate Cab Fare
```http
POST /schedule/cabFinder
```
Calculates the fare for different types of cabs based on distance and rates.

**Request Parameters**
| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| pickUpLocation | String | Yes | Pickup location |
| dropLocation | String | Yes | Drop location |
| time | String | Yes | Pickup time |
| returnTime | String | Yes | Return time |
| shiftTime | String | Yes | Shift time |
| distance | String | Yes | Distance in kilometers |
| hatchback | String | Yes | Rate per km for hatchback |
| sedan | String | Yes | Rate per km for sedan |
| suv | String | Yes | Rate per km for SUV |

**Response**
```json
{
  "pickUpLocation": "string",
  "dropLocation": "string",
  "distance": "number",
  "time": "string",
  "returnTime": "string",
  "shiftTime": "string",
  "hatchbackRate": "number",
  "sedanRate": "number",
  "suvRate": "number",
  "hatchbackFare": "number",
  "sedanFare": "number",
  "suvFare": "number"
}
```

#### Generate Invoice
```http
POST /schedule/invoice
```
Generates an invoice with fare, GST, and service charge details.

**Request Parameters**
| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| baseFare | String | Yes | Base fare amount |
| cabType | String | Yes | Type of cab selected |

**Response**
```json
{
  "baseFare": "number",
  "cabType": "string",
  "gst": "number",
  "serviceCharge": "number",
  "totalAmount": "number"
}
```

#### Confirm Booking
```http
POST /schedule/etsBookingConfirm
```
Confirms a booking and sends a confirmation email to the user.

**Request Parameters**
| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| pickUpLocation | String | No | Pickup location |
| dropLocation | String | No | Drop location |
| time | String | No | Pickup time |
| returnTime | String | No | Return time |
| cabType | String | No | Type of cab |
| finalAmount | String | No | Final amount to be paid |
| baseAmount | String | No | Base amount of the fare |
| serviceCharge | String | No | Service charge amount |
| gst | String | No | GST amount |
| distance | String | No | Distance in kilometers |
| sittingExcepatation | Integer | No | Number of seats expected |
| parnterSharing | Integer | No | Partner sharing preference |
| dates | List<LocalDate> | No | List of dates for scheduling |
| userId | Integer | Yes | User ID |

**Response**
```json
{
  "status": "string",
  "message": "string",
  "bookingId": "number",
  "booking": {
    "id": "number",
    "bookId": "string",
    "pickUpLocation": "string",
    "dropLocation": "string",
    "time": "string",
    "returnTime": "string",
    "cabType": "string",
    "status": "string",
    "carRentalUserId": "number",
    "finalAmount": "string",
    "baseAmount": "string",
    "serviceCharge": "string",
    "gst": "string",
    "distance": "string",
    "sittingExcepatation": "number",
    "partnerSharing": "number",
    "bookingType": "string",
    "scheduledDates": [
      {
        "id": "number",
        "date": "yyyy-MM-dd",
        "status": "string"
      }
    ]
  }
}
```

**Error Responses**
- User ID is required: 400 Bad Request
- User not found: 404 Not Found

#### Get Booking by User ID
```http
GET /schedule/byUserId/{id}
```
Retrieves all bookings for a specific user.

**Path Parameters**
| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| id | Integer | Yes | User ID |

**Response**
```json
[
  {
    "id": "number",
    "bookId": "string",
    "pickUpLocation": "string",
    "dropLocation": "string",
    "time": "string",
    "returnTime": "string",
    "cabType": "string",
    "status": "string",
    "carRentalUserId": "number",
    "finalAmount": "string",
    "baseAmount": "string",
    "serviceCharge": "string",
    "gst": "string",
    "distance": "string",
    "scheduledDates": [
      {
        "id": "number",
        "date": "yyyy-MM-dd",
        "status": "string"
      }
    ]
  }
]
```

#### Get Booking by ID
```http
GET /schedule/{id}
```
Retrieves a specific booking by its ID.

**Path Parameters**
| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| id | Integer | Yes | Booking ID |

**Response**
A SchedulingBooking object.

#### Update Booking Status
```http
PUT /schedule/update-status/{userId}
```
Updates the status of a scheduled date for a user.

**Path Parameters**
| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| userId | Integer | Yes | User ID |

**Request Parameters**
| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| date | String | Yes | Date in yyyy-MM-dd format |

**Response**
A ScheduledDate object or an error message.

#### Assign Vendor to Booking
```http
PUT /schedule/{bookingId}/assign-vendor/{vendorId}
```
Assigns a vendor to a booking.

**Path Parameters**
| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| bookingId | Integer | Yes | Booking ID |
| vendorId | Long | Yes | Vendor ID |

**Response**
An updated SchedulingBooking object.

#### Assign Driver to Booking
```http
PUT /schedule/{bookingId}/assignDriver/{vendorDriverId}
```
Assigns a driver to a booking.

**Path Parameters**
| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| bookingId | Integer | Yes | Booking ID |
| vendorDriverId | Integer | Yes | Vendor Driver ID |

**Response**
An updated SchedulingBooking object.

### Driver Management

#### Add Driver
```http
POST /driver/add
```
Adds a new driver with documents.

**Request Parameters (Multipart Form Data)**
| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| driverImg | File | No | Driver's photo |
| licenseFrontImg | File | No | Front side of driver's license |
| licenseBackImg | File | No | Back side of driver's license |
| idProofFrontImg | File | No | Front side of ID proof |
| idProofBackImg | File | No | Back side of ID proof |
| pccFormImg | File | No | Police Character Certificate form |
| driverName | String | Yes | Driver's name |
| mobileNumber | Long | Yes | Driver's mobile number |
| dob | String | Yes | Driver's date of birth |
| licenseIdNum | String | Yes | Driver's license number |
| licenseExpiryDate | Long | Yes | License expiry date |
| idProofType | String | Yes | Type of ID proof |

**Response**
```json
{
  "id": "number",
  "driverName": "string",
  "mobileNumber": "number",
  "driverImgUrl": "string",
  "dob": "string",
  "licenseIdNum": "string",
  "licenseExpiryDate": "number",
  "licenseFrontUrl": "string",
  "licenseBackUrl": "string",
  "idProofType": "string",
  "idProofFrontUrl": "string",
  "idProofBackUrl": "string",
  "pccFormUrl": "string",
  "status": "string"
}
```

#### Get All Drivers
```http
GET /driver/allDriver
```
Retrieves a list of all drivers.

**Response**
```json
[
  {
    "id": "number",
    "driverName": "string",
    "mobileNumber": "number",
    "driverImgUrl": "string",
    "dob": "string",
    "licenseIdNum": "string",
    "licenseExpiryDate": "number",
    "licenseFrontUrl": "string",
    "licenseBackUrl": "string",
    "idProofType": "string",
    "idProofFrontUrl": "string",
    "idProofBackUrl": "string",
    "pccFormUrl": "string",
    "status": "string"
  }
]
```

#### Get Driver by ID
```http
GET /driver/driver/{id}
```
Retrieves a specific driver by ID.

**Path Parameters**
| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| id | Integer | Yes | Driver ID |

**Response**
A Driver object.

### Vehicle Management

#### Add Vehicle
```http
POST /vehicles/add
```
Adds a new vehicle with documents.

**Request Parameters (Multipart Form Data)**
| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| vehicleNo | String | Yes | Vehicle registration number |
| vehicleCategory | String | Yes | Category of vehicle |
| brand | String | Yes | Vehicle brand |
| modelType | String | Yes | Model type |
| fuelType | String | Yes | Type of fuel |
| vehicleOwnership | String | Yes | Vehicle ownership details |
| registrationDate | String | Yes | Registration date (yyyy-MM-dd) |
| insuranceValidUpto | String | Yes | Insurance validity date (yyyy-MM-dd) |
| insuranceImageCopy | File | No | Copy of insurance document |
| registrationCertificateFront | File | No | Front of registration certificate |
| registrationCertificateBack | File | No | Back of registration certificate |
| carNumberPhoto | File | No | Photo of car number plate |

**Response**
```json
{
  "id": "number",
  "vehicleNo": "string",
  "vehicleCategory": "string",
  "brand": "string",
  "modelType": "string",
  "fuelType": "string",
  "vehicleOwnership": "string",
  "registrationDate": "yyyy-MM-dd",
  "insuranceValidUpto": "yyyy-MM-dd",
  "insuranceImageUrl": "string",
  "registrationCertificateFrontUrl": "string",
  "registrationCertificateBackUrl": "string",
  "carNumberPhotoUrl": "string"
}
```

#### Get All Vehicles
```http
GET /vehicles
```
Retrieves a list of all vehicles.

**Response**
```json
[
  {
    "id": "number",
    "vehicleNo": "string",
    "vehicleCategory": "string",
    "brand": "string",
    "modelType": "string",
    "fuelType": "string",
    "vehicleOwnership": "string",
    "registrationDate": "yyyy-MM-dd",
    "insuranceValidUpto": "yyyy-MM-dd",
    "insuranceImageUrl": "string",
    "registrationCertificateFrontUrl": "string",
    "registrationCertificateBackUrl": "string",
    "carNumberPhotoUrl": "string"
  }
]
```

#### Get Vehicle by ID
```http
GET /vehicles/{id}
```
Retrieves a specific vehicle by ID.

**Path Parameters**
| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| id | Long | Yes | Vehicle ID |

**Response**
A Vehicle object or 404 Not Found if the vehicle doesn't exist.

#### Update Vehicle
```http
PUT /vehicles/{id}
```
Updates an existing vehicle.

**Path Parameters**
| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| id | Long | Yes | Vehicle ID |

**Request Parameters (Multipart Form Data)**
| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| vehicleNo | String | Yes | Vehicle registration number |
| vehicleCategory | String | Yes | Category of vehicle |
| brand | String | Yes | Vehicle brand |
| modelType | String | Yes | Model type |
| fuelType | String | Yes | Type of fuel |
| vehicleOwnership | String | Yes | Vehicle ownership details |
| registrationDate | String | Yes | Registration date (yyyy-MM-dd) |
| insuranceValidUpto | String | Yes | Insurance validity date (yyyy-MM-dd) |
| insuranceImageCopy | File | No | Copy of insurance document |
| registrationCertificateFront | File | No | Front of registration certificate |
| registrationCertificateBack | File | No | Back of registration certificate |
| carNumberPhoto | File | No | Photo of car number plate |

**Response**
An updated Vehicle object or 404 Not Found if the vehicle doesn't exist.

#### Delete Vehicle
```http
DELETE /vehicles/{id}
```
Deletes a vehicle.

**Path Parameters**
| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| id | Long | Yes | Vehicle ID |

**Response**
204 No Content on successful deletion.

## Configuration

The application uses the following configuration settings:

- **Application Name**: ETS
- **Database**: MySQL 8
- **Port**: 8081
- **Connection URL**: jdbc:mysql://localhost:3306/ets_application
- **File Upload**: Enabled with max file size of 100MB
- **Cloudinary Integration**: Enabled for image storage

## Error Handling

The API returns appropriate HTTP status codes:
- 200: Success
- 201: Created
- 400: Bad Request
- 404: Not Found
- 500: Internal Server Error

## WebSocket Implementation

### Overview
The application includes real-time location tracking between users and drivers using WebSocket technology. The implementation allows users to see their driver's location in real-time and allows drivers to see all users assigned to them in a particular slot.

### WebSocket Endpoint
```
ws://localhost:8081/ws-tracking
```

### Connection Parameters
Both users and drivers connect to the WebSocket with the following parameters:

#### For Users:
| Parameter | Description |
| --- | --- |
| userId | User's unique identifier |
| slotId | Common identifier for matching users and drivers |
| pickupLocation | User's pickup location |
| dropLocation | User's drop location |

#### For Drivers:
| Parameter | Description |
| --- | --- |
| driverId | Driver's unique identifier |
| slotId | Common identifier for matching users and drivers |

### Message Topics

#### For Users
- Connect: `/app/location.connect`
- Send Location: `/app/location.sendLocation`
- Subscribe to driver location: `/topic/slots/{slotId}/driver`
- Subscribe to driver status: `/topic/slots/{slotId}/driver/status`

#### For Drivers
- Connect: `/app/location.connect`
- Send Location: `/app/location.sendLocation`
- Subscribe to user locations: `/topic/driver/{driverId}/slots/{slotId}`
- Subscribe to new user notifications: `/topic/driver/slots/{slotId}/users/new`

### Message Format
```json
{
  "userId": "string",
  "driverId": "string",
  "slotId": "string",
  "latitude": "number",
  "longitude": "number",
  "pickupLocation": "string",
  "dropLocation": "string",
  "messageType": "string"
}
```

### Web Interfaces
The application provides two HTML interfaces for testing and using the WebSocket functionality:

1. `/user.html` - For users to connect and view their driver's location
2. `/driver.html` - For drivers to connect and view all users in their slot

## Dependencies

- Spring Boot
- Spring Data JPA
- MySQL Connector
- Cloudinary for image storage
- Spring WebSocket for real-time communication
- SockJS and STOMP for WebSocket client
- Leaflet.js for interactive maps
- Google Maps API for location autocomplete
"# ETS_Backend" 
