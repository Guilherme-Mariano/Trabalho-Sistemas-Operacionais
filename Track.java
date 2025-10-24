// Track.java
import java.util.ArrayList;
import java.util.List;

public class Track {

    private PainelDeDesenho painel;
    private List<ObjetoGrafico> trackPieces; 

    /**
     * @param panel Referência ao painel de desenho.
     * @param startX Posição X inicial do primeiro pedaço de trilho.
     * @param y A posição Y (vertical) onde o trilho será desenhado (abaixo do trem).
     * @param pieceWidth Largura de cada imagem de pedaço de trilho.
     * @param pieceHeight Altura de cada imagem de pedaço de trilho.
     * @param numberOfPieces Quantos pedaços de trilho criar lado a lado.
     * @param imagePath 
     */
    public Track(PainelDeDesenho panel, int startX, int y, int pieceWidth, int pieceHeight, int numberOfPieces, String imagePath) {
        this.painel = panel;
        this.trackPieces = new ArrayList<>();

        for (int i = 0; i < numberOfPieces; i++) {
            int currentX = startX + (i * pieceWidth); 
            
            // Cria o ObjetoGrafico para  o pedaço
            ObjetoGrafico piece = new ObjetoGrafico(
                imagePath,
                currentX,
                y,
                pieceWidth,
                pieceHeight
            );
            
            // Adiciona à lista
            this.trackPieces.add(piece);
        }
    }

    /**
     * Retorna a lista de todos os ObjetoGrafico que compõem o trilho
     * @return A lista de ObjetoGrafico dos pedaços de trilho.
     */
    public List<ObjetoGrafico> getTrackPieces() {
        return this.trackPieces;
    }
}