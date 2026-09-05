package com.example.app_projecto.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app_projecto.R

// Replica visual de HistorialPage.xaml
class HistorialFragment : Fragment() {
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View? =
        i.inflate(R.layout.fragment_historial, c, false)

    override fun onViewCreated(v: View, s: Bundle?) {
        val lista = v.findViewById<RecyclerView>(R.id.listaHistorial)
        lista.layoutManager = LinearLayoutManager(requireContext())
        lista.adapter = HistorialAdapter(MockData.movimientos)
        // TODO fase 2: GET/DELETE/PUT api/Movimientos + SearchView filter
    }
}

// Replica visual de AhorrosPage.xaml
class AhorrosFragment : Fragment() {
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View? =
        i.inflate(R.layout.fragment_ahorros, c, false)

    override fun onViewCreated(v: View, s: Bundle?) {
        val lista = v.findViewById<RecyclerView>(R.id.listaAhorros)
        lista.layoutManager = LinearLayoutManager(requireContext())
        lista.adapter = AhorroAdapter(MockData.ahorros)
        // TODO fase 2: GET api/Ahorro, PUT api/Ahorro/{id}/sumar/{monto}
    }
}

// Replica visual de ResumenPage.xaml
class ResumenFragment : Fragment() {
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View? =
        i.inflate(R.layout.fragment_resumen, c, false)

    override fun onViewCreated(v: View, s: Bundle?) {
        val lista = v.findViewById<RecyclerView>(R.id.listaResumen)
        lista.layoutManager = LinearLayoutManager(requireContext())
        lista.adapter = ResumenAdapter(MockData.resumen)
        val refresh = v.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.refreshResumen)
        refresh.setOnRefreshListener { refresh.isRefreshing = false }
        // TODO fase 2: GET api/Reportes/resumen-mensual
    }
}
