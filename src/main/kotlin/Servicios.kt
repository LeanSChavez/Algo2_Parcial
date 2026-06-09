package ar.edu.algo2

interface MailSender{
    fun sendMail(mail : Mail)
}

data class Mail(
    val from: String,
    var to: String,
    var subject: String,
    var body: String
)


interface ServiceCalculoDistancia{

    fun calcularDistancia(lugarInicio: Float, destino: Float, codigoID : Int = 18) : Coordenadas

}

data class Coordenadas(var enteros: Int, var decimal: Int) {
    fun aKilometros() = (enteros + decimal / 100.0) * 1.609344
}