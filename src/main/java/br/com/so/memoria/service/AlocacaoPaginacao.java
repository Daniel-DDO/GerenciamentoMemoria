package br.com.so.memoria.service;

import br.com.so.memoria.model.Paginacao;
import br.com.so.memoria.model.Processo;
import br.com.so.memoria.model.UnidadeArmazenamento;

import java.util.*;

public class AlocacaoPaginacao {

    private final Scanner scanner = new Scanner(System.in);
    private Paginacao paginacao;
    private UnidadeArmazenamento unidadeEscolhida;
    private final List<Processo> filaDeProcessos = new ArrayList<>();

    public void executar() {
        unidadeEscolhida = escolherUnidade();
        configurarPaginacao();
        configurarProcessos();
        executarAlocacao();
        mostrarEstadoFinal();
    }

    private UnidadeArmazenamento escolherUnidade() {
        System.out.println("\nEscolha a Unidade de Armazenamento");
        int i = 1;
        for (UnidadeArmazenamento unidade : UnidadeArmazenamento.values()) {
            System.out.println(i + ". " + unidade.name() + " (" + unidade + ")");
            i++;
        }

        int escolha = -1;
        while (escolha < 1 || escolha > UnidadeArmazenamento.values().length) {
            System.out.print("Opção: ");
            try {
                escolha = Integer.parseInt(scanner.next());
                if (escolha < 1 || escolha > UnidadeArmazenamento.values().length) {
                    System.err.println("Opção inválida. Tente novamente.");
                }
            } catch (NumberFormatException e) {
                System.err.println("Por favor, digite um número válido.");
            }
        }

        return UnidadeArmazenamento.values()[escolha - 1];
    }

    private void configurarPaginacao() {
        System.out.println("\nConfiguração da Memória");

        System.out.print("Tamanho da memória física (em " + unidadeEscolhida.name() + "): ");
        long tamFisico = lerNumeroPositivo();
        System.out.print("Tamanho da memória virtual (em " + unidadeEscolhida.name() + "): ");
        long tamVirtual = lerNumeroPositivo();

        System.out.print("Tamanho da página/moldura (em " + unidadeEscolhida.name() + "): ");
        long tamPagina = lerNumeroPositivo();

        long tamFisicoBytes = tamFisico * unidadeEscolhida.getFatorParaBytes();
        long tamVirtualBytes = tamVirtual * unidadeEscolhida.getFatorParaBytes();
        long tamPaginaBytes = tamPagina * unidadeEscolhida.getFatorParaBytes();

        if (tamPaginaBytes > tamFisicoBytes) {
            throw new IllegalArgumentException("O tamanho da página não pode ser maior que a memória física.");
        }

        this.paginacao = new Paginacao(tamFisicoBytes, tamVirtualBytes, tamPaginaBytes, unidadeEscolhida);

        System.out.printf("\nMemória configurada com %d molduras e %d páginas virtuais.%n",
                paginacao.getNumMoldurasFisicas(), paginacao.getNumPaginasVirtuais());
    }

    private void configurarProcessos() {
        System.out.println("\n--- Definição da Fila de Processos ---");

        while (true) {
            System.out.print("Digite o nome do processo (ou 'fim' para encerrar): ");
            String nome = scanner.next();

            if (nome.equalsIgnoreCase("fim")) {
                if (filaDeProcessos.isEmpty()) {
                    System.err.println("A fila de processos não pode estar vazia.");
                    continue;
                }
                break;
            }

            System.out.print("Digite o tamanho do processo '" + nome + "' (em " + unidadeEscolhida.name() + "): ");
            long tamanho = lerNumeroPositivo();
            long tamanhoBytes = tamanho * unidadeEscolhida.getFatorParaBytes();

            filaDeProcessos.add(new Processo(nome, tamanhoBytes));
        }
    }

    private void executarAlocacao() {
        System.out.println("\n--- Execução da Alocação Paginada ---");

        for (Processo processo : filaDeProcessos) {
            System.out.printf("%nProcesso %s (%s)%n", processo.getNome(), formatarTamanho(processo.getTamanho()));

            int numPaginasProc = (int) Math.ceil((double) processo.getTamanho() / paginacao.getTamanhoPagina());
            List<Integer> moldurasLivres = paginacao.alocarPaginas(numPaginasProc, processo);

            if (moldurasLivres.isEmpty()) {
                System.err.println("Não há molduras físicas livres suficientes para este processo. Enviado para memória virtual.");
                paginacao.adicionarNaMemoriaVirtual(processo);
            } else {
                System.out.println("Páginas alocadas nas molduras: " + moldurasLivres);
            }

            mostrarEstadoAtual();
        }
    }

    private void mostrarEstadoAtual() {
        System.out.println("\n=== MAPA DE MOLDURAS ===");
        System.out.printf("%-10s | %-12s | %-12s | %-10s%n", "Moldura", "Status", "Proc. ID", "Página");
        System.out.println("------------+--------------+--------------+--------------");

        for (int i = 0; i < paginacao.getNumMoldurasFisicas(); i++) {
            var frame = paginacao.getTabelaMolduras().get(i);
            if (frame == null) {
                System.out.printf("%-10d | %-12s | %-12s | %-10s%n", i, "Livre", "N/A", "N/A");
            } else {
                System.out.printf("%-10d | %-12s | %-12d | %-10d%n", i, "Ocupado", frame.getProcesso().getId(), frame.getPagina());
            }
        }

        if (!paginacao.getMemoriaVirtual().isEmpty()) {
            System.out.println("\n--- PROCESSOS EM MEMÓRIA VIRTUAL ---");
            for (Processo p : paginacao.getMemoriaVirtual()) {
                System.out.printf("- ID=%d, Nome=%s, Tamanho=%s%n", p.getId(), p.getNome(), formatarTamanho(p.getTamanho()));
            }
        }
    }

    private void mostrarEstadoFinal() {
        System.out.println("\n\n##################################################");
        System.out.println("###       ESTADO FINAL DA PAGINAÇÃO         ###");
        System.out.println("##################################################");

        mostrarEstadoAtual();

        System.out.printf("%nMolduras físicas: %d%n", paginacao.getNumMoldurasFisicas());
        System.out.printf("Páginas virtuais: %d%n", paginacao.getNumPaginasVirtuais());
        System.out.printf("Molduras livres..: %d%n", paginacao.getNumMoldurasLivres());
        System.out.printf("Molduras ocupadas: %d%n", paginacao.getNumMoldurasOcupadas());
        System.out.println("##################################################");
    }

    private long lerNumeroPositivo() {
        while (true) {
            try {
                long valor = Long.parseLong(scanner.next());
                if (valor <= 0) throw new NumberFormatException();
                return valor;
            } catch (NumberFormatException e) {
                System.err.print("Por favor, digite um número positivo válido: ");
            }
        }
    }

    private String formatarTamanho(long tamanhoEmBytes) {
        double convertido = (double) tamanhoEmBytes / unidadeEscolhida.getFatorParaBytes();
        return String.format("%.2f %s", convertido, unidadeEscolhida.name());
    }
}
