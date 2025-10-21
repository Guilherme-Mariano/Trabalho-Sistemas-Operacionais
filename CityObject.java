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
            "/GameAsset/city_skyline.png", // 1. Caminho da Imagem
            cordX,                         // 2. X
            cordY,                         // 3. Y
            150,                           // 4. Largura
            150                            // 5. Altura
        );
    }
    
    public ObjetoGrafico getObjetoGrafico() {
        return this.cityObj;
    }
}