using System.ComponentModel.DataAnnotations;
using MiApiSistema.Models;

namespace MiApiSistema.Models
{
    public class Movimiento
    {
        public int Id { get; set; }
        public string Descripcion { get; set; } = string.Empty;
        public decimal Monto { get; set; }
        public DateTime Fecha { get; set; }
        public bool EsIngreso { get; set; }

        // Relación con Categoría (Obligatoria)
        public int CategoriaId { get; set; }
        public Categoria? Categoria { get; set; }

        // Relación con Ahorro (OPCIONAL)
        // Si el usuario llena este campo en el POST, se vincula al ahorro
        public int? AhorroId { get; set; } 
        public Ahorro? Ahorro { get; set; }
    }
}

