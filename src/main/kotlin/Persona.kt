package ar.edu.algo2

import java.time.Duration
import java.time.LocalDate
import java.time.Period
import java.time.YearMonth

class Persona(){
    var edad: Int = 0
    lateinit var tarjeta : TarjetaViajera
    var nombre : String = ""

    val kmPorMes = mutableMapOf<YearMonth, Double>()

    fun acumularKm(km: Double) {
        val mes = YearMonth.now()
        kmPorMes[mes] = (kmPorMes[mes] ?: 0.0) + km
    }

    fun esMenorEdad() = edad < 18
    fun esJubilado() = edad > 65
    fun deuda() = tarjeta.deuda()

    fun descuentoObtenido() : Double {
        return when {
            esMenorEdad() -> 0.7
            esJubilado() -> 0.5
            else -> 1.0
        }
    }

    fun pagar(costo: Double) = tarjeta.abonar(costo)




}

class TarjetaViajera{

    var saldo: Double = 0.0
    var fechaEmision : LocalDate = LocalDate.now()

    fun deuda() = if (saldo < 0.0) -saldo else 0.0
    fun longevidad() = Period.between(fechaEmision, LocalDate.now()).months

    fun abonar(costo: Double) { saldo -= costo }

}