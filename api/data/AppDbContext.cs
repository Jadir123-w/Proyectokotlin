using Microsoft.EntityFrameworkCore;
using MiApiSistema.Models; // Asegúrate de que coincida con tu namespace

namespace MiApiSistema.Data
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }

        public DbSet<Movimiento> Movimientos { get; set; }
        public DbSet<Categoria> Categorias { get; set; }
        public DbSet<Presupuesto> Presupuestos { get; set; } 
        public DbSet<Ahorro> Ahorros { get; set; }
    }
}