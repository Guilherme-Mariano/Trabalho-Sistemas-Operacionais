// Track.java
import java.util.ArrayList;
import java.util.List;

public class Track {

    private PainelDeDesenho painel;
    // Lista para guardar todos os objetos gráficos que compõem o trilho
    private List<ObjetoGrafico> trackPieces; 

    /**
     * Construtor da classe Track.
     * @param panel Referência ao painel de desenho.
     * @param startX Posição X inicial do primeiro pedaço de trilho.
     * @param y A posição Y (vertical) onde o trilho será desenhado (abaixo do trem).
     * @param pieceWidth Largura de cada imagem de pedaço de trilho.
     * @param pieceHeight Altura de cada imagem de pedaço de trilho.
     * @param numberOfPieces Quantos pedaços de trilho criar lado a lado.
     * @param imagePath O caminho para a imagem do pedaço de trilho (ex: "/GameAsset/track_piece.png").
     */
    public Track(PainelDeDesenho panel, int startX, int y, int pieceWidth, int pieceHeight, int numberOfPieces, String imagePath) {
        this.painel = panel;
        this.trackPieces = new ArrayList<>();

        // Cria os objetos gráficos para cada pedaço do trilho
        for (int i = 0; i < numberOfPieces; i++) {
            // Calcula a posição X para este pedaço
            int currentX = startX + (i * pieceWidth); 
            
            // Cria o ObjetoGrafico para este pedaço
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
     * Retorna a lista de todos os ObjetoGrafico que compõem o trilho,
     * para que possam ser adicionados ao painel de desenho.
     * @return A lista de ObjetoGrafico dos pedaços de trilho.
     */
    public List<ObjetoGrafico> getTrackPieces() {
        return this.trackPieces;
    }
}