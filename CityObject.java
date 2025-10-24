// CityObject.java
public class CityObject {
    private PainelDeDesenho painel;
    private ObjetoGrafico cityObj;
    private int cordX;
    private int cordY;

    public CityObject(PainelDeDesenho panel, int x, int y) {
        this.painel = panel;
        this.cordX = x;
        this.cordY = y;

        this.cityObj = new ObjetoGrafico(
            "/GameAsset/city_skyline.png", 
            cordX,                         
            cordY,                         
            150,                           
            150                           
        );
    }
    
    public ObjetoGrafico getObjetoGrafico() {
        return this.cityObj;
    }
}