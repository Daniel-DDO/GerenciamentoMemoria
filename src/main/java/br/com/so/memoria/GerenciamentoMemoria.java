package br.com.so.memoria;

import br.com.so.memoria.service.AlocacaoParticao;

import java.util.Scanner;

public class GerenciamentoMemoria {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int opcao;

        do {
            GerenciamentoMemoria.iniciarPrograma();
            opcao = scanner.nextInt();

            switch (opcao) {
                case 1:
                    GerenciamentoMemoria.alocacaoParticao();
                    break;
                case 2:
                    GerenciamentoMemoria.alocacaoPaginacao();
                    break;
                case 3:
                    System.err.println("Encerrando...");
                    return;
                default:
                    System.err.println("Erro, tente novamente.");
                    break;
            }
        } while (opcao != 3);
    }

    public static void iniciarPrograma() {
        System.out.println("Gerenciamento de Memória");
        System.out.println("\n1. Alocação com partições\n"+
                "2. Alocação com paginação\n"+
                "3. Encerrar");
    }

    public static void alocacaoParticao() {
        System.out.println("\nAlocação de memória com partições.\n");
        AlocacaoParticao.getAlocacaoParticao().executarAlocacaoParticao();
    }

    public static void alocacaoPaginacao() {
        System.out.println("\nAlocação de memória com paginação.\n");
        //chamar o metodo que executa
    }
}
