using System.Collections.Generic;
using MiApiSistema.Models;

namespace MiApiSistema.Models
{
    public class Categoria
    {
        public int Id { get; set; }
        public string Nombre { get; set; } = string.Empty;

        [System.Text.Json.Serialization.JsonIgnore]
        public List<Movimiento> Movimientos { get; set; } = new();
    }
}