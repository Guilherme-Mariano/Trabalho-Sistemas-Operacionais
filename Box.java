// Box.java
import javax.swing.SwingUtilities;

/**
 * Representa a caixa visual que o empacotador carrega.
 */
public class Box {

    private PainelDeDesenho painel;
    private ObjetoGrafico boxObj;
    private ObjetoGrafico ownerObj; // mpacotador dono da caixa
    
    public enum State { BELOW, ABOVE }

    /**
     * @param panel 
     * @param owner Referência empacotador
     */
    public Box(PainelDeDesenho panel, ObjetoGrafico owner) {
        this.painel = panel;
        this.ownerObj = owner;
        
        // Define tamanho da caixa
        int boxWidth = 70;
        int boxHeight = 70;

        // Calcula a posição inicial (abaixo do dono)
        int initialX = calculateX(owner.getX(), owner.getLargura(), boxWidth);
        int initialY = calculateY(owner.getY(), owner.getAltura(), boxHeight, State.BELOW);

        // Cria o ObjetoGrafico interno para a caixa
        this.boxObj = new ObjetoGrafico(
            "/GameAsset/box.png", 
            initialX,
            initialY,
            boxWidth,
            boxHeight
        );
        
        // A caixa começa invisível
        this.boxObj.setVisible(false);
    }

    /**
     * @return O ObjetoGrafico da caixa.
     */
    public ObjetoGrafico getObjetoGrafico() {
        return this.boxObj;
    }

    // Calcula a posição X para centralizar a caixa horizontalmente
    private int calculateX(int ownerX, int ownerWidth, int boxWidth) {
        return ownerX + (ownerWidth / 2) - (boxWidth / 2);
    }
    
    // Calcula a posição Y (acima ou abaixo)
    private int calculateY(int ownerY, int ownerHeight, int boxHeight, State state) {
        if (state == State.ABOVE) {
            return ownerY - boxHeight; 
        } else { // State.BELOW
            return ownerY + ownerHeight - 50; 
        }
    }

    /**
     * @param state ACIMA ou ABAIXO
     */
    public void updatePosition(State state) {
        if (ownerObj == null) return;

        int ownerX = ownerObj.getX();
        int ownerY = ownerObj.getY();
        int ownerWidth = ownerObj.getLargura();
        int ownerHeight = ownerObj.getAltura();
        
        int boxWidth = boxObj.getLargura();
        int boxHeight = boxObj.getAltura();

        int newX = calculateX(ownerX, ownerWidth, boxWidth);
        int newY = calculateY(ownerY, ownerHeight, boxHeight, state) + 35;
        
        boxObj.setLocation(newX, newY);
    }
    
    /**
     * @param visible True mostra, False esconde
     */
    public void setVisible(boolean visible) {
        SwingUtilities.invokeLater(() -> {
            boxObj.setVisible(visible);
            painel.repaint(); // mudança de visibilidade
        });
    }
}