using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MiApiSistema.Data;
using MiApiSistema.Models;

namespace MiApiSistema.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class CategoriaController : ControllerBase
    {
        private readonly AppDbContext _context;

        public CategoriaController(AppDbContext context)
        {
            _context = context;
        }

        // GET: api/Categoria
        [HttpGet]
        public async Task<ActionResult<IEnumerable<Categoria>>> GetCategorias()
        {
            // Retornamos la lista simple para que ella elija en la app
            return await _context.Categorias.ToListAsync();
        }

        // POST: api/Categoria
        [HttpPost]
        public async Task<ActionResult<Categoria>> PostCategoria(Categoria categoria)
        {
            _context.Categorias.Add(categoria);
            await _context.SaveChangesAsync();
            
            return CreatedAtAction(nameof(GetCategorias), new { id = categoria.Id }, categoria);
        }

        // DELETE: api/Categoria/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteCategoria(int id)
        {
            var categoria = await _context.Categorias.FindAsync(id);
            if (categoria == null) return NotFound();

            // Ojo: Si borras una categoría, podrías tener problemas con los movimientos asociados.
            // Una opción es verificar primero si tiene movimientos.
            _context.Categorias.Remove(categoria);
            await _context.SaveChangesAsync();

            return NoContent();
        }
    }
}