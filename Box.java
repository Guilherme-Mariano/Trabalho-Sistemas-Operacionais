// Box.java
import javax.swing.SwingUtilities;

/**
 * Representa a caixa visual que o empacotador carrega.
 * Sua posição é calculada dinamicamente em relação ao seu "dono".
 */
public class Box {

    private PainelDeDesenho painel;
    private ObjetoGrafico boxObj;
    private ObjetoGrafico ownerObj; // O ObjetoGrafico do empacotador dono desta caixa
    
    // Enum para definir os estados de posicionamento relativo ao dono
    public enum State { BELOW, ABOVE }

    /**
     * Construtor da Box.
     * @param panel Referência ao painel de desenho principal.
     * @param owner Referência ao ObjetoGrafico do empacotador que "possui" esta caixa.
     */
    public Box(PainelDeDesenho panel, ObjetoGrafico owner) {
        this.painel = panel;
        this.ownerObj = owner;
        
        // Define o tamanho da caixa (ajuste conforme sua imagem)
        int boxWidth = 70;
        int boxHeight = 70;

        // Calcula a posição inicial (abaixo do dono)
        int initialX = calculateX(owner.getX(), owner.getLargura(), boxWidth);
        int initialY = calculateY(owner.getY(), owner.getAltura(), boxHeight, State.BELOW);

        // Cria o ObjetoGrafico interno para a caixa
        this.boxObj = new ObjetoGrafico(
            "/GameAsset/box.png", // Certifique-se de ter esta imagem na pasta GameAsset!
            initialX,
            initialY,
            boxWidth,
            boxHeight
        );
        
        // A caixa começa invisível
        this.boxObj.setVisible(false);
    }

    /**
     * Retorna o ObjetoGrafico interno desta caixa, para que possa ser adicionado
     * ao painel de desenho.
     * @return O ObjetoGrafico da caixa.
     */
    public ObjetoGrafico getObjetoGrafico() {
        return this.boxObj;
    }

    // Calcula a posição X para centralizar a caixa horizontalmente com o dono
    private int calculateX(int ownerX, int ownerWidth, int boxWidth) {
        return ownerX + (ownerWidth / 2) - (boxWidth / 2);
    }
    
    // Calcula a posição Y (acima ou abaixo do dono)
    private int calculateY(int ownerY, int ownerHeight, int boxHeight, State state) {
        if (state == State.ABOVE) {
            return ownerY - boxHeight; // 5 pixels de espaço acima
        } else { // State.BELOW
            return ownerY + ownerHeight - 50; // 5 pixels de espaço abaixo
        }
    }

    /**
     * Atualiza a posição da caixa para que ela acompanhe o dono.
     * Deve ser chamado sempre que o dono se move, dentro de SwingUtilities.invokeLater.
     * @param state Define se a caixa deve ficar ACIMA ou ABAIXO do dono.
     */
    public void updatePosition(State state) {
        if (ownerObj == null) return; // Segurança

        // Obtém as propriedades atuais do dono
        int ownerX = ownerObj.getX();
        int ownerY = ownerObj.getY();
        int ownerWidth = ownerObj.getLargura();
        int ownerHeight = ownerObj.getAltura();
        
        // Obtém as propriedades da caixa
        int boxWidth = boxObj.getLargura();
        int boxHeight = boxObj.getAltura();

        // Calcula as novas coordenadas da caixa
        int newX = calculateX(ownerX, ownerWidth, boxWidth);
        int newY = calculateY(ownerY, ownerHeight, boxHeight, state) + 35;
        
        // Define a nova localização da caixa (o repaint será feito pelo chamador)
        boxObj.setLocation(newX, newY);
    }
    
    /**
     * Define se a caixa deve ser visível ou não na tela.
     * @param visible True para mostrar, False para esconder.
     */
    public void setVisible(boolean visible) {
        // Usa invokeLater para garantir a segurança da thread
        SwingUtilities.invokeLater(() -> {
            boxObj.setVisible(visible);
            painel.repaint(); // Solicita redesenho para aplicar a mudança de visibilidade
        });
    }
}