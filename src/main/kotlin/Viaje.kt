package ar.edu.algo2

import java.time.Duration
import java.time.LocalDateTime
import javax.print.attribute.standard.Destination

val horasPico = listOf(7..10, 16..19)

data class Lugar(var nombre : String , var coordX : Float , var coordY : Float)

abstract class Viaje(){
    lateinit var sistemaDeCobros: SistemaDeCobros
    var horario: Int = 0
    lateinit var lugarInicio : Lugar
    lateinit var destino : Lugar
    var completado : Boolean = false
    var distancia: Double = 0.0

    private var observers = mutableListOf<ViajeObservers>()
    fun agregarObserver(observer : ViajeObservers) = observers.add(observer)

    private var viajeros = mutableListOf<Persona>()
    fun viajeros() = viajeros.toList()


    fun esEnHoraPico() = horasPico.any{horario in it}

    fun costoParaViajero(persona: Persona) = sistemaDeCobros.calcularCosto(this, persona)

    fun completarViaje() {
        if (!completado){
            completado = true
            observers.forEach { it.viajeCompletado(this) }
        }

    }

    fun liberarViaje() { completado = false }

    fun agregarViajero(viajero: Persona) {
        validar(viajero)
        viajeros.add(viajero)
    }

    fun distanciaTotal(service: ServiceCalculoDistancia) =
        service.calcularDistancia(lugarInicio.coordX, lugarInicio.coordY
            , destino.coordX, destino.coordY).aKilometros()

    abstract fun validar(viajero: Persona)

}

class ViajeSimple() : Viaje(){
    override fun validar(viajero: Persona) {}



}

class ViajeProgramado(var capacidadMaxima : Int , var deudaMaxima : Int ) : Viaje(){

    var horarioDeSalida : LocalDateTime = LocalDateTime.now().plusHours(10)

    fun estaLleno() = viajeros().size >= capacidadMaxima

    fun saldoCorrecto(viajero: Persona) = viajero.deuda() <= deudaMaxima

    fun tiempoValido() = Duration.between(LocalDateTime.now(), horarioDeSalida).toHours() >= 2

    override fun validar(viajero: Persona) {
        require(tiempoValido()) { "No se puede ingresar con menos de 2 horas de anticipación" }
        require(!estaLleno()) { "El viaje esta completo" }
        require(saldoCorrecto(viajero)) { "El viajero supera la deuda maxima permitida" }
    }

}



class Itinerario() : Viaje(){

    private val paradas = mutableListOf<Viaje>()
    private var longevidadMinimaTarjeta : Int = 6

    fun agregarParada(parada: Viaje) = paradas.add(parada)

    fun saldoCorrecto(viajero: Persona) = viajero.tarjeta.saldo > costoParaViajero(viajero)

    fun tarjetaValida(viajero: Persona) = viajero.tarjeta.longevidad() > longevidadMinimaTarjeta

    override fun validar(viajero: Persona) {
        require(saldoCorrecto(viajero)) {"El viajero no tiene saldo suficiente " +
                "(tiene ${viajero.tarjeta.saldo} y el viaje vale ${costoParaViajero(viajero)})"}
        require(tarjetaValida(viajero)) {"La tarjeta debe tener mas de 6 meses de antiguedad " +
                "(tiene ${viajero.tarjeta.longevidad()})"}
    }


}

