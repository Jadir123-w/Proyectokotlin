package com.example.app_projecto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.app_projecto.ui.AhorrosFragment
import com.example.app_projecto.ui.HistorialFragment
import com.example.app_projecto.ui.PrincipalFragment
import com.example.app_projecto.ui.ResumenFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawer: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawer = findViewById(R.id.drawer_layout)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        toolbar.setNavigationOnClickListener { drawer.openDrawer(GravityCompat.START) }

        navView.setNavigationItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_historial -> HistorialFragment()
                R.id.nav_ahorros -> AhorrosFragment()
                R.id.nav_resumen -> ResumenFragment()
                else -> PrincipalFragment()
            }
            toolbar.title = when (item.itemId) {
                R.id.nav_historial -> "HISTORIAL"
                R.id.nav_ahorros -> "MIS AHORROS"
                R.id.nav_resumen -> "HISTÓRICO MENSUAL"
                else -> "FINANZAS"
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        // Pantalla inicial = Principal (igual que MAUI AppShell Route=MainPage)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PrincipalFragment())
                .commit()
            navView.setCheckedItem(R.id.nav_principal)
        }
    }
}
