package br.com.so.memoria.service;

import br.com.so.memoria.model.Paginacao;
import br.com.so.memoria.model.Processo;
import br.com.so.memoria.model.UnidadeArmazenamento;

import java.util.*;

public class AlocacaoPaginacao {

    private final Scanner scanner = new Scanner(System.in);
    private UnidadeArmazenamento unidadeEscolhida;
    private Paginacao paginacao;
    private final List<Processo> filaDeProcessos = new ArrayList<>();
    private final Random random = new Random();

    public void executar() {
        unidadeEscolhida = escolherUnidade();
        configurarMemoriaEPagina();
        configurarProcessos();
        executarSimulacao();
        mostrarEstadoFinal();
    }

    private UnidadeArmazenamento escolherUnidade() {
        System.out.println("\nEscolha a Unidade de Armazenamento");
        int i = 1;
        for (UnidadeArmazenamento u : UnidadeArmazenamento.values()) {
            System.out.println(i + ". " + u.name() + " (" + u + ")");
            i++;
        }
        int escolha = lerInteiroEntre(1, UnidadeArmazenamento.values().length);
        return UnidadeArmazenamento.values()[escolha - 1];
    }

    private void configurarMemoriaEPagina() {
        System.out.println("\nConfiguração de Memória / Páginas");
        System.out.print("Tamanho memória física (em " + unidadeEscolhida.name() + "): ");
        long fisico = lerLongPositivo();
        System.out.print("Tamanho memória virtual (>= física) (em " + unidadeEscolhida.name() + "): ");
        long virtual = lerLongPositivo();

        while (virtual < fisico) {
            System.err.println("Memória virtual deve ser maior ou igual à física. Digite novamente:");
            virtual = lerLongPositivo();
        }

        System.out.print("Tamanho da página/moldura (em " + unidadeEscolhida.name() + "): ");
        long pagina = lerLongPositivo();

        long fisicoBytes = fisico * unidadeEscolhida.getFatorParaBytes();
        long virtualBytes = virtual * unidadeEscolhida.getFatorParaBytes();
        long paginaBytes = pagina * unidadeEscolhida.getFatorParaBytes();

        if (paginaBytes > fisicoBytes) {
            System.err.println("A página não pode ser maior que a memória física. Ajuste os valores.");
            System.exit(1);
        }

        this.paginacao = new Paginacao(fisicoBytes, virtualBytes, paginaBytes, unidadeEscolhida);

        System.out.printf("Configurado: %d molduras físicas; %d páginas virtuais; página = %s%n",
                paginacao.getNumMoldurasFisicas(),
                paginacao.getNumPaginasVirtuais(),
                formatarBytes(paginaBytes));
    }

    private void configurarProcessos() {
        System.out.println("\n--- Definição de Processos (duração 'infinita' simulada) ---");
        while (true) {
            System.out.print("Nome do processo (ou 'fim' para encerrar): ");
            String nome = scanner.next();
            if (nome.equalsIgnoreCase("fim")) {
                if (filaDeProcessos.isEmpty()) {
                    System.err.println("Adicione ao menos um processo.");
                    continue;
                }
                break;
            }
            System.out.print("Tamanho do processo '" + nome + "' (em " + unidadeEscolhida.name() + "): ");
            long tam = lerLongPositivo();
            long tamBytes = tam * unidadeEscolhida.getFatorParaBytes();

            //processo não pode ter mais páginas do que a memória virtual suporta
            int paginasNecessarias = (int) Math.ceil((double) tamBytes / paginacao.getTamanhoPaginaBytes());
            if (paginasNecessarias > paginacao.getNumPaginasVirtuais()) {
                System.err.printf("Erro: o processo precisa de %d páginas, mas a memória virtual só comporta %d páginas.%n",
                        paginasNecessarias, paginacao.getNumPaginasVirtuais());
                continue;
            }

            filaDeProcessos.add(new Processo(nome, tamBytes));
        }
    }

    private void executarSimulacao() {
        System.out.println("\n--- Parâmetros da Simulação ---");
        System.out.println("Algoritmos disponíveis: 1. FIFO  2. LRU");
        System.out.print("Escolha algoritmo substituição (1 ou 2): ");
        int opAlg = lerInteiroEntre(1, 2);
        Paginacao.AlgoritmoSubstituicao algoritmo = (opAlg == 1) ? Paginacao.AlgoritmoSubstituicao.FIFO : Paginacao.AlgoritmoSubstituicao.LRU;

        System.out.println("Padrão de referência de páginas: 1. Ordem (0..P-1)  2. Aleatório");
        System.out.print("Escolha padrão (1 ou 2): ");
        int padrao = lerInteiroEntre(1, 2);

        System.out.print("Número de referências por processo (por quanto tempo simular, ex: 50): ");
        int referenciasPorProcesso = (int) lerLongPositivo();

        System.out.println("\n--- Iniciando simulação ---");
        paginacao.resetPageFaults();

        //pra cada processo, na ordem de criação (FIFO), executa a sequência de referências
        for (Processo p : filaDeProcessos) {
            int paginasDoProcesso = (int) Math.ceil((double) p.getTamanho() / paginacao.getTamanhoPaginaBytes());
            System.out.printf("%nProcesso %s (ID=%d) -> %d páginas. Simulando %d referências.%n",
                    p.getNome(), p.getId(), paginasDoProcesso, referenciasPorProcesso);

            List<Integer> padroesReferencia = gerarPadraoReferencias(paginasDoProcesso, referenciasPorProcesso, padrao == 2);
            int hits = 0, misses = 0;

            for (int referenciaPagina : padroesReferencia) {
                boolean hit = paginacao.referenciarPagina(p.getId(), referenciaPagina, algoritmo);
                if (hit) hits++; else misses++;
            }

            System.out.printf("Processo %s finalizou referências: hits=%d, misses=%d%n", p.getNome(), hits, misses);

            mostrarEstadoAtual();
        }

        System.out.printf("%nSimulação finalizada. Page faults totais (todos processos): %d%n", paginacao.getPageFaults());
    }

    private List<Integer> gerarPadraoReferencias(int numPaginas, int numReferencias, boolean aleatorio) {
        List<Integer> seq = new ArrayList<>(numReferencias);
        if (!aleatorio) {
            int i = 0;
            for (int k = 0; k < numReferencias; k++) {
                seq.add(i);
                i++;
                if (i >= numPaginas) i = 0;
            }
        } else {
            for (int k = 0; k < numReferencias; k++) {
                seq.add(random.nextInt(Math.max(1, numPaginas)));
            }
        }
        return seq;
    }

    private void mostrarEstadoAtual() {
        System.out.println("\n=== ESTADO ATUAL DA MEMÓRIA FÍSICA (MOLDURAS) ===");
        System.out.printf("%-8s | %-10s | %-10s | %-12s%n", "Frame", "Status", "ProcID", "Página");
        System.out.println("------------------------------------------------");
        int numFrames = paginacao.getNumMoldurasFisicas();
        for (int i = 0; i < numFrames; i++) {
            Paginacao.Frame f = paginacao.getTabelaMolduras().get(i);
            if (f == null) {
                System.out.printf("%-8d | %-10s | %-10s | %-12s%n", i, "Livre", "N/A", "N/A");
            } else {
                System.out.printf("%-8d | %-10s | %-10d | %-12d%n", i, "Ocupado", f.getProcessoId(), f.getPagina());
            }
        }

        if (!paginacao.getMemoriaVirtual().isEmpty()) {
            System.out.println("\n--- MEMÓRIA VIRTUAL (páginas em disco) ---");
            for (Paginacao.PageOnDisk pd : paginacao.getMemoriaVirtual()) {
                System.out.printf("ProcID=%d, Pag=%d%n", pd.getProcessoId(), pd.getPagina());
            }
        }

        // fragmentação interna: por processo (última página parcialmente usada)
        System.out.println("\n--- FRAGMENTAÇÃO INTERNA (estimada) ---");
        long totalFragInterna = 0;
        for (Processo p : filaDeProcessos) {
            int numPaginas = (int) Math.ceil((double) p.getTamanho() / paginacao.getTamanhoPaginaBytes());
            long sobraUltimaPaginaBytes = (numPaginas * paginacao.getTamanhoPaginaBytes()) - p.getTamanho();
            System.out.printf("ProcID=%d, Nome=%s -> fragmentação interna (última página): %s%n",
                    p.getId(), p.getNome(), formatarBytes(sobraUltimaPaginaBytes));
            totalFragInterna += sobraUltimaPaginaBytes;
        }
        System.out.println("Total fragmentação interna (soma das últimas páginas): " + formatarBytes(totalFragInterna));
    }

    private void mostrarEstadoFinal() {
        System.out.println("\n\n######################## ESTADO FINAL ########################");
        System.out.printf("Molduras físicas: %d | Molduras ocupadas: %d | Molduras livres: %d%n",
                paginacao.getNumMoldurasFisicas(), paginacao.getNumMoldurasOcupadas(), paginacao.getNumMoldurasLivres());
        System.out.printf("Page faults (total): %d%n", paginacao.getPageFaults());
        System.out.println("#############################################################");
        mostrarEstadoAtual();
    }

    // -------------------- utilitários de leitura --------------------

    private long lerLongPositivo() {
        while (true) {
            try {
                long v = Long.parseLong(scanner.next());
                if (v <= 0) throw new NumberFormatException();
                return v;
            } catch (NumberFormatException e) {
                System.err.print("Digite um número inteiro positivo: ");
            }
        }
    }

    private int lerInteiroEntre(int min, int max) {
        while (true) {
            try {
                int v = Integer.parseInt(scanner.next());
                if (v < min || v > max) throw new NumberFormatException();
                return v;
            } catch (NumberFormatException e) {
                System.err.printf("Digite um número entre %d e %d: ", min, max);
            }
        }
    }

    private String formatarBytes(long bytes) {
        double conv = (double) bytes / unidadeEscolhida.getFatorParaBytes();
        return String.format("%.2f %s", conv, unidadeEscolhida.name());
    }
}
