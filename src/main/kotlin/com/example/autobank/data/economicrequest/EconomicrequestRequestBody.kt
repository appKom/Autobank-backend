package com.example.autobank.data.economicrequest

data class EconomicrequestDTO(
    val subject: String? = null,
    val purpose: String? = null,
    val date: String? = null,
    val description: String? = null,
    val amount: Double? = null,
    val paymentDescription: String? = null,
    val otherInformation: String? = null,
    val committeeId: String? = null,
    val onlinemail: String? = null,
)

data class EconomicrequestRequestBody(
    val economicrequest: EconomicrequestDTO? = null,
    val attachments: Array<String> = arrayOf(),
)
