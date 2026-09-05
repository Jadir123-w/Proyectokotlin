using MiApiSistema.Models;

public class Presupuesto
{
    public int Id { get; set; }
    public decimal MontoLimite { get; set; }
    public int Mes { get; set; } // Ejemplo: 4 para Abril
    public int Anio { get; set; } // Ejemplo: 2026

    // Relación: Un presupuesto pertenece a una categoría
    public int CategoriaId { get; set; }
    public Categoria? Categoria { get; set; }
}