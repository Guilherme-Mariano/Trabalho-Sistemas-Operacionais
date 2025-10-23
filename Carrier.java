// Carrier.java
import javax.swing.SwingUtilities;

public class Carrier {

    private PainelDeDesenho painel;
    private ObjetoGrafico carrierObj;
    private ObjetoGrafico trainObj; // Referência ao ObjetoGrafico do trem

    // Enum para os estados visuais do vagão
    public enum State { EMPTY, FULL }

    // Caminhos para as imagens (frames)
    private static final String IMAGE_EMPTY = "/GameAsset/empty_carrier.png";
    private static final String IMAGE_FULL = "/GameAsset/carrier.png";

    /**
     * Construtor do Carrier.
     * @param panel Referência ao painel de desenho.
     * @param train Referência ao ObjetoGrafico da locomotiva.
     */
    public Carrier(PainelDeDesenho panel, ObjetoGrafico train) {
        this.painel = panel;
        this.trainObj = train;

        // Define o tamanho do vagão 
        int carrierWidth = 100;
        int carrierHeight = 70;

        // Calcula a posição inicial relativa ao trem
        // Usa a direção atual do trem para calcular a posição X inicial
        Direcao initialDirection = train.isVisible() ? Direcao.DIREITA : Direcao.ESQUERDA; // Suposição inicial
        int initialX = calculateX(train.getX(), train.getLargura(), initialDirection, carrierWidth);
        int initialY = train.getY() + (train.getAltura() / 2) - (carrierHeight / 2); // Alinha verticalmente

        // Cria o ObjetoGrafico usando o construtor de animação
        this.carrierObj = new ObjetoGrafico(
            initialX,
            initialY,
            carrierWidth,
            carrierHeight,
            IMAGE_EMPTY, // Frame 0 = Vazio
            IMAGE_FULL   // Frame 1 = Cheio
        );

        // Começa no estado VAZIO
        setState(State.EMPTY);
    }

    /** Retorna o ObjetoGrafico do vagão para ser adicionado ao painel. */
    public ObjetoGrafico getObjetoGrafico() {
        return this.carrierObj;
    }

    /** Calcula a posição X do vagão (atrás da locomotiva). */
    private int calculateX(int trainX, int trainWidth, Direcao trainDirection, int carrierWidth) {
        if (trainDirection == Direcao.DIREITA) {
            // Se o trem vai para a direita, o vagão fica à esquerda (atrás)
            return trainX - carrierWidth;
        } else { // Direcao.ESQUERDA
            // Se o trem vai para a esquerda, o vagão fica à direita (atrás)
            return trainX + trainWidth;
        }
    }

    /**
     * Atualiza a posição do vagão para seguir o trem.
     * Deve ser chamado dentro de SwingUtilities.invokeLater.
     * @param trainDirection A direção atual do trem.
     */
    public void updatePosition(Direcao trainDirection) {
        if (trainObj == null || carrierObj == null) return;

        int trainX = trainObj.getX();
        int trainY = trainObj.getY();
        int trainWidth = trainObj.getLargura();
        int trainHeight = trainObj.getAltura();
        int carrierWidth = carrierObj.getLargura();
        int carrierHeight = carrierObj.getAltura();

        int newX;
        if(trainDirection == Direcao.DIREITA)
            newX = calculateX(trainX, trainWidth, trainDirection, carrierWidth) + 15;
        else{
            newX = calculateX(trainX, trainWidth, trainDirection, carrierWidth) - 15;
        }
        // Mantém o alinhamento vertical com o trem
        int newY = trainY + (trainHeight / 2) - (carrierHeight / 2);

        carrierObj.setLocation(newX, newY);
    }

    /**
     * Define a aparência do vagão (vazio ou cheio).
     * @param state O estado desejado (EMPTY ou FULL).
     */
    public void setState(State state) {
        if (carrierObj == null) return; // Segurança
        final int frameIndex = (state == State.FULL) ? 1 : 0;
        SwingUtilities.invokeLater(() -> {
            carrierObj.setAnimationFrame(frameIndex);
            if (painel != null) {
                 painel.repaint();
            }
        });
    }
}