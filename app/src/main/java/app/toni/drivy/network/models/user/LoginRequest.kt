package app.toni.drivy.network.models.user


data class LoginRequest(
    val email: String,
    val password: String
)
