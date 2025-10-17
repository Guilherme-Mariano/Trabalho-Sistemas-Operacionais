// ObjetoGrafico.java
import javax.swing.ImageIcon;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ObjetoGrafico {

    private ImageIcon imagemAtual;
    private ImageIcon imagemOriginal;
    private ImageIcon imagemEspelhada;
    private List<ImageIcon> animationFrames;

    private int x;
    private int y;
    private int largura;
    private int altura;

    /** Construtor para uma única imagem com espelhamento (usado pelo Trem). */
    public ObjetoGrafico(String caminhoDaImagem, int xInicial, int yInicial, int larguraDesejada, int alturaDesejada) {
        this.x = xInicial;
        this.y = yInicial;
        this.largura = larguraDesejada;
        this.altura = alturaDesejada;
        carregarEProcessarImagensParaEspelhamento(caminhoDaImagem);
    }
    
    /** Construtor para múltiplos frames de animação (usado pelo Empacotador). */
    public ObjetoGrafico(int xInicial, int yInicial, int larguraDesejada, int alturaDesejada, String... caminhosDasImagens) {
        this.x = xInicial;
        this.y = yInicial;
        this.largura = larguraDesejada;
        this.altura = alturaDesejada;
        this.animationFrames = new ArrayList<>();
        carregarFramesDeAnimacao(caminhosDasImagens);
    }

    private void carregarEProcessarImagensParaEspelhamento(String caminhoDaImagem) {
        java.net.URL imgURL = getClass().getResource(caminhoDaImagem);
        if (imgURL == null) {
            System.err.println("FALHA AO CARREGAR IMAGEM: " + caminhoDaImagem);
            return;
        }
        ImageIcon iconOriginal = new ImageIcon(imgURL);
        Image imgRedimensionada = iconOriginal.getImage().getScaledInstance(this.largura, this.altura, Image.SCALE_SMOOTH);
        this.imagemOriginal = new ImageIcon(imgRedimensionada);
        this.imagemEspelhada = criarImagemEspelhada(imgRedimensionada);
        this.imagemAtual = this.imagemOriginal;
    }
    
    private void carregarFramesDeAnimacao(String[] caminhos) {
        for (String caminho : caminhos) {
            java.net.URL imgURL = getClass().getResource(caminho);
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                Image imgRedimensionada = icon.getImage().getScaledInstance(this.largura, this.altura, Image.SCALE_SMOOTH);
                animationFrames.add(new ImageIcon(imgRedimensionada));
            } else {
                System.err.println("FALHA AO CARREGAR FRAME DE ANIMAÇÃO: " + caminho);
            }
        }
        if (!animationFrames.isEmpty()) {
            this.imagemAtual = animationFrames.get(0);
        }
    }

    private ImageIcon criarImagemEspelhada(Image img) {
        BufferedImage bufferedImage = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-largura, 0);
        g2d.setTransform(tx);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();
        return new ImageIcon(bufferedImage);
    }
    
    public void setDirecao(Direcao direcao) {
        if (direcao == Direcao.DIREITA) {
            this.imagemAtual = this.imagemEspelhada;
        } else {
            this.imagemAtual = this.imagemOriginal;
        }
    }
    
    public void setAnimationFrame(int index) {
        if (animationFrames != null && index >= 0 && index < animationFrames.size()) {
            this.imagemAtual = animationFrames.get(index);
        }
    }

    public ImageIcon getImagem() { return imagemAtual; }
    public int getX() { return x; }
    public int getY() { return y; }
    public void setLocation(int x, int y) { this.x = x; }
}