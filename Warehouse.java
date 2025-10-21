// Warehouse.java
public class Warehouse {
    private PainelDeDesenho painel;
    private ObjetoGrafico warehouseObj;
    private int cordX;
    private int cordY;

    public Warehouse(PainelDeDesenho panel, int x, int y) {
        this.painel = panel;
        this.cordX = x;
        this.cordY = y;

        this.warehouseObj = new ObjetoGrafico(
            "/GameAsset/warehouse.png", // 1. Caminho da Imagem
            cordX,                         // 2. X
            cordY,                         // 3. Y
            150,                           // 4. Largura
            150                            // 5. Altura
        );
    }
    
    public ObjetoGrafico getObjetoGrafico() {
        return this.warehouseObj;
    }
}