package com.example.app_projecto.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Endpoints usados por MainPage.xaml.cs (OnRefreshClicked / OnAppearing / OnGuardarClicked)
// Base: http://localhost:5179 en MAUI -> http://10.0.2.2:5179 en emulador Android
interface ApiService {

    // ReportesController.cs:18 -> GET api/Reportes/resumen-mensual
    // Ahora devuelve la lista de los 12 meses del año actual armada desde Movimientos
    @GET("api/Reportes/resumen-mensual")
    suspend fun getResumenMensual(): Response<List<ResumenMensualDto>>

    // ReportesController.cs:44 -> GET api/Reportes/dashboard-principal
    // Este da saldoEnCaja lifetime (el "Dinero Actual" real), resumen-mensual solo da el mes
    @GET("api/Reportes/dashboard-principal")
    suspend fun getDashboard(): Response<DashboardPrincipalDto>

    // MovimientoControllers.cs:20 -> GET api/Movimientos
    @GET("api/Movimientos")
    suspend fun getMovimientos(): Response<List<MovimientoDto>>

    // CategoriaController.cs:20 -> GET api/Categoria (OJO: singular, en MAUI había bug con /api/Categorias plural)
    @GET("api/Categoria")
    suspend fun getCategorias(): Response<List<CategoriaDto>>

    // MovimientoControllers.cs:44 -> POST api/Movimientos
    @POST("api/Movimientos")
    suspend fun postMovimiento(@Body body: PostMovimientoRequest): Response<PostMovimientoResponse>

    // MovimientoControllers.cs:74 -> DELETE api/Movimientos/{id} (HistorialPage.xaml.cs:122 OnBorrarClicked)
    @DELETE("api/Movimientos/{id}")
    suspend fun deleteMovimiento(@Path("id") id: Int): Response<Unit>

    // MovimientoControllers.cs:99 -> PUT api/Movimientos/{id} (HistorialPage.xaml.cs:154 OnEditarClicked)
    // OJO: el body debe llevar Id igual al de la URL (if id != movimiento.Id -> BadRequest)
    @PUT("api/Movimientos/{id}")
    suspend fun putMovimiento(@Path("id") id: Int, @Body body: PutMovimientoRequest): Response<Unit>

    // AhorroController.cs:20 -> GET api/Ahorro (AhorrosPage.xaml.cs:19 CargarMetasAhorro)
    @GET("api/Ahorro")
    suspend fun getAhorros(): Response<List<AhorroDto>>

    // AhorroController.cs:27 -> PUT api/Ahorro/{id}/sumar/{monto} (AhorrosPage.xaml.cs:54 OnSumarMontoClicked)
    // Crea además un Movimiento con CategoriaId=11 para el Historial
    @PUT("api/Ahorro/{id}/sumar/{monto}")
    suspend fun sumarAhorro(@Path("id") id: Int, @Path("monto") monto: Double): Response<Unit>

    // AhorroController.cs: RetirarAhorro -> PUT api/Ahorro/{id}/retirar/{monto}
    // Inverso de sumar: resta al ahorro y crea Movimiento EsIngreso=true para devolver a caja
    @PUT("api/Ahorro/{id}/retirar/{monto}")
    suspend fun retirarAhorro(@Path("id") id: Int, @Path("monto") monto: Double): Response<Unit>

    // PresupuestoController.cs:20 -> GET api/Presupuesto/mes/{mes}/anio/{anio}
    @GET("api/Presupuesto/mes/{mes}/anio/{anio}")
    suspend fun getPresupuestos(
        @Path("mes") mes: Int,
        @Path("anio") anio: Int
    ): Response<List<PresupuestoDto>>

    // PresupuestoController.cs:32 -> POST api/Presupuesto
    @POST("api/Presupuesto")
    suspend fun postPresupuesto(@Body body: PostPresupuestoRequest): Response<PresupuestoDto>

    // CategoriaController.cs:29 -> POST api/Categoria
    @POST("api/Categoria")
    suspend fun postCategoria(@Body body: PostCategoriaRequest): Response<CategoriaDto>

    // AhorroController.cs:60 -> POST api/Ahorro (crea meta + movimiento inicial con CategoriaId=11)
    @POST("api/Ahorro")
    suspend fun postAhorro(@Body body: PostAhorroRequest): Response<AhorroDto>
}
