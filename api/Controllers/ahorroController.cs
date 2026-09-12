using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MiApiSistema.Data;
using MiApiSistema.Models;

namespace MiApiSistema.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AhorroController : ControllerBase
    {
        private readonly AppDbContext _context;

        public AhorroController(AppDbContext context)
        {
            _context = context;
        }

        // GET: api/Ahorro
        [HttpGet]
        public async Task<ActionResult<IEnumerable<Ahorro>>> GetAhorros()
        {
            return await _context.Ahorros.ToListAsync();
        }

        // PUT: api/Ahorro/5/sumar/50
        [HttpPut("{id}/sumar/{monto}")]
        public async Task<IActionResult> SumarAhorro(int id, decimal monto)
        {
            var ahorro = await _context.Ahorros.FindAsync(id);
            if (ahorro == null) return NotFound();

            // 1. Actualizamos el monto en la tabla de Ahorros
            ahorro.MontoTotalAcumulado += monto; 
            ahorro.UltimaActualizacion = DateTime.Now;

            // 2. Creamos el registro de movimiento para que aparezca en el Historial
            var movimientoHistorial = new Movimiento
            {
                Descripcion = $"Aporte a: {ahorro.Descripcion}",
                Monto = monto,
                Fecha = DateTime.Now,
                EsIngreso = false, // Se marca como false porque sale de tu "efectivo" para ir al ahorro
                CategoriaId = 11,   // ID de la categoría Ahorro (Amarillo en tu App)
                AhorroId = id
            };

            _context.Movimientos.Add(movimientoHistorial);

            // 3. Guardamos ambos cambios (el ahorro y el nuevo movimiento)
            await _context.SaveChangesAsync();
            
            return Ok(new { 
                Mensaje = $"¡Felicidades! Has sumado {monto} al {ahorro.Descripcion}.", 
                SaldoTotal = ahorro.MontoTotalAcumulado 
            });
        }

        // PUT: api/Ahorro/5/retirar/50 (inverso de sumar: devuelve la plata a caja)
        [HttpPut("{id}/retirar/{monto}")]
        public async Task<IActionResult> RetirarAhorro(int id, decimal monto)
        {
            if (monto <= 0) return BadRequest("El monto debe ser mayor a cero.");

            var ahorro = await _context.Ahorros.FindAsync(id);
            if (ahorro == null) return NotFound();

            if (ahorro.MontoTotalAcumulado < monto)
                return BadRequest($"Saldo insuficiente. Disponible: {ahorro.MontoTotalAcumulado}");

            // 1. Restamos de la alcancía
            ahorro.MontoTotalAcumulado -= monto;
            ahorro.UltimaActualizacion = DateTime.Now;

            // 2. Creamos el movimiento inverso: EsIngreso=true para que SUME a caja
            // (saldoEnCaja = ingresos - gastos). Con false se perdería doble.
            var movimientoHistorial = new Movimiento
            {
                Descripcion = $"Retiro de: {ahorro.Descripcion}",
                Monto = monto,
                Fecha = DateTime.Now,
                EsIngreso = true, // Entra a "efectivo" desde el ahorro
                CategoriaId = 11, // Dorado en la app, tipo "Reserva de ahorro"
                AhorroId = id
            };

            _context.Movimientos.Add(movimientoHistorial);

            await _context.SaveChangesAsync();

            return Ok(new {
                Mensaje = $"Has retirado {monto} de {ahorro.Descripcion}.",
                SaldoTotal = ahorro.MontoTotalAcumulado
            });
        }

        // POST: api/Ahorro
        [HttpPost]
        public async Task<ActionResult<Ahorro>> PostAhorro(Ahorro ahorro)
        {
            // 1. Primero agregamos el ahorro para que la base de datos le genere un ID
            _context.Ahorros.Add(ahorro);
            await _context.SaveChangesAsync(); 
            // Después de SaveChanges, 'ahorro.Id' ya tiene el valor real (ej: 1, 2, 3...)

            // 2. Creamos el movimiento vinculado a ese ahorro específico
            var movimientoAsociado = new Movimiento
            {
                Descripcion = $"Ahorro inicial: {ahorro.Descripcion}",
                Monto = ahorro.MontoTotalAcumulado,
                Fecha = DateTime.Now,
                EsIngreso = false,
                CategoriaId = 11,   // Categoría Ahorro para el color amarillo
                AhorroId = ahorro.Id // <--- AQUÍ hacemos el puente que mostraste en tu JSON
            };

            _context.Movimientos.Add(movimientoAsociado);
            
            // 3. Guardamos el movimiento
            await _context.SaveChangesAsync();

            return Ok(ahorro);
        }
    }
}