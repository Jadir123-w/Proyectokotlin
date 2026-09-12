package com.example.app_projecto.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.app_projecto.R

// item_movimiento.xml = CollectionView listaMovimientos de MainPage.xaml
class MovimientoAdapter(private val items: List<MovimientoVisual>) :
    RecyclerView.Adapter<MovimientoAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val icono: TextView = v.findViewById(R.id.txtIcono)
        val desc: TextView = v.findViewById(R.id.txtDescripcion)
        val fecha: TextView = v.findViewById(R.id.txtFecha)
        val monto: TextView = v.findViewById(R.id.txtMonto)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_movimiento, p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val m = items[pos]
        h.icono.text = iconoMovimiento(m)
        h.desc.text = m.descripcion
        h.fecha.text = m.fecha
        // Retiro de ahorro (CategoriaId=11 + EsIngreso=true) se muestra en negativo
        // para diferenciarlo del aporte sin abrir el detalle
        h.monto.text = formatearMontoAhorro(m)
        try { h.monto.setTextColor(Color.parseColor(m.colorHex)) } catch (_: Exception) {}
    }

    override fun getItemCount() = items.size
}

// item_historial.xml con detalle expandible = HistorialPage.xaml
// Réplica de HistorialPage.xaml.cs: OnExpandItemTapped + OnEditarClicked + OnBorrarClicked
class HistorialAdapter(
    private val items: MutableList<MovimientoVisual>,
    private val onEditar: (MovimientoVisual) -> Unit = {},
    private val onBorrar: (MovimientoVisual) -> Unit = {}
) :
    RecyclerView.Adapter<HistorialAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val cat: TextView = v.findViewById(R.id.txtCategoria)
        val fecha: TextView = v.findViewById(R.id.txtFecha)
        val monto: TextView = v.findViewById(R.id.txtMonto)
        val flecha: TextView = v.findViewById(R.id.txtFlecha)
        val detalle = v.findViewById<View>(R.id.cardDetalle)
        val desc: TextView = v.findViewById(R.id.txtDescripcion)
        val tipo: TextView = v.findViewById(R.id.txtTipo)
        val btnEditar: View = v.findViewById(R.id.btnEditar)
        val btnBorrar: View = v.findViewById(R.id.btnBorrar)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_historial, p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val m = items[pos]
        h.cat.text = m.categoria
        h.fecha.text = m.fecha
        // Retiro (11 + EsIngreso=true) en negativo, aporte en positivo
        h.monto.text = formatearMontoAhorro(m)
        try { h.monto.setTextColor(Color.parseColor(m.colorHex)) } catch (_: Exception) {}
        h.desc.text = m.descripcion
        h.tipo.text = m.tipoTexto
        h.detalle.visibility = if (m.expandido) View.VISIBLE else View.GONE
        h.flecha.rotation = if (m.expandido) 180f else 0f
        h.itemView.setOnClickListener {
            m.expandido = !m.expandido
            notifyItemChanged(pos)
        }
        h.btnEditar.setOnClickListener { onEditar(m) }
        h.btnBorrar.setOnClickListener { onBorrar(m) }
    }

    override fun getItemCount() = items.size

    fun actualizar(nuevaLista: List<MovimientoVisual>) {
        items.clear()
        items.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    fun quitar(item: MovimientoVisual) {
        val i = items.indexOf(item)
        if (i >= 0) {
            items.removeAt(i)
            notifyItemRemoved(i)
        }
    }
}

// item_ahorro.xml = CollectionView listaAhorros de AhorrosPage.xaml
// Réplica de AhorrosPage.xaml.cs: OnSumarMontoClicked (botón +) + RetirarAhorro (botón −)
class AhorroAdapter(
    private val items: MutableList<AhorroVisual>,
    private val onSumar: (AhorroVisual) -> Unit = {},
    private val onRetirar: (AhorroVisual) -> Unit = {}
) :
    RecyclerView.Adapter<AhorroAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val desc: TextView = v.findViewById(R.id.txtDescripcion)
        val fecha: TextView = v.findViewById(R.id.txtFecha)
        val monto: TextView = v.findViewById(R.id.txtMonto)
        val btnSumar: View = v.findViewById(R.id.btnSumar)
        val btnRetirar: View = v.findViewById(R.id.btnRetirar)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_ahorro, p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val a = items[pos]
        h.desc.text = a.descripcion
        h.fecha.text = a.ultimaActualizacion
        h.monto.text = "S/ %.2f".format(a.monto)
        h.btnSumar.setOnClickListener { onSumar(a) }
        h.btnRetirar.setOnClickListener { onRetirar(a) }
    }

    override fun getItemCount() = items.size

    fun actualizar(nuevaLista: List<AhorroVisual>) {
        items.clear()
        items.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}

// item_resumen_mes.xml = CollectionView cvResumen de ResumenPage.xaml
// Réplica de ResumenPage.xaml.cs: CargarResumenAnual (12 meses)
class ResumenAdapter(private val items: MutableList<ResumenMesVisual>) :
    RecyclerView.Adapter<ResumenAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val mes: TextView = v.findViewById(R.id.txtMes)
        val ing: TextView = v.findViewById(R.id.txtIngresos)
        val gas: TextView = v.findViewById(R.id.txtGastos)
        val disp: TextView = v.findViewById(R.id.txtDisponible)
        val ahor: TextView = v.findViewById(R.id.txtAhorroTotal)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_resumen_mes, p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val r = items[pos]
        h.mes.text = r.mesDisplay
        h.ing.text = "S/ %.2f".format(r.ingresos)
        h.gas.text = "S/ %.2f".format(r.gastos)
        h.disp.text = "S/ %.2f".format(r.disponible)
        h.ahor.text = "S/ %.2f".format(r.ahorroTotal)
    }

    override fun getItemCount() = items.size

    fun actualizar(nuevaLista: List<ResumenMesVisual>) {
        items.clear()
        items.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}

// Visual de presupuesto con comparativa contra gasto real del mes
data class PresupuestoVisual(
    val categoria: String,
    val limite: Double,
    val gastado: Double,
    val restante: Double,
    val porcentaje: Int, // 0-100 para la barra (puede pasar 100 si excede)
    var expandido: Boolean = false
)

// item_presupuesto.xml = tarjeta con % visible + detalle gráfico expandible al tocar
class PresupuestoAdapter(private val items: MutableList<PresupuestoVisual>) :
    RecyclerView.Adapter<PresupuestoAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val cat: TextView = v.findViewById(R.id.txtCategoria)
        val limite: TextView = v.findViewById(R.id.txtLimite)
        val porc: TextView = v.findViewById(R.id.txtPorcentaje)
        val flecha: TextView = v.findViewById(R.id.txtFlecha)
        val detalle: View = v.findViewById(R.id.cardDetallePresupuesto)
        val porcGrande: TextView = v.findViewById(R.id.txtPorcentajeGrande)
        val estado: TextView = v.findViewById(R.id.txtEstado)
        val gastado: TextView = v.findViewById(R.id.txtGastado)
        val restante: TextView = v.findViewById(R.id.txtRestante)
        val barra: android.widget.ProgressBar = v.findViewById(R.id.barProgreso)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_presupuesto, p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = items[pos]
        h.cat.text = item.categoria
        h.limite.text = "Límite: S/ %.2f".format(item.limite)
        h.porc.text = "${item.porcentaje}%"
        h.porcGrande.text = "${item.porcentaje}%"
        // Color del % según nivel: verde <70, dorado 70-99, rojo >=100
        val colorPct = when {
            item.porcentaje >= 100 -> "#EF4444"
            item.porcentaje >= 70 -> "#FFD700"
            else -> "#10B981"
        }
        try {
            h.porc.setTextColor(Color.parseColor(colorPct))
            h.porcGrande.setTextColor(Color.parseColor(colorPct))
        } catch (_: Exception) {}
        h.estado.text = when {
            item.porcentaje >= 100 -> "¡Excedido!"
            item.porcentaje >= 70 -> "Casi al límite"
            else -> "En control"
        }
        h.gastado.text = "Gastado: S/ %.2f de S/ %.2f".format(item.gastado, item.limite)
        h.restante.text = if (item.restante >= 0)
            "Queda: S/ %.2f".format(item.restante)
        else
            "Excedido por: S/ %.2f".format(-item.restante)
        try {
            h.restante.setTextColor(
                if (item.restante >= 0) Color.parseColor("#10B981")
                else Color.parseColor("#EF4444")
            )
        } catch (_: Exception) {}
        h.barra.progress = item.porcentaje.coerceIn(0, 100)
        h.detalle.visibility = if (item.expandido) View.VISIBLE else View.GONE
        h.flecha.rotation = if (item.expandido) 180f else 0f
        h.itemView.setOnClickListener {
            item.expandido = !item.expandido
            notifyItemChanged(pos)
        }
    }

    override fun getItemCount() = items.size

    fun actualizar(nuevaLista: List<PresupuestoVisual>) {
        items.clear()
        items.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}

// Aporte a ahorro (11 + EsIngreso=false, "Aporte a: ...") = positivo.
// Retiro de ahorro (11 + EsIngreso=true, "Retiro de: ...") = negativo con "-" al lado del monto.
private fun esRetiroAhorro(m: MovimientoVisual): Boolean =
    m.categoriaId == 11 && m.esIngreso

private fun formatearMontoAhorro(m: MovimientoVisual): String =
    if (esRetiroAhorro(m)) "- S/ %.2f".format(m.monto)
    else "S/ %.2f".format(m.monto)

// Icono por categoría para la lista de Principal (círculo de item_movimiento)
private fun iconoMovimiento(m: MovimientoVisual): String {
    if (m.categoriaId == 11) return "💰"
    if (m.categoriaId == 3 || m.esIngreso) return "💵"
    val n = (m.categoria + " " + m.descripcion).lowercase()
    return when {
        n.contains("aliment") || n.contains("comida") || n.contains("mercado") -> "🍔"
        n.contains("transport") || n.contains("pasaje") || n.contains("moto") || n.contains("carro") -> "🚌"
        n.contains("luz") || n.contains("agua") || n.contains("internet") || n.contains("servicio") -> "💡"
        n.contains("salud") || n.contains("medic") -> "❤️"
        n.contains("educ") || n.contains("senati") || n.contains("coleg") -> "🎓"
        n.contains("ropa") || n.contains("vest") -> "👕"
        n.contains("casa") || n.contains("hogar") || n.contains("alquil") -> "🏠"
        n.contains("celular") || n.contains("telefono") -> "📱"
        n.contains("gusto") || n.contains("divers") || n.contains("ocio") -> "🎉"
        n.contains("trabajo") -> "💼"
        else -> "💸"
    }
}
