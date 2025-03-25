import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javax.swing.JOptionPane;

import busca.BuscaLargura;
import busca.BuscaProfundidade;
import busca.Estado;
import busca.MostraStatusConsole;
import busca.Nodo;

public class Labirinto implements Estado {
    @Override
    public String getDescricao() {
        throw new UnsupportedOperationException("Unimplemented method 'getDescricao'");
    }

    final char matriz[][];
    int linhaEntrada1, colunaEntrada1;
    int linhaEntrada2, colunaEntrada2;
    int linhaSaida, colunaSaida;
    final String op;
    final int entradaAtiva;

    char[][] clonar(char origem[][]) {
        char destino[][] = new char[origem.length][origem.length];
        for (int i = 0; i < origem.length; i++) {
            for (int j = 0; j < origem.length; j++) {
                destino[i][j] = origem[i][j];
            }
        }
        return destino;
    }
    
    public Labirinto(char m[][], int le1, int ce1, int le2, int ce2, 
                    int ls, int cs, String o, int entradaAtiva) {
        this.matriz = clonar(m);
        this.linhaEntrada1 = le1;
        this.colunaEntrada1 = ce1;
        this.linhaEntrada2 = le2;
        this.colunaEntrada2 = ce2;
        this.linhaSaida = ls;
        this.colunaSaida = cs;
        this.op = o;
        this.entradaAtiva = entradaAtiva;
    }
    
    public Labirinto(int dimensao, int numObstaculos) {
        this.matriz = new char[dimensao][dimensao];
        this.op = "Estado inicial";
        this.entradaAtiva = 0;

        Random gerador = new Random();

        int saida = gerador.nextInt(dimensao * dimensao);
        this.linhaSaida = saida / dimensao;
        this.colunaSaida = saida % dimensao;

        int entrada1 = gerador.nextInt(dimensao * dimensao);
        while (entrada1 == saida) {
            entrada1 = gerador.nextInt(dimensao * dimensao);
        }
        this.linhaEntrada1 = entrada1 / dimensao;
        this.colunaEntrada1 = entrada1 % dimensao;

        int entrada2 = gerador.nextInt(dimensao * dimensao);
        while (entrada2 == saida || entrada2 == entrada1) {
            entrada2 = gerador.nextInt(dimensao * dimensao);
        }
        this.linhaEntrada2 = entrada2 / dimensao;
        this.colunaEntrada2 = entrada2 % dimensao;

        for (int i = 0; i < dimensao; i++) {
            for (int j = 0; j < dimensao; j++) {
                if (i == linhaSaida && j == colunaSaida) {
                    this.matriz[i][j] = 'S';
                } else if (i == linhaEntrada1 && j == colunaEntrada1) {
                    this.matriz[i][j] = '1';
                } else if (i == linhaEntrada2 && j == colunaEntrada2) {
                    this.matriz[i][j] = '2';
                } else {
                    this.matriz[i][j] = 'O';
                }
            }
        }

        int obstaculosColocados = 0;
        while (obstaculosColocados < numObstaculos) {
            int pos = gerador.nextInt(dimensao * dimensao);
            int i = pos / dimensao;
            int j = pos % dimensao;
            
            if (this.matriz[i][j] == 'O') {
                this.matriz[i][j] = '@';
                obstaculosColocados++;
            }
        }
    }

    @Override
    public boolean ehMeta() {
        if (entradaAtiva == 1) {
            return this.linhaEntrada1 == this.linhaSaida && 
                   this.colunaEntrada1 == this.colunaSaida;
        } else {
            return this.linhaEntrada2 == this.linhaSaida && 
                   this.colunaEntrada2 == this.colunaSaida;
        }
    }

    @Override
    public int custo() {
        return 1;
    }

    @Override
    public List<Estado> sucessores() {
        List<Estado> visitados = new LinkedList<>();
        if (entradaAtiva == 1) {
            gerarSucessores(visitados, linhaEntrada1, colunaEntrada1, 1);
        } else {
            gerarSucessores(visitados, linhaEntrada2, colunaEntrada2, 2);
        }
        return visitados;
    }

    private void gerarSucessores(List<Estado> visitados, int linha, int coluna, int entrada) {
        int[][] direcoes = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : direcoes) {
            int novaLinha = linha + dir[0];
            int novaColuna = coluna + dir[1];
            if (movimentoValido(novaLinha, novaColuna)) {
                adicionarSucessor(visitados, novaLinha, novaColuna, entrada);
            }
        }
    }

    private boolean movimentoValido(int linha, int coluna) {
        return linha >= 0 && linha < matriz.length && 
               coluna >= 0 && coluna < matriz.length && 
               matriz[linha][coluna] != '@';
    }

    private void adicionarSucessor(List<Estado> visitados, int novaLinha, int novaColuna, int entrada) {
        char[][] novaMatriz = clonar(matriz);
        if (entrada == 1) {
            novaMatriz[linhaEntrada1][colunaEntrada1] = 'O';
            novaMatriz[novaLinha][novaColuna] = '1';
        } else {
            novaMatriz[linhaEntrada2][colunaEntrada2] = 'O';
            novaMatriz[novaLinha][novaColuna] = '2';
        }
        Labirinto novo = new Labirinto(
            novaMatriz,
            (entrada == 1) ? novaLinha : linhaEntrada1,
            (entrada == 1) ? novaColuna : colunaEntrada1,
            (entrada == 2) ? novaLinha : linhaEntrada2,
            (entrada == 2) ? novaColuna : colunaEntrada2,
            linhaSaida, colunaSaida,
            "Movendo entrada " + entrada,
            entrada
        );
        if (!visitados.contains(novo)) {
            visitados.add(novo);
        }
    }

    public static void main(String[] a) {
        try {
            int dimensao = Integer.parseInt(JOptionPane.showInputDialog("Dimensão do labirinto:"));
            int obstaculos = Integer.parseInt(JOptionPane.showInputDialog("Número de obstáculos:"));
            
            String[] opcoes = {"Profundidade", "Largura"};
            int metodo1 = JOptionPane.showOptionDialog(null, "Método para Entrada 1:", 
                "Configuração", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, 
                null, opcoes, opcoes[0]);
            
            int metodo2 = JOptionPane.showOptionDialog(null, "Método para Entrada 2:", 
                "Configuração", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, 
                null, opcoes, opcoes[1]);

            Labirinto lab = new Labirinto(dimensao, obstaculos);
            System.out.println("Labirinto inicial:\n" + lab);

            Nodo sol1 = resolver(lab, 1, metodo1);
            Nodo sol2 = resolver(lab, 2, metodo2);

            exibirComparacao(sol1, sol2);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
        }
        System.exit(0);
    }

    private static Nodo resolver(Labirinto lab, int entrada, int metodo) {
        Labirinto estado = new Labirinto(
            lab.matriz, 
            lab.linhaEntrada1, lab.colunaEntrada1,
            lab.linhaEntrada2, lab.colunaEntrada2,
            lab.linhaSaida, lab.colunaSaida,
            "Resolvendo entrada " + entrada,
            entrada
        );
        
        if (metodo == 0) {
            return new BuscaProfundidade(new MostraStatusConsole()).busca(estado);
        } else {
            return new BuscaLargura(new MostraStatusConsole()).busca(estado);
        }
    }

    private static void exibirComparacao(Nodo sol1, Nodo sol2) {
        System.out.println("\n=== RESULTADOS ===");
        System.out.println("Entrada 1: " + formatarSolucao(sol1));
        System.out.println("Entrada 2: " + formatarSolucao(sol2));
        
        if (sol1 != null && sol2 != null) {
            if (sol1.getProfundidade() < sol2.getProfundidade()) {
                System.out.println("Entrada 1 teve melhor desempenho!");
            } else if (sol1.getProfundidade() > sol2.getProfundidade()) {
                System.out.println("Entrada 2 teve melhor desempenho!");
            } else {
                System.out.println("Ambas tiveram o mesmo desempenho!");
            }
        }
    }

    private static String formatarSolucao(Nodo sol) {
        if (sol == null) return "Sem solução";
        return sol.getProfundidade() + " passos";
    }

}