package com.privatecloset.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "firstName") val firstName: String? = null,
    @Json(name = "lastName") val lastName: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "role") val role: String? = "STUDENT",
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "profileImageUrl") val profileImageUrl: String? = null,
    @Json(name = "createdAt") val createdAt: String? = null,
) {
    val fullName: String
        get() {
            val combined = listOfNotNull(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
            return combined.ifBlank { username ?: email?.substringBefore("@") ?: "Student" }
        }
}

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "firstName") val firstName: String,
    @Json(name = "lastName") val lastName: String,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "username") val username: String? = null,
    @Json(name = "phoneNumber") val phoneNumber: String? = null,
)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    @Json(name = "firstName") val firstName: String? = null,
    @Json(name = "lastName") val lastName: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "profileImageUrl") val profileImageUrl: String? = null,
)

@JsonClass(generateAdapter = true)
data class AuthResponseDto(
    @Json(name = "success") val success: Boolean? = true,
    @Json(name = "message") val message: String? = null,
    @Json(name = "token") val token: String? = null,
    @Json(name = "user") val user: UserDto? = null,
)

@JsonClass(generateAdapter = true)
data class UserResponseDto(
    @Json(name = "success") val success: Boolean? = true,
    @Json(name = "user") val user: UserDto? = null,
)
