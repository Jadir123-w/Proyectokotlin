package com.example.app_projecto.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app_projecto.R
import com.example.app_projecto.data.ApiClient
import com.example.app_projecto.data.CategoriaDto
import com.example.app_projecto.data.PostCategoriaRequest
import com.example.app_projecto.data.PostPresupuestoRequest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Pestaña extra sin equivalente MAUI: usa PresupuestoController (GET mes/anio + POST)
// Compara límite vs gasto real del mes (armado desde GET api/Movimientos por CategoriaId y Fecha)
class PresupuestosFragment : Fragment() {

    private lateinit var adapter: PresupuestoAdapter
    private lateinit var spinner: Spinner
    private lateinit var txtLimite: EditText
    private lateinit var lblMes: TextView
    private var categorias: List<CategoriaDto> = emptyList()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View? =
        i.inflate(R.layout.fragment_presupuestos, c, false)

    override fun onViewCreated(v: View, s: Bundle?) {
        lblMes = v.findViewById(R.id.lblMesActual)
        spinner = v.findViewById(R.id.spinnerCategoria)
        txtLimite = v.findViewById(R.id.txtLimite)
        val lista = v.findViewById<RecyclerView>(R.id.listaPresupuestos)
        lista.layoutManager = LinearLayoutManager(requireContext())
        adapter = PresupuestoAdapter(mutableListOf())
        lista.adapter = adapter

        val cal = Calendar.getInstance()
        val mes = cal.get(Calendar.MONTH) + 1 // API usa 1-12
        val anio = cal.get(Calendar.YEAR)
        lblMes.text = "PRESUPUESTOS ${nombreMes(mes).uppercase()} $anio"

        v.findViewById<Button>(R.id.btnGuardarPresupuesto).setOnClickListener {
            guardarPresupuesto(mes, anio)
        }
        v.findViewById<Button>(R.id.btnNuevaCategoria).setOnClickListener {
            pedirNuevaCategoria(mes, anio)
        }
        v.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.refreshPresupuestos)
            .setOnRefreshListener { cargarTodo(mes, anio) }
        cargarTodo(mes, anio)
    }

    private fun pedirNuevaCategoria(mes: Int, anio: Int) {
        val input = EditText(requireContext()).apply { hint = "Nombre (ej. Mascota)" }
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Nueva categoría")
            .setView(input)
            .setPositiveButton("Crear") { _, _ ->
                val nombre = input.text.toString().trim()
                if (nombre.isEmpty()) {
                    Toast.makeText(requireContext(), "Ponle un nombre", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val res = ApiClient.api.postCategoria(PostCategoriaRequest(nombre))
                        if (res.isSuccessful) {
                            Toast.makeText(requireContext(), "Categoría creada", Toast.LENGTH_SHORT).show()
                            cargarTodo(mes, anio)
                        } else {
                            Toast.makeText(requireContext(), "No se pudo crear", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            val cal = Calendar.getInstance()
            cargarTodo(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
        }
    }

    private fun cargarTodo(mes: Int, anio: Int) {
        val refresh = view?.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.refreshPresupuestos)
        refresh?.isRefreshing = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1. Categorías (excluye Ahorro Id 11, igual que Principal)
                val resCat = ApiClient.api.getCategorias()
                if (resCat.isSuccessful) {
                    categorias = resCat.body().orEmpty().filter { it.id != 11 }
                    // Mismo fix que Principal: texto blanco cerrada, negro en desplegable
                    val spinAdapter = ArrayAdapter(
                        requireContext(),
                        R.layout.spinner_selected,
                        categorias
                    )
                    spinAdapter.setDropDownViewResource(R.layout.spinner_dropdown)
                    spinner.adapter = spinAdapter
                } else {
                    Toast.makeText(requireContext(), "Categorías: error ${resCat.code()}", Toast.LENGTH_LONG).show()
                }
                // 2. Presupuestos del mes + movimientos para el gasto real
                val resPre = ApiClient.api.getPresupuestos(mes, anio)
                val resMov = ApiClient.api.getMovimientos()
                if (resPre.isSuccessful) {
                    val presupuestos = resPre.body().orEmpty()
                    val movimientos = resMov.body().orEmpty()
                    val visuales = presupuestos.map { p ->
                        val nombre = p.categoria?.nombre
                            ?: categorias.find { it.id == p.categoriaId }?.nombre
                            ?: "Categoría ${p.categoriaId}"
                        // Gasto real: movimientos no-ingreso de esa categoría en ese mes/anio
                        val gastado = movimientos
                            .filter {
                                !it.esIngreso && it.categoriaId == p.categoriaId &&
                                    fechaEnMes(it.fecha, mes, anio)
                            }
                            .sumOf { it.monto }
                        val restante = p.montoLimite - gastado
                        val porc = if (p.montoLimite > 0)
                            ((gastado / p.montoLimite) * 100).toInt() else 0
                        PresupuestoVisual(nombre, p.montoLimite, gastado, restante, porc)
                    }
                    adapter.actualizar(visuales)
                    view?.findViewById<TextView>(R.id.txtVacioPresupuestos)?.visibility =
                        if (visuales.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    Toast.makeText(requireContext(), "Presupuestos: error ${resPre.code()}. Desliza para reintentar.", Toast.LENGTH_LONG).show()
                }
            } catch (e: java.net.UnknownHostException) {
                Toast.makeText(requireContext(), "Sin conexión a ${ApiClient.BASE_URL}", Toast.LENGTH_LONG).show()
            } catch (e: java.net.SocketTimeoutException) {
                Toast.makeText(requireContext(), "La API tardó mucho. Reintenta.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "No se pudo cargar: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                refresh?.isRefreshing = false
            }
        }
    }

    private fun guardarPresupuesto(mes: Int, anio: Int) {
        val cat = spinner.selectedItem as? CategoriaDto
        val limite = txtLimite.text.toString().toDoubleOrNull()
        if (cat == null || limite == null || limite <= 0) {
            Toast.makeText(requireContext(), "Elige categoría y límite válido", Toast.LENGTH_SHORT).show()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = ApiClient.api.postPresupuesto(
                    PostPresupuestoRequest(
                        montoLimite = limite,
                        mes = mes,
                        anio = anio,
                        categoriaId = cat.id
                    )
                )
                if (res.isSuccessful) {
                    Toast.makeText(requireContext(), "Presupuesto guardado", Toast.LENGTH_SHORT).show()
                    txtLimite.text.clear()
                    cargarTodo(mes, anio)
                } else {
                    Toast.makeText(requireContext(), "No se pudo guardar", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun nombreMes(mes1a12: Int): String {
        val nombres = arrayOf(
            "enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
        )
        return nombres.getOrElse(mes1a12 - 1) { "" }
    }

    private fun fechaEnMes(iso: String, mes: Int, anio: Int): Boolean {
        return try {
            val limpio = iso.substringBefore("+").substringBefore("Z")
            var date: java.util.Date? = null
            for (f in listOf("yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSSSSS", "yyyy-MM-dd'T'HH:mm:ss.SSS")) {
                try {
                    date = SimpleDateFormat(f, Locale.US).parse(limpio)
                    if (date != null) break
                } catch (_: Exception) {}
            }
            if (date == null) return false
            val cal = Calendar.getInstance().apply { time = date }
            (cal.get(Calendar.MONTH) + 1) == mes && cal.get(Calendar.YEAR) == anio
        } catch (_: Exception) {
            false
        }
    }
}
