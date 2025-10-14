import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon; // Importe para ImageIcon
import javax.swing.JPanel;

public class PainelDeDesenho extends JPanel {

    private final List<ObjetoGrafico> objetosParaDesenhar;
    private ImageIcon backgroundImage; // Nova propriedade para a imagem de fundo

    public PainelDeDesenho() {
        objetosParaDesenhar = Collections.synchronizedList(new ArrayList<>());
        
        // Carrega a imagem de fundo no construtor
        // Certifique-se de que "background.png" exista na pasta GameAsset
        java.net.URL imgURL = getClass().getResource("/GameAsset/background.png");
        if (imgURL != null) {
            this.backgroundImage = new ImageIcon(imgURL);
            System.out.println("Background carregado: " + imgURL.getPath());
        } else {
            System.err.println("FALHA AO CARREGAR BACKGROUND: /GameAsset/background.png");
            System.err.println("Verifique se o arquivo background.png está na pasta GameAsset.");
        }

        this.setPreferredSize(new Dimension(1400, 900)); // Dimensões do painel
        this.setDoubleBuffered(true);
    }

    public void adicionarObjetoParaDesenhar(ObjetoGrafico obj) {
        this.objetosParaDesenhar.add(obj);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Limpa a tela (preenche com a cor de fundo do JPanel)

        // 1. Desenha a imagem de fundo PRIMEIRO
        if (backgroundImage != null) {
            // Desenha a imagem de fundo para preencher todo o painel
            // Ajusta automaticamente a imagem de fundo para o tamanho do painel.
            g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
        } else {
            // Se a imagem de fundo não carregar, pelo menos preenche com uma cor
            g.setColor(java.awt.Color.LIGHT_GRAY); 
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        // 2. Desenha todos os outros objetos POR CIMA do background
        synchronized (objetosParaDesenhar) {
            for (ObjetoGrafico obj : objetosParaDesenhar) {
                if (obj != null && obj.getImagem() != null) {
                    g.drawImage(obj.getImagem().getImage(), obj.getX(), obj.getY(), this);
                }
            }
        }
    }
}