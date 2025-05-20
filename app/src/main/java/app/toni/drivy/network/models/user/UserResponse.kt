package app.toni.drivy.network.models.user

data class UserResponse(
    val id: Long,
    val nombre: String,
    val email: String,
    val rol: String
)
