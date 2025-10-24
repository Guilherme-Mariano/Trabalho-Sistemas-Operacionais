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
     * @param panel
     * @param train Referência locomotiva.
     */
    public Carrier(PainelDeDesenho panel, ObjetoGrafico train) {
        this.painel = panel;
        this.trainObj = train;

        // Define o tamanho do vagão 
        int carrierWidth = 100;
        int carrierHeight = 70;

        Direcao initialDirection = train.isVisible() ? Direcao.DIREITA : Direcao.ESQUERDA; // Suposição inicial
        int initialX = calculateX(train.getX(), train.getLargura(), initialDirection, carrierWidth);
        int initialY = train.getY() + (train.getAltura() / 2) - (carrierHeight / 2); // Alinha verticalmente

        this.carrierObj = new ObjetoGrafico(
            initialX,
            initialY,
            carrierWidth,
            carrierHeight,
            IMAGE_EMPTY, 
            IMAGE_FULL   
        );

        // Começa VAZIO
        setState(State.EMPTY);
    }

    /** Retorna o ObjetoGrafico do vagão para ser adicionado ao painel. */
    public ObjetoGrafico getObjetoGrafico() {
        return this.carrierObj;
    }

    /** Calcula a posição X do vagão. */
    private int calculateX(int trainX, int trainWidth, Direcao trainDirection, int carrierWidth) {
        if (trainDirection == Direcao.DIREITA) {
            return trainX - carrierWidth;
        } else { 
            return trainX + trainWidth;
        }
    }

    /**
     * @param trainDirection direção do trem.
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
        // alinhamento vertical
        int newY = trainY + (trainHeight / 2) - (carrierHeight / 2);

        carrierObj.setLocation(newX, newY);
    }

    /**
     * Define a aparência do vagão (vazio ou cheio).
     * @param state O estado (EMPTY | FULL).
     */
    public void setState(State state) {
        if (carrierObj == null) return;
        final int frameIndex = (state == State.FULL) ? 1 : 0;
        SwingUtilities.invokeLater(() -> {
            carrierObj.setAnimationFrame(frameIndex);
            if (painel != null) {
                 painel.repaint();
            }
        });
    }
}