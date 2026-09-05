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
        val desc: TextView = v.findViewById(R.id.txtDescripcion)
        val fecha: TextView = v.findViewById(R.id.txtFecha)
        val monto: TextView = v.findViewById(R.id.txtMonto)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_movimiento, p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val m = items[pos]
        h.desc.text = m.descripcion
        h.fecha.text = m.fecha
        h.monto.text = "S/ %.2f".format(m.monto)
        try { h.monto.setTextColor(Color.parseColor(m.colorHex)) } catch (_: Exception) {}
    }

    override fun getItemCount() = items.size
}

// item_historial.xml con detalle expandible = HistorialPage.xaml
class HistorialAdapter(private val items: List<MovimientoVisual>) :
    RecyclerView.Adapter<HistorialAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val cat: TextView = v.findViewById(R.id.txtCategoria)
        val fecha: TextView = v.findViewById(R.id.txtFecha)
        val monto: TextView = v.findViewById(R.id.txtMonto)
        val flecha: TextView = v.findViewById(R.id.txtFlecha)
        val detalle = v.findViewById<View>(R.id.cardDetalle)
        val desc: TextView = v.findViewById(R.id.txtDescripcion)
        val tipo: TextView = v.findViewById(R.id.txtTipo)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_historial, p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val m = items[pos]
        h.cat.text = m.categoria
        h.fecha.text = m.fecha
        h.monto.text = "S/ %.2f".format(m.monto)
        try { h.monto.setTextColor(Color.parseColor(m.colorHex)) } catch (_: Exception) {}
        h.desc.text = m.descripcion
        h.tipo.text = m.tipoTexto
        h.detalle.visibility = if (m.expandido) View.VISIBLE else View.GONE
        h.flecha.rotation = if (m.expandido) 180f else 0f
        h.itemView.setOnClickListener {
            m.expandido = !m.expandido
            notifyItemChanged(pos)
        }
    }

    override fun getItemCount() = items.size
}

// item_ahorro.xml = CollectionView listaAhorros de AhorrosPage.xaml
class AhorroAdapter(private val items: List<AhorroVisual>) :
    RecyclerView.Adapter<AhorroAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val desc: TextView = v.findViewById(R.id.txtDescripcion)
        val fecha: TextView = v.findViewById(R.id.txtFecha)
        val monto: TextView = v.findViewById(R.id.txtMonto)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_ahorro, p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val a = items[pos]
        h.desc.text = a.descripcion
        h.fecha.text = a.ultimaActualizacion
        h.monto.text = "S/ %.2f".format(a.monto)
    }

    override fun getItemCount() = items.size
}

// item_resumen_mes.xml = CollectionView cvResumen de ResumenPage.xaml
class ResumenAdapter(private val items: List<ResumenMesVisual>) :
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
}
