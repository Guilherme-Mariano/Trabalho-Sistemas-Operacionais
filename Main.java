// Main.java
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.concurrent.Semaphore;
import java.util.Random; // Mantido
import java.util.List;

public class Main {

    private static final int N_CAIXAS_DEFAULT = 5;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // --- Coleta de Inputs Iniciais ---
            final int tempoViagemTrem = getInputAsInt("Tempo de Viagem do Trem (segundos):", 10);
            final int tempoArmazenamentoGlobal = getInputAsInt("Tempo de ARMAZENAMENTO (segundos - para todos empacotadores):", 1);
            final int nCaixasParaPartida = getInputAsInt("Nº de Caixas para Partida (N):", N_CAIXAS_DEFAULT);

            int M_value;
            while (true) {
                M_value = getInputAsInt("Capacidade Máxima do Armazém (M > N):", nCaixasParaPartida + 10);
                if (M_value > nCaixasParaPartida) {
                    break;
                } else {
                    JOptionPane.showMessageDialog(null, "Erro: A Capacidade Máxima (M) deve ser maior que o N de Caixas para Partida.", "Entrada Inválida", JOptionPane.ERROR_MESSAGE);
                }
            }
            final int capacidadeMaximaM = M_value;
            // --- Fim dos Inputs ---

            // --- Setup da Janela e Painel ---
            JFrame frame = new JFrame("Simulação Sincronizada com Semáforo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            final PainelDeDesenho painel = new PainelDeDesenho();
            frame.add(painel, BorderLayout.CENTER);

            // --- Semáforos ---
            final Semaphore semaforoCaixasProntas = new Semaphore(0);
            final Semaphore mutexArmazemA = new Semaphore(1);
            final Semaphore semaforoEspacoDisponivel = new Semaphore(0);

            // --- Objetos Estáticos ---
            final CityObject cidadeA = new CityObject(painel, 50, 580);
            final Warehouse armazemA = new Warehouse(painel, 50, 400);
            final CityObject cidadeB = new CityObject(painel, 900, 580);
            final Warehouse armazemB = new Warehouse(painel, 900, 400);

            final int trackYPosition = 390;
            final int trackPieceWidth = 50;
            final int trackPieceHeight = 40;
            final int numberOfTrackPieces = 22;
            final Track trainTrack = new Track(
                painel, 10, trackYPosition, trackPieceWidth, trackPieceHeight,
                numberOfTrackPieces, "/GameAsset/track.png"
            );

            // --- Thread do Trem ---
            final ThreadTrem tremThread = new ThreadTrem(
                painel,
                semaforoCaixasProntas,
                nCaixasParaPartida,
                tempoViagemTrem,
                semaforoEspacoDisponivel
            );

            // --- Registro de Objetos Gráficos ---
            List<ObjetoGrafico> trackPieces = trainTrack.getTrackPieces();
            for (ObjetoGrafico piece : trackPieces) {
                painel.adicionarObjetoParaDesenhar(piece);
            }
            painel.adicionarObjetoParaDesenhar(tremThread.getObjetoGrafico());
            if (tremThread.getCarrier() != null && tremThread.getCarrier().getObjetoGrafico() != null) {
                painel.adicionarObjetoParaDesenhar(tremThread.getCarrier().getObjetoGrafico());
            }
            painel.adicionarObjetoParaDesenhar(cidadeA.getObjetoGrafico());
            painel.adicionarObjetoParaDesenhar(armazemA.getObjetoGrafico());
            painel.adicionarObjetoParaDesenhar(cidadeB.getObjetoGrafico());
            painel.adicionarObjetoParaDesenhar(armazemB.getObjetoGrafico());

            // --- Botão Adicionar Empacotador ---
            JButton addButton = new JButton("Adicionar Empacotador");
            addButton.addActionListener(e -> { // Lambda expression (inner class context)
                String input = JOptionPane.showInputDialog(frame, "Digite o tempo de EMPACOTAMENTO (segundos):", "Tempo de Trabalho", JOptionPane.PLAIN_MESSAGE);
                if (input != null && !input.trim().isEmpty()) {
                    try {
                        int tempoEmpacotamentoInput = Integer.parseInt(input.trim());
                        if (tempoEmpacotamentoInput > 0) {
                            // Cria um NOVO empacotador
                            ThreadEmpacotador novoEmpacotador = new ThreadEmpacotador(
                                painel,
                                semaforoCaixasProntas,
                                mutexArmazemA,
                                armazemA,
                                tempoArmazenamentoGlobal, // Tempo de ARMAZENAMENTO (global)
                                capacidadeMaximaM,       // M (global)
                                semaforoEspacoDisponivel,
                                tempoEmpacotamentoInput  // Tempo de EMPACOTAMENTO (individual)
                            );

                            painel.adicionarObjetoParaDesenhar(novoEmpacotador.getObjetoGrafico());
                            if (novoEmpacotador.getBox() != null && novoEmpacotador.getBox().getObjetoGrafico() != null) {
                                painel.adicionarObjetoParaDesenhar(novoEmpacotador.getBox().getObjetoGrafico());
                            } else {
                                System.err.println("ERRO: Caixa ou Objeto Gráfico da Caixa nulos!");
                            }

                            novoEmpacotador.start();
                        } else {
                            JOptionPane.showMessageDialog(frame, "Por favor, digite um número positivo.", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Entrada inválida. Por favor, digite um número inteiro.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(addButton);
            frame.add(buttonPanel, BorderLayout.SOUTH);

            // --- Inicia a thread do trem ---
            tremThread.start();

            // --- Finaliza Setup da Janela ---
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    /** Helper para obter input inteiro do usuário com valor padrão. */
    private static int getInputAsInt(String message, int defaultValue) {
        while (true) {
            String input = JOptionPane.showInputDialog(null, message, "Configuração Inicial", JOptionPane.PLAIN_MESSAGE);
            if (input == null) {
                 System.out.println("Input cancelado para '" + message + "', usando valor padrão: " + defaultValue);
                 return defaultValue;
            }
            try {
                int value = Integer.parseInt(input.trim());
                 // Permite 0 apenas para tempo de armazenamento
                if (value >= 0) {
                    // Verificação M > N é feita após coletar ambos os valores
                    return value;
                } else {
                     JOptionPane.showMessageDialog(null, "Por favor, digite um número não negativo.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Entrada inválida. Por favor, digite um número inteiro.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}