import ar.edu.algo2.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import java.time.LocalDateTime
import java.time.LocalDate
import io.mockk.Runs
import java.time.YearMonth


class ParcialTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerTest

    // Viajeros
    val anciano = Persona().apply{edad = 70}
    val bebe = Persona().apply{edad = 3}
    val hombre = Persona().apply{edad = 24}

    // Sistema de Cobros
    var sistema = SistemaDeCobros()


    // Viaje
    val viaje = ViajeSimple().apply { sistemaDeCobros = sistema }

    // --------------------------- PUNTO 1 ---------------------------

    describe ("Condiciones tarifarias : Descuentos por edad"){
        sistema.agregarCondicionTarifaria(DescuentoEdad())
        it ("Descuentos para ancianos es del 50%"){
            sistema.calcularCosto(viaje, anciano) shouldBe 2.5
        }
        it ("Descuentos para menores es del 30%"){
            sistema.calcularCosto(viaje, bebe) shouldBe 3.5
        }
        it ("No hay descuentos para gente intermedia"){
            sistema.calcularCosto(viaje, hombre) shouldBe 5
        }
    }

    describe ("Condiciones tarifarias : Recarga por hora pico"){
        sistema.agregarCondicionTarifaria(RecargoHoraPico())

        it ("Viaje en hora pico cobra 2 mas"){
            val viajeSimple = ViajeSimple().apply{horario = 17}
            sistema.calcularCosto(viajeSimple, anciano) shouldBe 7
        }
        it ("Viaje en horario no pico no cobra mas"){
            val viajeSimple = ViajeSimple().apply{horario = 14}
            sistema.calcularCosto(viajeSimple, anciano) shouldBe 5
        }
    }

    describe("Condiciones tarifarias : Condiciones multiples"){
        sistema.agregarCondicionTarifaria(RecargoHoraPico())
        sistema.agregarCondicionTarifaria(DescuentoEdad())

        it ("Primero se aplica el recargo y luego el descuento"){
            val viajeSimple = ViajeSimple().apply{horario = 17}
            sistema.calcularCosto(viajeSimple, anciano) shouldBe 3.5
        }
        it ("Primero se aplica el descuento, luego el recargo"){
            val viajeSimple = ViajeSimple().apply{horario = 17}
            sistema.invertirCondicionesTarifarias()
            sistema.calcularCosto(viajeSimple, anciano) shouldBe 4.5
        }
        it ("Puede no cumplirse una condicion"){
            val viajeSimple = ViajeSimple().apply{horario = 14}
            sistema.calcularCosto(viajeSimple, hombre) shouldBe 5
        }
    }

    // --------------------------- PUNTO 2 ---------------------------

    describe ("Viaje Programado"){
        var viajeProgramado = ViajeProgramado(10 , 200)
        var tarjetaViajera = TarjetaViajera().apply {saldo = 20.0}
        var viajeroGenerico = Persona().apply {tarjeta = tarjetaViajera}

        it ("No puede sumar pasajero si faltan menos de 2 horas para el viaje"){
            viajeProgramado.apply {horarioDeSalida = LocalDateTime.now().plusHours(1)}
            val exception = shouldThrow<IllegalArgumentException> {
                viajeProgramado.agregarViajero(viajeroGenerico)
            }
            exception.message shouldBe "No se puede ingresar con menos de 2 horas de anticipación"
        }
        it ("No se puede ingresar si el viaje esta lleno"){
            repeat(10){viajeProgramado.agregarViajero(viajeroGenerico)}
            viajeProgramado.viajeros().size shouldBe 10

            val exception = shouldThrow<IllegalArgumentException> {
                viajeProgramado.agregarViajero(viajeroGenerico)
            }
            exception.message shouldBe "El viaje esta completo"
            viajeProgramado.viajeros().size shouldBe 10

        }
        it ("No se puede ingresar si el viajero debe mas que la deuda maxima"){
            tarjetaViajera.apply {saldo = -350.0}
            val exception = shouldThrow<IllegalArgumentException>{
                viajeProgramado.agregarViajero(viajeroGenerico)
            }
            exception.message shouldBe "El viajero supera la deuda maxima permitida"
        }

        it ("Se puede agregar si cumple todas las condiciones"){
            // De base :
            // El Viaje es 10 horas despues
            // Esta vacio
            // La deuda es 0
            viajeProgramado.agregarViajero(viajeroGenerico)
            viajeProgramado.viajeros().size shouldBe 1
        }
    }

    describe ("Viaje con itinerario"){
        var sistemaNuevo = SistemaDeCobros()
        val viajeItinerario = Itinerario().apply {sistemaDeCobros = sistemaNuevo}
        var tarjetaViajera = TarjetaViajera().apply {saldo = 2.0 ; fechaEmision = LocalDate.now().minusMonths(3)}
        var viajeroGenerico = Persona().apply {tarjeta = tarjetaViajera ; edad = 13}

        it("No se puede agregar si no alcanza el saldo"){
            sistemaNuevo.agregarCondicionTarifaria(DescuentoEdad())
            viajeItinerario.saldoCorrecto(viajeroGenerico) shouldBe false

            val exception = shouldThrow<IllegalArgumentException>{
                viajeItinerario.agregarViajero(viajeroGenerico)
            }

            exception.message shouldBe "El viajero no tiene saldo suficiente (tiene 2.0 y el viaje vale 3.5)"
        }

        it ("No se puede agregar si la tarjeta tiene menos de 6 meses"){
            tarjetaViajera.apply {saldo = 30.0}
            viajeItinerario.tarjetaValida(viajeroGenerico) shouldBe false

            val exception = shouldThrow<IllegalArgumentException>{
                viajeItinerario.agregarViajero(viajeroGenerico)
            }

            exception.message shouldBe "La tarjeta debe tener mas de 6 meses de antiguedad (tiene 3)"

        }
    }

    describe ("Observers"){
        var sistemaCobros = SistemaDeCobros()
        var tarjetaViajeraConSaldo = TarjetaViajera().apply {saldo = 500.0 }
        var tarjetaViajeraSinSaldo = TarjetaViajera().apply {saldo = 0.0 }

        var viajeroRico = Persona().apply {tarjeta = tarjetaViajeraConSaldo}
        var viajeroPobre = Persona().apply {tarjeta = tarjetaViajeraSinSaldo}

        var mailSender = mockk<MailSender>()
        every {mailSender.sendMail(any())} just Runs

        var viajeFuturo = ViajeProgramado(30 , 50)
            .apply{sistemaDeCobros = sistemaCobros ; distancia = 500.0}
        viajeFuturo.agregarObserver(RegistrarValor())
        viajeFuturo.agregarObserver(AvisarDeuda(mailSender, 3.0))



        it ("Registrar el valor del viaje en la billetera"){
            viajeFuturo.agregarViajero(viajeroRico)
            viajeFuturo.completarViaje()

            viajeroRico.tarjeta.saldo shouldBe 495
        }
        it ("Avisar a tripulantes de deuda"){
            repeat(3){viajeFuturo.agregarViajero(viajeroRico)} // Estos se filtrarian
            repeat(3){viajeFuturo.agregarViajero(viajeroPobre)} // Tienen 0, se les cobra 5, deuda max es 3

            viajeFuturo.completarViaje()

           verify(exactly = 3) {mailSender.sendMail(any())}
        }


        // --------------------------- PUNTO 3 ---------------------------

        it ("Actualizar kilometros"){
            var lugarUno = Lugar("San Martin" , 500F, 200F)
            var lugarDos = Lugar("San Isidro", 200F, 132F)

            viajeFuturo.apply{lugarInicio = lugarUno ; destino = lugarDos}

            var serviceCalculo = mockk<ServiceCalculoDistancia>()
            every {serviceCalculo.calcularDistancia(500F, 200F, 200F, 132F)} returns Coordenadas(10 , 3)

            viajeFuturo.agregarObserver(ActualizarKilometros(serviceCalculo))

            viajeFuturo.agregarViajero(viajeroRico)
            viajeFuturo.viajeros().size shouldBe 1
            viajeFuturo.completarViaje()

            viajeroRico.kmPorMes[YearMonth.now()] shouldBe 16.14172032

            viajeFuturo.liberarViaje()

            viajeFuturo.completarViaje()

            viajeroRico.kmPorMes[YearMonth.now()] shouldBe 32.28344064
        }

    }
})