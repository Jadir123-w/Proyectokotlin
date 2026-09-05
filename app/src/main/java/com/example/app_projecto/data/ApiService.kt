package com.example.app_projecto.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Endpoints usados por MainPage.xaml.cs (OnRefreshClicked / OnAppearing / OnGuardarClicked)
// Base: http://localhost:5179 en MAUI -> http://10.0.2.2:5179 en emulador Android
interface ApiService {

    // ReportesController.cs:18 -> GET api/Reportes/resumen-mensual
    @GET("api/Reportes/resumen-mensual")
    suspend fun getResumenMensual(): Response<ResumenMensualDto>

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
}
