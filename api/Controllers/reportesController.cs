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
            var anioActual = DateTime.Now.Year;

            // Traemos solo los movimientos del año actual y agrupamos por mes (Fecha.Month/Year)
            var movimientosAnio = await _context.Movimientos
                .Where(m => m.Fecha.Year == anioActual)
                .ToListAsync();

            // Ahorro total es global (alcancía), se repite en cada mes como antes
            var totalAhorrado = await _context.Ahorros.SumAsync(a => a.MontoTotalAcumulado);

            // Nombres fijos en español para no depender de la cultura del servidor
            // (antes ToString("MMMM") devolvía "September" y rompía el match en la app)
            string[] meses = { "enero", "febrero", "marzo", "abril", "mayo", "junio",
                               "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre" };

            var lista = Enumerable.Range(1, 12).Select(mes =>
            {
                var delMes = movimientosAnio.Where(m => m.Fecha.Month == mes).ToList();
                var ingresos = delMes.Where(m => m.EsIngreso).Sum(m => m.Monto);
                var gastos = delMes.Where(m => !m.EsIngreso).Sum(m => m.Monto);

                return new
                {
                    Mes = meses[mes - 1],
                    Anio = anioActual,
                    Ingresos = ingresos,
                    Gastos = gastos,
                    SaldoDisponible = ingresos - gastos,
                    AhorroTotalAcumulado = totalAhorrado
                };
            }).ToList();

            return Ok(lista);
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