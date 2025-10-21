// PainelDeDesenho.java
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class PainelDeDesenho extends JPanel {

    private final List<ObjetoGrafico> objetosParaDesenhar;
    private ImageIcon backgroundImage;

    public PainelDeDesenho() {
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
        this.objetosParaDesenhar.add(obj);
    }

    public void removerObjetoParaDesenhar(ObjetoGrafico obj) {
        this.objetosParaDesenhar.remove(obj);
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(java.awt.Color.LIGHT_GRAY); 
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        synchronized (objetosParaDesenhar) {
            for (ObjetoGrafico obj : objetosParaDesenhar) {
                if (obj != null && obj.getImagem() != null) {
                    g.drawImage(obj.getImagem().getImage(), obj.getX(), obj.getY(), this);
                }
            }
        }
    }
}