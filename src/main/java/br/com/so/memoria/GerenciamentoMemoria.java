package br.com.so.memoria;

import br.com.so.memoria.service.AlocacaoParticao;

import java.util.Scanner;

public class GerenciamentoMemoria {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int opcao;

        do {
            GerenciamentoMemoria.iniciarPrograma();
            // Validação de entrada para garantir que seja um número
            while (!scanner.hasNextInt()) {
                System.err.println("Por favor, digite um número válido.");
                scanner.next(); // descarta a entrada inválida
            }
            opcao = scanner.nextInt();

            switch (opcao) {
                case 1:
                    GerenciamentoMemoria.alocacaoParticao();
                    break;
                case 2:
                    GerenciamentoMemoria.alocacaoPaginacao();
                    break;
                case 3:
                    System.out.println("Encerrando...");
                    break;
                default:
                    System.err.println("Erro, opção inválida. Tente novamente.");
                    break;
            }
            System.out.println("\nPressione Enter para continuar...");
            scanner.nextLine(); // Consome a nova linha
            scanner.nextLine(); // Espera pelo Enter

        } while (opcao != 3);
        
        scanner.close();
    }

    public static void iniciarPrograma() {
        System.out.println("\n===================================");
        System.out.println("    Gerenciamento de Memória");
        System.out.println("===================================");
        System.out.println("1. Alocação com partições fixas");
        System.out.println("2. Alocação com paginação (Não implementado)");
        System.out.println("3. Encerrar");
        System.out.print("Escolha uma opção: ");
    }

    public static void alocacaoParticao() {
        System.out.println("\n--- Alocação de memória com partições ---");
        AlocacaoParticao alocador = new AlocacaoParticao();
        alocador.executar();
    }

    public static void alocacaoPaginacao() {
        System.out.println("\n--- Alocação de memória com paginação ---");
        System.err.println("Funcionalidade ainda não implementada.");
    }
}