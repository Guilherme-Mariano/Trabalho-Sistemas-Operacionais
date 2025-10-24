// PainelDeDesenho.java
import java.awt.Color; // Importar Color
import java.awt.Dimension;
import java.awt.Font; // Importar Font
import java.awt.FontMetrics; // Importar FontMetrics
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore; // Importar Semaphore
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class PainelDeDesenho extends JPanel {

    private final List<ObjetoGrafico> objetosParaDesenhar;
    private ImageIcon backgroundImage;
    // Referências para a contagem de caixas
    private Semaphore semaforoCaixas;
    private Warehouse warehouseToTrack; // O armazém cuja contagem será exibida

    /**
     * recebe semáforo e o armazém a monitorar.
     */
    public PainelDeDesenho(Semaphore semaforoCaixasProntas, Warehouse warehouseA) {
        this.semaforoCaixas = semaforoCaixasProntas; // Armazena o semáforo
        this.warehouseToTrack = warehouseA;      // Armazena o armazém A
        
        objetosParaDesenhar = Collections.synchronizedList(new ArrayList<>());
        
        java.net.URL imgURL = getClass().getResource("/GameAsset/background.png");
        if (imgURL != null) {
            this.backgroundImage = new ImageIcon(imgURL);
        } else {
            System.err.println("FALHA AO CARREGAR BACKGROUND: /GameAsset/background.png");
        }

        this.setPreferredSize(new Dimension(1200, 800));
        this.setDoubleBuffered(true);
    }

    public void adicionarObjetoParaDesenhar(ObjetoGrafico obj) {
        if (obj != null) { 
            this.objetosParaDesenhar.add(obj);
        } else {
            System.err.println("Tentativa de adicionar objeto gráfico nulo ao painel!");
        }
    }

    public void removerObjetoParaDesenhar(ObjetoGrafico obj) {
        if (obj != null) { 
            boolean removed = this.objetosParaDesenhar.remove(obj);
             if (!removed) {
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. Desenha o fundo
        if (backgroundImage != null) {
            g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(java.awt.Color.LIGHT_GRAY); 
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        // 2. Desenha todos os objetos (trem, robôs, caixas, armazéns, etc.)
        synchronized (objetosParaDesenhar) {
            for (ObjetoGrafico obj : objetosParaDesenhar) {
                if (obj != null && obj.isVisible() && obj.getImagem() != null) {
                    g.drawImage(obj.getImagem().getImage(), obj.getX(), obj.getY(), this);
                }
            }
        }
        
        // --- Desenha a Contagem de Caixas ---
        if (semaforoCaixas != null && warehouseToTrack != null && warehouseToTrack.getObjetoGrafico() != null) {
            // Pega a contagem atual do semáforo
            int currentBoxCount = semaforoCaixas.availablePermits();
            String countText = "Caixas: " + currentBoxCount;

            // fonte e a cor
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.setColor(Color.WHITE);

            // Calcula a posição do texto
            ObjetoGrafico warehouseObj = warehouseToTrack.getObjetoGrafico();
            int warehouseX = warehouseObj.getX();
            int warehouseY = warehouseObj.getY();
            int warehouseWidth = warehouseObj.getLargura();
            
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(countText);
            int textX = warehouseX + (warehouseWidth / 2) - (textWidth / 2);
            int textY = warehouseY + 50; 

            // Desenha
            g.drawString(countText, textX, textY);
        }
    }
}