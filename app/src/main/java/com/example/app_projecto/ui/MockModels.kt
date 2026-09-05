package com.example.app_projecto.ui

// Modelos visuales provisorios (idénticos a MAUI: MovimientoHistorial.cs / ResumenMensual.cs)
// En la fase 2 se reemplazarán por los DTOs reales de la API.
data class MovimientoVisual(
    val descripcion: String,
    val fecha: String,
    val monto: Double,
    val colorHex: String = "#EF4444", // ColorMonto de MAUI
    val categoria: String = "General",
    val tipoTexto: String = "Gasto realizado",
    var expandido: Boolean = false
)

data class AhorroVisual(
    val descripcion: String,
    val ultimaActualizacion: String,
    val monto: Double
)

data class ResumenMesVisual(
    val mesDisplay: String,
    val ingresos: Double,
    val gastos: Double,
    val disponible: Double,
    val ahorroTotal: Double
)

object MockData {
    val movimientos = listOf(
        MovimientoVisual("Sueldo", "05 Sep", 1500.0, "#10B981", "Ingreso", "Ingreso a cuenta"),
        MovimientoVisual("Compras mercado", "04 Sep", 120.50, "#EF4444", "Comida"),
        MovimientoVisual("Aporte ahorro", "03 Sep", 100.0, "#FFD700", "Ahorro", "Reserva de ahorro")
    )
    val ahorros = listOf(
        AhorroVisual("Ahorro General", "Última vez: 05/09", 450.0),
        AhorroVisual("Viaje", "Última vez: 01/09", 200.0)
    )
    val resumen = listOf(
        ResumenMesVisual("SEPTIEMBRE 2026", 1500.0, 620.5, 879.5, 650.0),
        ResumenMesVisual("AGOSTO 2026", 0.0, 0.0, 0.0, 0.0)
    )
    val categorias = listOf("Comida", "Transporte", "Ingreso", "Servicios")
}
