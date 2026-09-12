package com.example.app_projecto.ui

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app_projecto.R
import com.example.app_projecto.data.ApiClient
import com.example.app_projecto.data.ColorHelper
import com.example.app_projecto.data.MovimientoDto
import com.example.app_projecto.data.PutMovimientoRequest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Lógica idéntica a HistorialPage.xaml.cs: CargarHistorial + OnSearchTextChanged + OnBorrarClicked + OnEditarClicked
class HistorialFragment : Fragment() {
    private lateinit var adapter: HistorialAdapter
    private var listaCompleta: List<MovimientoVisual> = emptyList()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View? =
        i.inflate(R.layout.fragment_historial, c, false)

    override fun onViewCreated(v: View, s: Bundle?) {
        val lista = v.findViewById<RecyclerView>(R.id.listaHistorial)
        lista.layoutManager = LinearLayoutManager(requireContext())
        adapter = HistorialAdapter(
            mutableListOf(),
            onEditar = { editarMonto(it) },
            onBorrar = { borrarMovimiento(it) }
        )
        lista.adapter = adapter

        val search = v.findViewById<SearchView>(R.id.searchHistorial)
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?): Boolean = false
            override fun onQueryTextChange(nuevo: String?): Boolean {
                filtrar(nuevo.orEmpty())
                return true
            }
        })
        v.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.refreshHistorial)
            .setOnRefreshListener { cargarHistorial() }

        cargarHistorial()
    }

    // Equivale a OnAppearing: recarga cada vez que se muestra
    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) cargarHistorial()
    }

    private fun mostrarVacio(mensaje: String) {
        view?.findViewById<android.widget.TextView>(R.id.txtVacioHistorial)?.let {
            it.text = mensaje
            it.visibility = View.VISIBLE
        }
        view?.findViewById<RecyclerView>(R.id.listaHistorial)?.visibility = View.GONE
    }

    private fun ocultarVacio() {
        view?.findViewById<android.widget.TextView>(R.id.txtVacioHistorial)?.visibility = View.GONE
        view?.findViewById<RecyclerView>(R.id.listaHistorial)?.visibility = View.VISIBLE
    }

    private fun cargarHistorial() {
        val refresh = view?.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.refreshHistorial)
        refresh?.isRefreshing = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // HistorialPage.xaml.cs:27 -> GET api/Movimientos
                val res = ApiClient.api.getMovimientos()
                if (res.isSuccessful) {
                    val dtos = res.body().orEmpty()
                        // Más actuales arriba (id mayor = más reciente), viejos abajo
                        .sortedByDescending { it.id }
                    listaCompleta = dtos.map { it.toVisual() }
                    adapter.actualizar(listaCompleta)
                    if (listaCompleta.isEmpty()) mostrarVacio("Sin movimientos todavía.\nRegistra el primero en Principal 💸")
                    else ocultarVacio()
                } else {
                    mostrarVacio("No se pudo cargar (error ${res.code()}).\nDesliza para reintentar 📡")
                    Toast.makeText(requireContext(), "No se pudo cargar el historial (error ${res.code()})", Toast.LENGTH_LONG).show()
                }
            } catch (e: java.net.UnknownHostException) {
                mostrarVacio("Sin conexión a ${ApiClient.BASE_URL}\nDesliza para reintentar 📡")
                Toast.makeText(requireContext(), "Sin conexión. Verifica IP y puerto.", Toast.LENGTH_LONG).show()
            } catch (e: java.net.SocketTimeoutException) {
                Toast.makeText(requireContext(), "La API tardó mucho. Reintenta.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                mostrarVacio("Error: ${e.message}\nDesliza para reintentar 📡")
                Toast.makeText(requireContext(), "No se pudo cargar el historial", Toast.LENGTH_SHORT).show()
            } finally {
                refresh?.isRefreshing = false
            }
        }
    }

    // Réplica de OnSearchTextChanged (HistorialPage.xaml.cs:48)
    // Filtra por descripcion, NombreCategoria o CategoriaId exacto, insensible a mayúsculas
    private fun filtrar(texto: String) {
        val filtro = texto.lowercase().trim()
        if (filtro.isEmpty()) {
            adapter.actualizar(listaCompleta)
        } else {
            adapter.actualizar(listaCompleta.filter {
                it.descripcion.lowercase().contains(filtro) ||
                    it.categoria.lowercase().contains(filtro) ||
                    it.categoriaId.toString() == filtro
            })
        }
    }

    // Réplica de OnBorrarClicked (HistorialPage.xaml.cs:114)
    private fun borrarMovimiento(item: MovimientoVisual) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar")
            .setMessage("¿Deseas borrar este movimiento?")
            .setPositiveButton("Sí") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        // DELETE api/Movimientos/{id} -> 204 NoContent
                        val res = ApiClient.api.deleteMovimiento(item.id)
                        if (res.isSuccessful) {
                            listaCompleta = listaCompleta.filter { it.id != item.id }
                            adapter.quitar(item)
                        } else {
                            Toast.makeText(requireContext(), "No se pudo borrar en el servidor", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    // Réplica de OnEditarClicked (HistorialPage.xaml.cs:135)
    // Solo edita el monto, igual que MAUI con DisplayPromptAsync
    private fun editarMonto(item: MovimientoVisual) {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(item.monto.toString())
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Editar Monto")
            .setMessage("Categoría: ${item.categoria}")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val nuevo = input.text.toString().toDoubleOrNull()
                if (nuevo == null || nuevo <= 0) {
                    Toast.makeText(requireContext(), "Monto inválido", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        // PUT api/Movimientos/{id} con objeto completo y monto actualizado
                        val body = PutMovimientoRequest(
                            id = item.id,
                            descripcion = item.descripcion,
                            monto = nuevo,
                            fecha = item.fechaIso.ifEmpty { item.fecha },
                            esIngreso = item.esIngreso,
                            categoriaId = item.categoriaId,
                            ahorroId = item.ahorroId
                        )
                        val res = ApiClient.api.putMovimiento(item.id, body)
                        if (res.isSuccessful) {
                            Toast.makeText(requireContext(), "Movimiento actualizado en la base de datos", Toast.LENGTH_SHORT).show()
                            cargarHistorial() // refresca igual que MAUI línea 159
                        } else {
                            val err = res.errorBody()?.string().orEmpty()
                            Toast.makeText(requireContext(), "Error: ${res.code()} - $err", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Error de Conexión: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun MovimientoDto.toVisual(): MovimientoVisual {
        val nombreCat = categoria?.nombre ?: "General"
        val color = ColorHelper.getColorByCategoria(categoriaId, esIngreso)
        val tipo = when {
            categoriaId == 11 -> "Reserva de ahorro"
            esIngreso -> "Ingreso a cuenta"
            else -> "Gasto realizado"
        }
        return MovimientoVisual(
            descripcion = descripcion,
            fecha = formatearFecha(fecha), // HistorialPage.xaml:31 -> dd/MM/yyyy
            monto = monto,
            colorHex = color,
            categoria = nombreCat,
            tipoTexto = tipo,
            id = id,
            categoriaId = categoriaId,
            esIngreso = esIngreso,
            ahorroId = ahorroId,
            fechaIso = fecha
        )
    }

    private fun formatearFecha(iso: String): String {
        return try {
            var date: Date? = null
            for (f in listOf("yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSSSSS", "yyyy-MM-dd'T'HH:mm:ss.SSS")) {
                try {
                    date = SimpleDateFormat(f, Locale.US)
                        .parse(iso.substringBefore("+").substringBefore("Z"))
                    if (date != null) break
                } catch (_: Exception) {}
            }
            if (date == null) return iso.take(10)
            SimpleDateFormat("dd/MM/yyyy", Locale.US).format(date)
        } catch (_: Exception) {
            iso.take(10)
        }
    }
}

// Lógica idéntica a AhorrosPage.xaml.cs: CargarMetasAhorro + OnSumarMontoClicked + OnAppearing
class AhorrosFragment : Fragment() {
    private lateinit var adapter: AhorroAdapter
    private lateinit var lblTotal: android.widget.TextView

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View? =
        i.inflate(R.layout.fragment_ahorros, c, false)

    override fun onViewCreated(v: View, s: Bundle?) {
        lblTotal = v.findViewById(R.id.lblTotalGlobal)
        val lista = v.findViewById<RecyclerView>(R.id.listaAhorros)
        lista.layoutManager = LinearLayoutManager(requireContext())
        adapter = AhorroAdapter(
            mutableListOf(),
            onSumar = { preguntarMonto(it) },
            onRetirar = { pedirRetiro(it) }
        )
        lista.adapter = adapter
        v.findViewById<View>(R.id.btnNuevaMeta).setOnClickListener { pedirNuevaMeta() }
        v.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.refreshAhorros)
            .setOnRefreshListener { cargarMetas() }
        cargarMetas()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) cargarMetas()
    }

    private fun cargarMetas() {
        val refresh = view?.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.refreshAhorros)
        refresh?.isRefreshing = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // AhorrosPage.xaml.cs:19 -> GET api/Ahorro
                val res = ApiClient.api.getAhorros()
                if (res.isSuccessful) {
                    val visuales = res.body().orEmpty().map { it.toVisual() }
                    adapter.actualizar(visuales)
                    // AhorrosPage.xaml.cs:23 -> Suma total
                    lblTotal.text = "S/ %.2f".format(visuales.sumOf { it.monto })
                    view?.findViewById<android.widget.TextView>(R.id.txtVacioAhorros)?.visibility =
                        if (visuales.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    Toast.makeText(requireContext(), "Ahorros: error ${res.code()}. Desliza para reintentar.", Toast.LENGTH_LONG).show()
                }
            } catch (e: java.net.UnknownHostException) {
                Toast.makeText(requireContext(), "Sin conexión a ${ApiClient.BASE_URL}", Toast.LENGTH_LONG).show()
            } catch (e: java.net.SocketTimeoutException) {
                Toast.makeText(requireContext(), "La API tardó mucho. Reintenta.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "No se pudo conectar: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                refresh?.isRefreshing = false
            }
        }
    }

    // Réplica de OnSumarMontoClicked (AhorrosPage.xaml.cs:32)
    // DisplayActionSheet con S/5, S/10, S/20, Otro monto
    private fun preguntarMonto(item: AhorroVisual) {
        val opciones = arrayOf("S/ 5", "S/ 10", "S/ 20", "Otro monto")
        AlertDialog.Builder(requireContext())
            .setTitle("¿Cuánto quieres sumar a ${item.descripcion}?")
            .setItems(opciones) { _, cual ->
                when (cual) {
                    0 -> sumar(item, 5.0)
                    1 -> sumar(item, 10.0)
                    2 -> sumar(item, 20.0)
                    else -> pedirOtroMonto(item)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Réplica de DisplayPromptAsync "Sumar Ahorro" (AhorrosPage.xaml.cs:47)
    private fun pedirOtroMonto(item: AhorroVisual) {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Escribe el monto"
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Sumar Ahorro")
            .setMessage("Escribe el monto:")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val monto = input.text.toString().toDoubleOrNull()
                if (monto == null || monto <= 0) {
                    Toast.makeText(requireContext(), "Monto inválido", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                sumar(item, monto)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun sumar(item: AhorroVisual, monto: Double) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // PUT api/Ahorro/{id}/sumar/{monto} -> actualiza ahorro + crea movimiento CategoriaId=11
                val res = ApiClient.api.sumarAhorro(item.id, monto)
                if (res.isSuccessful) {
                    Toast.makeText(requireContext(), "¡Genial! Sumaste S/ $monto a tu ahorro.", Toast.LENGTH_SHORT).show()
                    cargarMetas() // refresca igual que MAUI línea 59
                } else {
                    Toast.makeText(requireContext(), "No se pudo sumar en el servidor", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Flujo retirar: pide monto, valida contra saldo, confirma y llama al endpoint inverso
    private fun pedirRetiro(item: AhorroVisual) {
        if (item.monto <= 0) {
            Toast.makeText(requireContext(), "Sin saldo para retirar en ${item.descripcion}", Toast.LENGTH_SHORT).show()
            return
        }
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Monto (disponible S/ %.2f)".format(item.monto)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Usar ahorro")
            .setMessage("¿Cuánto quieres retirar de ${item.descripcion}? Vuelve a caja.")
            .setView(input)
            .setPositiveButton("Retirar") { _, _ ->
                val monto = input.text.toString().toDoubleOrNull()
                if (monto == null || monto <= 0) {
                    Toast.makeText(requireContext(), "Monto inválido", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (monto > item.monto) {
                    Toast.makeText(requireContext(), "Saldo insuficiente. Disponible: S/ %.2f".format(item.monto), Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                confirmarRetiro(item, monto)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarRetiro(item: AhorroVisual, monto: Double) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar")
            .setMessage("Retirar S/ %.2f de ${item.descripcion}?".format(monto))
            .setPositiveButton("Sí") { _, _ -> retirar(item, monto) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun retirar(item: AhorroVisual, monto: Double) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // PUT api/Ahorro/{id}/retirar/{monto} -> resta al ahorro + movimiento EsIngreso=true a caja
                val res = ApiClient.api.retirarAhorro(item.id, monto)
                if (res.isSuccessful) {
                    Toast.makeText(requireContext(), "Retiraste S/ $monto a caja.", Toast.LENGTH_SHORT).show()
                    cargarMetas()
                } else {
                    val err = res.errorBody()?.string().orEmpty()
                    Toast.makeText(requireContext(), "No se pudo retirar: $err", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // POST api/Ahorro: nueva meta con monto inicial (la API crea el movimiento CategoriaId=11)
    private fun pedirNuevaMeta() {
        val layout = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
        }
        val inputNombre = EditText(requireContext()).apply { hint = "Nombre de la meta (ej. Viaje)" }
        val inputMonto = EditText(requireContext()).apply {
            hint = "Monto inicial S/ (0 si es solo la meta)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        layout.addView(inputNombre)
        layout.addView(inputMonto)
        AlertDialog.Builder(requireContext())
            .setTitle("Nueva meta de ahorro")
            .setView(layout)
            .setPositiveButton("Crear") { _, _ ->
                val nombre = inputNombre.text.toString().trim()
                val inicial = inputMonto.text.toString().toDoubleOrNull() ?: 0.0
                if (nombre.isEmpty()) {
                    Toast.makeText(requireContext(), "Ponle un nombre a la meta", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (inicial < 0) {
                    Toast.makeText(requireContext(), "Monto inválido", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                crearMeta(nombre, inicial)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun crearMeta(nombre: String, inicial: Double) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val fechaIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                    .format(Date())
                val res = ApiClient.api.postAhorro(
                    com.example.app_projecto.data.PostAhorroRequest(
                        descripcion = nombre,
                        montoTotalAcumulado = inicial,
                        ultimaActualizacion = fechaIso
                    )
                )
                if (res.isSuccessful) {
                    Toast.makeText(requireContext(), "Meta creada", Toast.LENGTH_SHORT).show()
                    cargarMetas()
                } else {
                    Toast.makeText(requireContext(), "No se pudo crear la meta", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun com.example.app_projecto.data.AhorroDto.toVisual(): AhorroVisual {
        return AhorroVisual(
            descripcion = descripcion,
            // AhorrosPage.xaml:36 -> 'Última vez: {0:dd/MM}'
            ultimaActualizacion = "Última vez: ${formatearCorto(ultimaActualizacion)}",
            monto = montoTotalAcumulado,
            id = id,
            fechaIso = ultimaActualizacion
        )
    }

    private fun formatearCorto(iso: String): String {
        return try {
            var date: Date? = null
            for (f in listOf("yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSSSSS", "yyyy-MM-dd'T'HH:mm:ss.SSS")) {
                try {
                    date = SimpleDateFormat(f, Locale.US)
                        .parse(iso.substringBefore("+").substringBefore("Z"))
                    if (date != null) break
                } catch (_: Exception) {}
            }
            if (date == null) return iso.take(5)
            SimpleDateFormat("dd/MM", Locale.US).format(date)
        } catch (_: Exception) {
            iso.take(5)
        }
    }
}

// Lógica idéntica a ResumenPage.xaml.cs: CargarResumenAnual + RefreshCommand + OnAppearing
// La API ahora devuelve los 12 meses armados desde Movimientos, ya no se rellenan ceros en cliente
class ResumenFragment : Fragment() {
    private lateinit var adapter: ResumenAdapter
    private lateinit var refresh: androidx.swiperefreshlayout.widget.SwipeRefreshLayout

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View? =
        i.inflate(R.layout.fragment_resumen, c, false)

    override fun onViewCreated(v: View, s: Bundle?) {
        val lista = v.findViewById<RecyclerView>(R.id.listaResumen)
        lista.layoutManager = LinearLayoutManager(requireContext())
        adapter = ResumenAdapter(mutableListOf())
        lista.adapter = adapter
        refresh = v.findViewById(R.id.refreshResumen)
        // RefreshView Command={RefreshCommand} de ResumenPage.xaml:8
        refresh.setOnRefreshListener { cargarResumen() }
        cargarResumen()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) cargarResumen()
    }

    private fun cargarResumen() {
        refresh.isRefreshing = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // GET api/Reportes/resumen-mensual -> lista de 12 meses armada en la API
                // desde las fechas de Movimientos (Fecha.Month/Year del año actual)
                val res = ApiClient.api.getResumenMensual()
                if (res.isSuccessful) {
                    val listaApi = res.body().orEmpty()
                    val visuales = listaApi.map {
                        ResumenMesVisual(
                            // MesDisplay igual que MAUI: "MES ANIO" en mayúsculas
                            mesDisplay = "${it.mes.orEmpty().uppercase()} ${it.anio ?: 0}",
                            ingresos = it.ingresos,
                            gastos = it.gastos,
                            disponible = it.saldoDisponible,
                            ahorroTotal = it.ahorroTotalAcumulado
                        )
                    }
                    adapter.actualizar(visuales)
                } else {
                    Toast.makeText(requireContext(), "No se pudo cargar el resumen", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "No se pudo conectar: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                refresh.isRefreshing = false
            }
        }
    }
}
