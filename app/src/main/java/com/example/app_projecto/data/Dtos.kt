package com.example.app_projecto.data

import com.google.gson.annotations.SerializedName

// Réplica de MainPage.Categoria (MainPage.xaml.cs:134)
data class CategoriaDto(
    @SerializedName(value = "id", alternate = ["Id"])
    val id: Int = 0,
    @SerializedName(value = "nombre", alternate = ["Nombre"])
    val nombre: String = ""
) {
    // Para mostrar el Nombre en el Spinner (igual que ItemDisplayBinding="Nombre" en MAUI)
    override fun toString(): String = nombre
}

data class CategoriaMovimientoDto(
    @SerializedName(value = "id", alternate = ["Id"])
    val id: Int = 0,
    @SerializedName(value = "nombre", alternate = ["Nombre"])
    val nombre: String = ""
)

// Réplica de models/movimiento.cs + MovimientoHistorial.cs
data class MovimientoDto(
    @SerializedName(value = "id", alternate = ["Id"])
    val id: Int = 0,
    @SerializedName(value = "descripcion", alternate = ["Descripcion"])
    val descripcion: String = "",
    @SerializedName(value = "monto", alternate = ["Monto"])
    val monto: Double = 0.0,
    // La API devuelve ISO-8601; lo dejamos String y formateamos en UI
    @SerializedName(value = "fecha", alternate = ["Fecha"])
    val fecha: String = "",
    @SerializedName(value = "esIngreso", alternate = ["EsIngreso"])
    val esIngreso: Boolean = false,
    @SerializedName(value = "categoriaId", alternate = ["CategoriaId"])
    val categoriaId: Int = 0,
    @SerializedName(value = "categoria", alternate = ["Categoria"])
    val categoria: CategoriaMovimientoDto? = null,
    @SerializedName(value = "ahorroId", alternate = ["AhorroId"])
    val ahorroId: Int? = null
)

// Réplica de MainPage.ResumenResponse + reportesController resumen-mensual
data class ResumenMensualDto(
    @SerializedName(value = "mes", alternate = ["Mes"])
    val mes: String? = null,
    @SerializedName(value = "anio", alternate = ["Anio"])
    val anio: Int? = null,
    @SerializedName(value = "ingresos", alternate = ["Ingresos"])
    val ingresos: Double = 0.0,
    @SerializedName(value = "gastos", alternate = ["Gastos"])
    val gastos: Double = 0.0,
    @SerializedName(value = "saldoDisponible", alternate = ["SaldoDisponible"])
    val saldoDisponible: Double = 0.0,
    @SerializedName(value = "ahorroTotalAcumulado", alternate = ["AhorroTotalAcumulado"])
    val ahorroTotalAcumulado: Double = 0.0
)

// Body del POST api/Movimientos (MovimientoControllers.cs:44)
data class PostMovimientoRequest(
    val descripcion: String,
    val monto: Double,
    val fecha: String, // ISO-8601
    val esIngreso: Boolean,
    val categoriaId: Int,
    val ahorroId: Int? = null
)

// Body del PUT api/Movimientos/{id} (MovimientoControllers.cs:99 + HistorialPage.xaml.cs:154)
// Debe incluir Id igual al de la URL, resto de campos se reenvían igual
data class PutMovimientoRequest(
    val id: Int,
    val descripcion: String,
    val monto: Double,
    val fecha: String, // reenviar la ISO original del GET
    val esIngreso: Boolean,
    val categoriaId: Int,
    val ahorroId: Int? = null
)
// La API devuelve { Mensaje, Datos } en el POST
data class PostMovimientoResponse(
    @SerializedName(value = "mensaje", alternate = ["Mensaje"])
    val mensaje: String? = null,
    @SerializedName(value = "datos", alternate = ["Datos"])
    val datos: MovimientoDto? = null
)

// Réplica de models/ahorro.cs + AhorrosPage.Ahorro (AhorrosPage.xaml.cs:65)
data class AhorroDto(
    @SerializedName(value = "id", alternate = ["Id"])
    val id: Int = 0,
    @SerializedName(value = "descripcion", alternate = ["Descripcion"])
    val descripcion: String = "",
    @SerializedName(value = "montoTotalAcumulado", alternate = ["MontoTotalAcumulado"])
    val montoTotalAcumulado: Double = 0.0,
    @SerializedName(value = "ultimaActualizacion", alternate = ["UltimaActualizacion"])
    val ultimaActualizacion: String = ""
)

// Réplica de reportesController dashboard-principal (líneas 44-74)
// saldoEnCaja = lifetime (ingresos - gastos totales), no solo del mes
data class DashboardPrincipalDto(
    @SerializedName(value = "usuario", alternate = ["Usuario"])
    val usuario: String? = null,
    @SerializedName(value = "saldoEnCaja", alternate = ["SaldoEnCaja"])
    val saldoEnCaja: Double = 0.0,
    @SerializedName(value = "totalAhorrado", alternate = ["TotalAhorrado"])
    val totalAhorrado: Double = 0.0,
    @SerializedName(value = "gastosDeEsteMes", alternate = ["GastosDeEsteMes"])
    val gastosDeEsteMes: Double = 0.0,
    @SerializedName(value = "ultimaActualizacion", alternate = ["UltimaActualizacion"])
    val ultimaActualizacion: String? = null
)

// POST api/Categoria (CategoriaController.cs:29)
data class PostCategoriaRequest(
    val nombre: String
)

// POST api/Ahorro (AhorroController.cs:60) -> crea ahorro + movimiento inicial CategoriaId=11
data class PostAhorroRequest(
    val descripcion: String,
    val montoTotalAcumulado: Double,
    val ultimaActualizacion: String // ISO-8601
)

// Réplica de models/presupuesto.cs + PresupuestoController (mes/anio + Categoria incluida)
data class PresupuestoDto(
    @SerializedName(value = "id", alternate = ["Id"])
    val id: Int = 0,
    @SerializedName(value = "montoLimite", alternate = ["MontoLimite"])
    val montoLimite: Double = 0.0,
    @SerializedName(value = "mes", alternate = ["Mes"])
    val mes: Int = 0,
    @SerializedName(value = "anio", alternate = ["Anio"])
    val anio: Int = 0,
    @SerializedName(value = "categoriaId", alternate = ["CategoriaId"])
    val categoriaId: Int = 0,
    @SerializedName(value = "categoria", alternate = ["Categoria"])
    val categoria: CategoriaMovimientoDto? = null
)

data class PostPresupuestoRequest(
    val montoLimite: Double,
    val mes: Int,
    val anio: Int,
    val categoriaId: Int
)

// Lógica de colores idéntica a MovimientoHistorial.GetColorByCategoria (líneas 30-35)
object ColorHelper {
    const val ORO_AHORRO = "#FFD700"   // categoriaId == 11
    const val VERDE_INGRESO = "#10B981" // categoriaId == 3 o esIngreso
    const val ROJO_GASTO = "#EF4444"

    fun getColorByCategoria(categoriaId: Int, esIngreso: Boolean): String {
        if (categoriaId == 11) return ORO_AHORRO
        if (categoriaId == 3 || esIngreso) return VERDE_INGRESO
        return ROJO_GASTO
    }
}
