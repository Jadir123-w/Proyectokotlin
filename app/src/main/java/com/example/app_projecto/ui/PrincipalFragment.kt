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
import com.example.app_projecto.data.ColorHelper
import com.example.app_projecto.data.MovimientoDto
import com.example.app_projecto.data.PostMovimientoRequest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Lógica idéntica a MainPage.xaml.cs: OnAppearing + OnRefreshClicked + OnGuardarClicked
class PrincipalFragment : Fragment() {

    private lateinit var lblSaldo: TextView
    private lateinit var lblAhorro: TextView
    private lateinit var txtDescripcion: EditText
    private lateinit var txtMonto: EditText
    private lateinit var spinner: Spinner
    private lateinit var lista: RecyclerView
    private var categorias: List<CategoriaDto> = emptyList()
    private var tieneDatos = false

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View? =
        i.inflate(R.layout.fragment_principal, c, false)

    override fun onViewCreated(v: View, s: Bundle?) {
        lblSaldo = v.findViewById(R.id.lblSaldo)
        lblAhorro = v.findViewById(R.id.lblAhorro)
        txtDescripcion = v.findViewById(R.id.txtDescripcion)
        txtMonto = v.findViewById(R.id.txtMonto)
        spinner = v.findViewById(R.id.spinnerCategoria)
        lista = v.findViewById(R.id.listaMovimientos)
        lista.layoutManager = LinearLayoutManager(requireContext())

        v.findViewById<Button>(R.id.btnActualizar).setOnClickListener { cargarDatos() }
        v.findViewById<Button>(R.id.btnGuardar).setOnClickListener { guardarRegistro() }

        cargarDatos()
    }

    // Equivale a OnAppearing: recarga cada vez que se muestra
    override fun onResume() {
        super.onResume()
        if (!tieneDatos) cargarDatos()
    }

    private fun cargarDatos() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1. Dashboard = Dinero Actual real (lifetime). Resumen-mensual solo da el mes
                // ej: si en septiembre solo hay un gasto de 10 y ningún ingreso -> -10.00,
                // mientras dashboard da 1334 lifetime. Usamos dashboard para lblSaldo.
                val resDash = ApiClient.api.getDashboard()
                if (resDash.isSuccessful) {
                    resDash.body()?.let {
                        lblSaldo.text = "S/ %.2f".format(it.saldoEnCaja)
                        lblAhorro.text = "S/ %.2f".format(it.totalAhorrado)
                    }
                } else {
                    // Fallback al mensual si dashboard falla (comportamiento MAUI original)
                    val resResumen = ApiClient.api.getResumenMensual()
                    if (resResumen.isSuccessful) {
                        resResumen.body()?.let {
                            lblSaldo.text = "S/ %.2f".format(it.saldoDisponible)
                            lblAhorro.text = "S/ %.2f".format(it.ahorroTotalAcumulado)
                        }
                    }
                }

                // 2. Movimientos (MainPage.xaml.cs:35) - últimos 5, lo nuevo primero
                val resMov = ApiClient.api.getMovimientos()
                if (resMov.isSuccessful) {
                    val dtos = resMov.body().orEmpty().sortedByDescending { it.id }.take(5)
                    lista.adapter = MovimientoAdapter(dtos.map { it.toVisual() })
                    if (dtos.isNotEmpty()) tieneDatos = true
                }

                // 3. Categorías filtrando Id != 11 (MainPage.xaml.cs:49)
                val resCat = ApiClient.api.getCategorias()
                if (resCat.isSuccessful) {
                    categorias = resCat.body().orEmpty().filter { it.id != 11 }
                    spinner.adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        categorias
                    )
                }
            } catch (e: Exception) {
                // Igual que MAUI: solo avisa si no hay datos visibles
                if (!tieneDatos) {
                    Toast.makeText(
                        requireContext(),
                        "Aviso: problema al sincronizar. Revisa que la API esté corriendo.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun guardarRegistro() {
        // Validación idéntica a MainPage.xaml.cs:86
        val categoria = spinner.selectedItem as? CategoriaDto
        val desc = txtDescripcion.text.toString().trim()
        val montoStr = txtMonto.text.toString().trim()

        if (desc.isEmpty() || montoStr.isEmpty() || categoria == null) {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }
        val monto = montoStr.toDoubleOrNull()
        if (monto == null || monto <= 0) {
            Toast.makeText(requireContext(), "Monto inválido", Toast.LENGTH_SHORT).show()
            return
        }

        // Regla MAUI línea 95: Id == 3 es ingreso
        val esIngreso = (categoria.id == 3)
        val fechaIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = ApiClient.api.postMovimiento(
                    PostMovimientoRequest(
                        descripcion = desc,
                        monto = monto,
                        fecha = fechaIso,
                        esIngreso = esIngreso,
                        categoriaId = categoria.id,
                        ahorroId = null
                    )
                )
                if (res.isSuccessful) {
                    Toast.makeText(requireContext(), "Registro guardado correctamente", Toast.LENGTH_SHORT).show()
                    txtDescripcion.text.clear()
                    txtMonto.text.clear()
                    spinner.setSelection(0)
                    cargarDatos()
                } else {
                    Toast.makeText(requireContext(), "No se pudo guardar en el servidor", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
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
            fecha = formatearFecha(fecha), // "dd MMM" como en MainPage.xaml:78
            monto = monto,
            colorHex = color,
            categoria = nombreCat,
            tipoTexto = tipo
        )
    }

    private fun formatearFecha(iso: String): String {
        return try {
            val parseFormats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
                "yyyy-MM-dd'T'HH:mm:ss.SSS"
            )
            var date: Date? = null
            for (f in parseFormats) {
                try {
                    date = SimpleDateFormat(f, Locale.US).parse(iso.substringBefore("+").substringBefore("Z"))
                    if (date != null) break
                } catch (_: Exception) {}
            }
            if (date == null) return iso.take(10)
            SimpleDateFormat("dd MMM", Locale.forLanguageTag("es")).format(date)
        } catch (_: Exception) {
            iso.take(10)
        }
    }
}
