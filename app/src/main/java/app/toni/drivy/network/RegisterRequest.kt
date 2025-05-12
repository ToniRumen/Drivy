package app.toni.drivy.network

data class RegisterRequest(
    val nombre: String,
    val email: String,
    val password: String
)
