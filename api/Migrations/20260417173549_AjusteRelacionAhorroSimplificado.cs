using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace MiApiSistema.Migrations
{
    /// <inheritdoc />
    public partial class AjusteRelacionAhorroSimplificado : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "MontoActual",
                table: "Ahorros");

            migrationBuilder.RenameColumn(
                name: "MontoObjetivo",
                table: "Ahorros",
                newName: "MontoTotalAcumulado");

            migrationBuilder.RenameColumn(
                name: "Meta",
                table: "Ahorros",
                newName: "Descripcion");

            migrationBuilder.RenameColumn(
                name: "FechaLimite",
                table: "Ahorros",
                newName: "UltimaActualizacion");

            migrationBuilder.AddColumn<int>(
                name: "AhorroId",
                table: "Movimientos",
                type: "integer",
                nullable: true);

            migrationBuilder.CreateIndex(
                name: "IX_Movimientos_AhorroId",
                table: "Movimientos",
                column: "AhorroId");

            migrationBuilder.AddForeignKey(
                name: "FK_Movimientos_Ahorros_AhorroId",
                table: "Movimientos",
                column: "AhorroId",
                principalTable: "Ahorros",
                principalColumn: "Id");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_Movimientos_Ahorros_AhorroId",
                table: "Movimientos");

            migrationBuilder.DropIndex(
                name: "IX_Movimientos_AhorroId",
                table: "Movimientos");

            migrationBuilder.DropColumn(
                name: "AhorroId",
                table: "Movimientos");

            migrationBuilder.RenameColumn(
                name: "UltimaActualizacion",
                table: "Ahorros",
                newName: "FechaLimite");

            migrationBuilder.RenameColumn(
                name: "MontoTotalAcumulado",
                table: "Ahorros",
                newName: "MontoObjetivo");

            migrationBuilder.RenameColumn(
                name: "Descripcion",
                table: "Ahorros",
                newName: "Meta");

            migrationBuilder.AddColumn<decimal>(
                name: "MontoActual",
                table: "Ahorros",
                type: "numeric",
                nullable: false,
                defaultValue: 0m);
        }
    }
}
