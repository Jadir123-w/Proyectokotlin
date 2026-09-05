using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MiApiSistema.Data;
using MiApiSistema.Models;

namespace MiApiSistema.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class MovimientosController : ControllerBase
    {
        private readonly AppDbContext _context;

        public MovimientosController(AppDbContext context)
        {
            _context = context;
        }

        // GET: api/Movimientos (Obtener todos los gastos e ingresos)
        [HttpGet]
        public async Task<ActionResult<IEnumerable<Movimiento>>> GetMovimientos()
        {
            // Incluimos la categoría para que tu mamá sepa en qué gastó (ej: "Mercado")
            return await _context.Movimientos.Include(m => m.Categoria).ToListAsync();
        }

        // GET: api/Movimientos/resumen (Para ver el saldo total actual)
        [HttpGet("resumen")]
        public async Task<IActionResult> GetResumen()
        {
            var movimientos = await _context.Movimientos.ToListAsync();
            var ingresos = movimientos.Where(m => m.EsIngreso).Sum(m => m.Monto);
            var egresos = movimientos.Where(m => !m.EsIngreso).Sum(m => m.Monto);
            var saldo = ingresos - egresos;

            return Ok(new { 
                TotalIngresos = ingresos, 
                TotalEgresos = egresos, 
                SaldoActual = saldo 
            });
        }

        // POST: api/Movimientos (Registrar un nuevo gasto o ingreso)
        [HttpPost]
        public async Task<ActionResult<Movimiento>> PostMovimiento(Movimiento movimiento)
        {
            // 1. Validaciones básicas
            if (movimiento.Monto <= 0) 
                return BadRequest("El monto debe ser un número positivo mayor a cero.");

            if (movimiento.Fecha == default) 
                movimiento.Fecha = DateTime.Now;

            // 2. Lógica de Ahorro (Un solo bloque)
            if (movimiento.AhorroId.HasValue)
            {
                var ahorro = await _context.Ahorros.FindAsync(movimiento.AhorroId);
                if (ahorro == null) 
                    return BadRequest($"No existe la alcancía con ID {movimiento.AhorroId}");

                // Actualizamos el acumulado una sola vez
                ahorro.MontoTotalAcumulado += movimiento.Monto;
                ahorro.UltimaActualizacion = DateTime.Now;
            }

            // 3. Guardado único
            _context.Movimientos.Add(movimiento);
            await _context.SaveChangesAsync();

            return Ok(new { Mensaje = "Registro exitoso", Datos = movimiento });
        }

        // DELETE: api/Movimientos/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteMovimiento(int id)
        {
            var movimiento = await _context.Movimientos.FindAsync(id);
            if (movimiento == null) return NotFound();

            // 1. Si era un ahorro, restamos su valor de la alcancía correspondiente
            if (movimiento.AhorroId.HasValue)
            {
                var ahorro = await _context.Ahorros.FindAsync(movimiento.AhorroId);
                if (ahorro != null)
                {
                    ahorro.MontoTotalAcumulado -= movimiento.Monto;
                    ahorro.UltimaActualizacion = DateTime.Now;
                }
            }

            // 2. Eliminamos el registro
            _context.Movimientos.Remove(movimiento);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        // PUT: api/Movimientos/5
        [HttpPut("{id}")]
        public async Task<IActionResult> PutMovimiento(int id, Movimiento movimiento)
        {
            if (id != movimiento.Id) return BadRequest();

            // 1. Buscamos el valor real que está actualmente en la DB (antes de los cambios)
            // Usamos AsNoTracking para que no interfiera con la actualización posterior
            var movOriginal = await _context.Movimientos
                .AsNoTracking()
                .FirstOrDefaultAsync(m => m.Id == id);

            if (movOriginal == null) return NotFound();

            // 2. Si el movimiento está vinculado a un ahorro, actualizamos el saldo del ahorro
            if (movimiento.AhorroId.HasValue)
            {
                var ahorro = await _context.Ahorros.FindAsync(movimiento.AhorroId);
                if (ahorro != null)
                {
                    // Calculamos la diferencia (Nuevo - Viejo)
                    // Ejemplo: Pasaste de 100 a 50 -> 50 - 100 = -50 (Se le resta 50 al ahorro)
                    decimal diferencia = movimiento.Monto - movOriginal.Monto;
                    ahorro.MontoTotalAcumulado += diferencia;
                    ahorro.UltimaActualizacion = DateTime.Now;
                }
            }

            _context.Entry(movimiento).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!_context.Movimientos.Any(e => e.Id == id)) return NotFound();
                else throw;
            }

            return NoContent();
        }
    }
}