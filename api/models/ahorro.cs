using System.ComponentModel.DataAnnotations;

namespace MiApiSistema.Models 
{
    public class Ahorro
    {
        public int Id { get; set; }
        
        // Solo para identificar el registro (ej: "Ahorro General")
        public string Descripcion { get; set; } = "Ahorro General"; 
        
        public decimal MontoTotalAcumulado { get; set; }
        public DateTime UltimaActualizacion { get; set; }

        // Mantenemos la relación para saber qué movimientos sumaron a este ahorro
        public List<Movimiento>? Movimientos { get; set; }
    }
}