package ar.edu.algo2



class SistemaDeCobros{

    var costoBase: Int = 5
    private var condicionesTarifarias = mutableListOf<CondicionTarifaria>()

    fun agregarCondicionTarifaria(condicion : CondicionTarifaria) =
        condicionesTarifarias.add(condicion)

    fun eliminarCondicionTarifaria(condicion: CondicionTarifaria) =
        condicionesTarifarias.remove(condicion)

    fun invertirCondicionesTarifarias() =
        condicionesTarifarias.reverse()

    fun calcularCosto(viaje: Viaje, persona: Persona) =
        condicionesTarifarias.fold(costoBase.toDouble()) {
            acumulador, condicionTarifaria ->
            condicionTarifaria.aplicar(acumulador, viaje, persona)
        }
    }



interface CondicionTarifaria{

    fun aplicar(costoParcial : Double, viaje: Viaje, persona: Persona) : Double
}

class DescuentoEdad() : CondicionTarifaria{

    override fun aplicar(costoParcial: Double, viaje: Viaje, persona: Persona) = (costoParcial * persona.descuentoObtenido())
}

class RecargoHoraPico() : CondicionTarifaria {

    override fun aplicar(costoParcial: Double, viaje: Viaje, persona: Persona): Double =
        if (viaje.esEnHoraPico()) costoParcial + 2.0 else costoParcial
}

