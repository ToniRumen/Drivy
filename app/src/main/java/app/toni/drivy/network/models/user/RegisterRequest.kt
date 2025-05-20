package app.toni.drivy.network.models.user

data class RegisterRequest(
    val nombre: String,
    val email: String,
    val password: String
)
