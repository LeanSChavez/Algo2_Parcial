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
        viajero.acumularKm(viaje.distanciaTotal(service))
    }
}

