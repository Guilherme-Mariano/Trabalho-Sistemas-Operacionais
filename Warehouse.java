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
            "/GameAsset/warehouse.png", 
            cordX,                         
            cordY,                         
            150,                           
            150                            
        );
    }
    
    public ObjetoGrafico getObjetoGrafico() {
        return this.warehouseObj;
    }
}