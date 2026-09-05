using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MiApiSistema.Data;
using MiApiSistema.Models;

namespace MiApiSistema.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class PresupuestoController : ControllerBase
    {
        private readonly AppDbContext _context;

        public PresupuestoController(AppDbContext context)
        {
            _context = context;
        }

        // GET: api/Presupuesto/mes/4/anio/2026
        [HttpGet("mes/{mes}/anio/{anio}")]
        public async Task<ActionResult<IEnumerable<Presupuesto>>> GetPresupuestosMes(int mes, int anio)
        {
            return await _context.Presupuestos
                .Include(p => p.Categoria)
                .Where(p => p.Mes == mes && p.Anio == anio)
                .ToListAsync();
        }

        // POST: api/Presupuesto
        [HttpPost]
        public async Task<ActionResult<Presupuesto>> PostPresupuesto(Presupuesto presupuesto)
        {
            _context.Presupuestos.Add(presupuesto);
            await _context.SaveChangesAsync();
            return Ok(presupuesto);
        }
    }
}