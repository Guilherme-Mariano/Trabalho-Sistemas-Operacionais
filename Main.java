// Main.java
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JButton; // Importar JButton
import javax.swing.JOptionPane; // Importar JOptionPane
import javax.swing.JPanel; // Importar JPanel
import java.awt.BorderLayout; // Importar BorderLayout
import java.awt.FlowLayout; // Importar FlowLayout
import java.util.concurrent.Semaphore;
import java.util.Random;
import java.util.List;

public class Main {

    private static final int N_CAIXAS_PARA_PARTIDA = 2;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Simulação Sincronizada com Semáforo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // Botão com borderlayout
            frame.setLayout(new BorderLayout()); 

            PainelDeDesenho painel = new PainelDeDesenho();
            // Colocando Painel de desenho no centro
            frame.add(painel, BorderLayout.CENTER); 
            
            // --- COORDENAÇÃO CENTRAL ---
            // Variáveis Finais para serem acessíveis no ActionListener
            final Semaphore semaforoCaixasProntas = new Semaphore(0);
            final Semaphore mutexArmazemA = new Semaphore(1); 

            // 1. Cria os objetos estáticos PRIMEIRO
            final CityObject cidadeA = new CityObject(painel, 50, 580); 
            final Warehouse armazemA = new Warehouse(painel, 50, 400); 
            final CityObject cidadeB = new CityObject(painel, 900, 580);
            final Warehouse armazemB = new Warehouse(painel, 900, 400);

            // coordenada Y do trilho
            final int trackYPosition = 390; 
            final int trackPieceWidth = 50; 
            final int trackPieceHeight = 40;// altura do trilho
            final int numberOfTrackPieces = 22; 
            final Track trainTrack = new Track(
                painel, 10, trackYPosition, trackPieceWidth, trackPieceHeight, 
                numberOfTrackPieces, "/GameAsset/track.png" // asset do trilho
            );
            
            // 2. Cria a thread do Trem
            final ThreadTrem tremThread = new ThreadTrem(painel, semaforoCaixasProntas, N_CAIXAS_PARA_PARTIDA);
            
            // 3. Registra os objetos estáticos, O TRILHO, e o trem
            List<ObjetoGrafico> trackPieces = trainTrack.getTrackPieces();
            for(ObjetoGrafico piece : trackPieces) {
                painel.adicionarObjetoParaDesenhar(piece);
            }
            
            painel.adicionarObjetoParaDesenhar(tremThread.getObjetoGrafico()); 
            // Adiciona o Carrier do trem ao painel
            if (tremThread.getCarrier() != null && tremThread.getCarrier().getObjetoGrafico() != null) {
                painel.adicionarObjetoParaDesenhar(tremThread.getCarrier().getObjetoGrafico());
            }

            painel.adicionarObjetoParaDesenhar(cidadeA.getObjetoGrafico());
            painel.adicionarObjetoParaDesenhar(armazemA.getObjetoGrafico());
            painel.adicionarObjetoParaDesenhar(cidadeB.getObjetoGrafico());
            painel.adicionarObjetoParaDesenhar(armazemB.getObjetoGrafico());

            // Adiciona o Botão
            JButton addButton = new JButton("Adicionar Empacotador");
            addButton.addActionListener(e -> {
                String input = JOptionPane.showInputDialog(frame, "Digite o tempo de empacotamento (segundos):", "Tempo de Trabalho", JOptionPane.PLAIN_MESSAGE);
                if (input != null && !input.trim().isEmpty()) {
                    try {
                        int tempoDeTrabalho = Integer.parseInt(input.trim());
                        if (tempoDeTrabalho > 0) {
                            // Cria um NOVO empacotador com o tempo fornecido
                            ThreadEmpacotador novoEmpacotador = new ThreadEmpacotador(
                                painel,                  
                                semaforoCaixasProntas,   
                                mutexArmazemA,           
                                armazemA,                
                                tempoDeTrabalho          
                            );
                            
                            // Adiciona o robô E a sua caixa ao painel
                            painel.adicionarObjetoParaDesenhar(novoEmpacotador.getObjetoGrafico());
                            if (novoEmpacotador.getBox() != null && novoEmpacotador.getBox().getObjetoGrafico() != null) {
                                 painel.adicionarObjetoParaDesenhar(novoEmpacotador.getBox().getObjetoGrafico());
                            } else {
                                 System.err.println("ERRO: Caixa ou Objeto Gráfico da Caixa nulos!");
                            }
                            
                            // Inicia a thread do novo empacotador
                            novoEmpacotador.start();
                        } else {
                             JOptionPane.showMessageDialog(frame, "Por favor, digite um número positivo.", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Entrada inválida. Por favor, digite um número inteiro.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            
            // Cria um painel para o botão
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(addButton);
            
            // Adiciona o painel do botão na parte inferior da janela
            frame.add(buttonPanel, BorderLayout.SOUTH);

            
            // 5. Inicia a thread do trem AUTOMÁTICA
            tremThread.start();
            
            // Ajusta o tamanho da janela DEPOIS de adicionar todos os componentes
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}