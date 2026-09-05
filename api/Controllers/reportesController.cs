using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MiApiSistema.Data;

namespace MiApiSistema.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class ReportesController : ControllerBase
    {
        private readonly AppDbContext _context;

        public ReportesController(AppDbContext context)
        {
            _context = context;
        }

        [HttpGet("resumen-mensual")]
        public async Task<IActionResult> GetResumen()
        {
            var fechaActual = DateTime.Now;
            var movimientosMes = await _context.Movimientos
                .Where(m => m.Fecha.Month == fechaActual.Month && m.Fecha.Year == fechaActual.Year)
                .ToListAsync();

            var totalIngresos = movimientosMes.Where(m => m.EsIngreso).Sum(m => m.Monto);
            var totalGastos = movimientosMes.Where(m => !m.EsIngreso).Sum(m => m.Monto);
            var saldoActual = totalIngresos - totalGastos;

            // También calculamos cuánto ha ahorrado
            var totalAhorrado = await _context.Ahorros.SumAsync(a => a.MontoTotalAcumulado);

            return Ok(new
            {
                Mes = fechaActual.ToString("MMMM"),
                Anio = fechaActual.Year,
                Ingresos = totalIngresos,
                Gastos = totalGastos,
                SaldoDisponible = saldoActual,
                AhorroTotalAcumulado = totalAhorrado
            });
        }

        [HttpGet("dashboard-principal")]
        public async Task<IActionResult> GetDashboard()
        {
            var hoy = DateTime.Now;

            // 1. Obtener todos los movimientos para el saldo general
            var todosLosMovimientos = await _context.Movimientos.ToListAsync();

            decimal ingresosTotales = todosLosMovimientos.Where(m => m.EsIngreso).Sum(m => m.Monto);
            decimal gastosTotales = todosLosMovimientos.Where(m => !m.EsIngreso).Sum(m => m.Monto);
            
            // Saldo que ella tiene "en el bolsillo" o cuenta corriente
            decimal saldoDisponible = ingresosTotales - gastosTotales;

            // 2. Obtener el ahorro total acumulado (lo que está en la 'alcancía')
            decimal ahorroAcumulado = await _context.Ahorros.SumAsync(a => a.MontoTotalAcumulado);

            // 3. Gastos específicos de este mes (para que ella vea cuánto va gastando en Abril)
            decimal gastosMesActual = todosLosMovimientos
                .Where(m => !m.EsIngreso && m.Fecha.Month == hoy.Month && m.Fecha.Year == hoy.Year)
                .Sum(m => m.Monto);

            return Ok(new
            {
                usuario = "Jadir", // Puedes hacerlo dinámico después
                saldoEnCaja = saldoDisponible,
                totalAhorrado = ahorroAcumulado,
                gastosDeEsteMes = gastosMesActual,
                ultimaActualizacion = DateTime.Now.ToString("dd/MM/yyyy HH:mm")
            });
        }
    }
}