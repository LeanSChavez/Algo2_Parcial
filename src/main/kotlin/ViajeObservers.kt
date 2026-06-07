package ar.edu.algo2

interface ViajeObservers {
    fun viajeCompletado(viaje: Viaje) {

        viaje.viajeros().forEach { aplicarA(viaje, it) } }

    fun aplicarA(viaje: Viaje, viajero: Persona)
}

class RegistrarValor() : ViajeObservers {

    override fun aplicarA(viaje: Viaje, viajero: Persona) = viajero.pagar(viaje.costoParaViajero(viajero))
}

class AvisarDeuda(var mailSender: MailSender, var limiteDeuda: Double) : ViajeObservers {

    override fun aplicarA(viaje: Viaje, viajero: Persona) {
        if(viajero.deuda() > limiteDeuda) {
            mailSender.sendMail(Mail(
                "empresaTeletransporte@mail.com",
                "${viajero.nombre}@mail.com",
                "Aviso de deuda" ,
                "WARNING, te encuentras en el limite de deuda, cargar saldo porfavor"))
        }
    }
}

class ActualizarKilometros(val service: ServiceCalculoDistancia) : ViajeObservers {

    override fun aplicarA(viaje: Viaje, viajero: Persona) {
        val distanciaEnMillas = service.calcularDistancia(viaje.lugarInicio.coordenadas ,
            viaje.destino.coordenadas)
        // La distancia devuelve una data class coordenadas con (var enteros : Int, var decimal: Int) EN MILLAS
        val distanciaEnKm = (distanciaEnMillas.enteros + distanciaEnMillas.decimal / 100.0) * 1.609344

        viajero.acumularKm(distanciaEnKm)
    }
}

