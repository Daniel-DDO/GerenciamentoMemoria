package br.com.so.memoria.service;

import br.com.so.memoria.model.Memoria;
import br.com.so.memoria.model.Particao;
import br.com.so.memoria.model.Processo;
import br.com.so.memoria.model.UnidadeArmazenamento;

import java.util.*;

public class AlocacaoParticao {

    private final Scanner scanner = new Scanner(System.in);
    private Memoria memoria;
    private UnidadeArmazenamento unidadeEscolhida;
    private final List<Processo> filaDeProcessos = new ArrayList<>();

    public void executar() {
        unidadeEscolhida = escolherUnidade();
        configurarMemoria();
        configurarProcessos();
        executarAlgoritmoDeAlocacao();
        mostrarEstadoFinalMemoria();
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

    private void configurarMemoria() {
        System.out.print("\nDigite o tamanho máximo da memória física (em " + unidadeEscolhida.name() + "): ");
        while (!scanner.hasNextLong()) {
                System.err.println("Por favor, digite um número válido.");
                scanner.next(); // descarta a entrada inválida
        }

        long tamanhoUsuario = scanner.nextLong();
        long tamanhoTotalEmBytes = tamanhoUsuario * unidadeEscolhida.getFatorParaBytes();
        this.memoria = new Memoria(tamanhoTotalEmBytes, unidadeEscolhida);

        System.out.println("\nDefinição das Partições Fixas");

        long memoriaRestanteBytes = tamanhoTotalEmBytes;

        while (true && (memoriaRestanteBytes > 0)) {

            System.out.print("Digite o tamanho da próxima partição (em " + unidadeEscolhida.name() + ") ou 0 para finalizar: ");
            while (!scanner.hasNextLong()) {
                System.err.println("Por favor, digite um número válido.");
                scanner.next(); // descarta a entrada inválida

            }
            long tamanhoParticaoUsuario = scanner.nextLong();

            if (tamanhoParticaoUsuario == 0) {
                break;
            }

            long tamanhoParticaoBytes = tamanhoParticaoUsuario * unidadeEscolhida.getFatorParaBytes();

            if (tamanhoParticaoBytes > memoriaRestanteBytes) {
                double memoriaRestanteUsuario = (double) memoriaRestanteBytes / unidadeEscolhida.getFatorParaBytes();
                System.err.printf("Erro: Tamanho da partição excede a memória restante (%.2f %s).\n", memoriaRestanteUsuario, unidadeEscolhida.name());
            } else {
                memoria.adicionarParticao(tamanhoParticaoBytes);
                memoriaRestanteBytes -= tamanhoParticaoBytes;
                double memoriaRestanteUsuario = (double) memoriaRestanteBytes / unidadeEscolhida.getFatorParaBytes();
                System.out.printf("Partição de %d %s criada. Restam %.2f %s.\n", tamanhoParticaoUsuario, unidadeEscolhida.name(), memoriaRestanteUsuario, unidadeEscolhida.name());
            }
        }
    }

    private void configurarProcessos() {
        System.out.println("\n--- Definição da Fila de Processos ---");

        while (true) {
            System.out.print("Digite o nome do processo (ou 'fim' para encerrar): ");
            String nome = scanner.next();

            if (nome.equalsIgnoreCase("fim")) {
                if(filaDeProcessos.isEmpty()) {
                    System.err.println("A fila de processos não pode estar vazia. Adicione pelo menos um processo.");
                    continue;
                }
                else break;
            }

            System.out.print("Digite o tamanho do processo '" + nome + "' (em " + unidadeEscolhida.name() + "): ");

            while (!scanner.hasNextLong()) {
                System.err.println("Por favor, digite um número válido.");
                scanner.next(); // descarta a entrada inválida

            }

            long tamanhoUsuario = scanner.nextLong();
            long tamanhoEmBytes = tamanhoUsuario * unidadeEscolhida.getFatorParaBytes();

            if (tamanhoEmBytes > memoria.getTamanhoTotal()){
                System.err.println("Erro: Tamanho do processo excede o tamanho total da memória.");
                continue;
            }

            else if(!analisarSuporteAoProcesso (tamanhoEmBytes)) {
                 System.err.println("Erro: O tamanho do processo excede o tamanho da maior partição da memória.");
                continue;
            }
            filaDeProcessos.add(new Processo(nome, tamanhoEmBytes));
        }
    }

    private boolean analisarSuporteAoProcesso (long tamanho){

        for (Particao particao : memoria.getParticoes()){
            if (particao.getTamanho() >= tamanho) return true;
        }
        return false;
    }

    private String formatarTamanho(long tamanhoEmBytes) {
        double tamanhoConvertido = (double) tamanhoEmBytes / unidadeEscolhida.getFatorParaBytes();
        return String.format("%.2f %s", tamanhoConvertido, unidadeEscolhida.name());
    }

    private void executarAlgoritmoDeAlocacao() {

        System.out.println("\n--- Escolha o Algoritmo de Alocação ---");
        System.out.println("1. First-Fit");
        System.out.println("2. Best-Fit");
        System.out.print("Opção: ");
        int opcao = scanner.nextInt();

        for (Processo processo : filaDeProcessos) {
            System.out.println("\n--------------------------------------------------");
            System.out.println("Tentando alocar o processo: ID=" + processo.getId() + ", Nome=" + processo.getNome() + ", Tamanho=" + formatarTamanho(processo.getTamanho()));

            boolean alocado = false;
            switch (opcao) {
                case 1 -> alocado = firstFit(processo);
                case 2 -> alocado = bestFit(processo);
                default -> {
                    System.err.println("Opção inválida, usando First-Fit por padrão.");
                    alocado = firstFit(processo);
                }
            }

            if (!alocado) {
                System.out.println("Não há partição livre que comporte o processo " + processo.getId() + ".");

                    if (realizarSwapping(processo)) {
                        System.out.println("Swapping realizado com sucesso. Tentando alocar novamente...");

                        switch (opcao) {
                            case 1 -> firstFit(processo);
                            case 2 -> bestFit(processo);
                        }

                    } else {
                        System.err.println("Falha no swapping.");
                    }
            }
            mostrarEstadoAtualMemoria();
        }

    }

    private boolean firstFit(Processo processo) {

        for (Particao p : memoria.getParticoes()) {
            if (p.isLivre() && p.getTamanho() >= processo.getTamanho()) {
                p.alocar(processo);
                System.out.println("Processo " + processo.getId() + " alocado na Partição " + p.getId());
                return true;
            }
        }
        return false;
    }

    private boolean bestFit(Processo processo) {

        Particao melhorParticao = null;

        long menorDiferenca = Long.MAX_VALUE;

        for (Particao p : memoria.getParticoes()) {
            if (p.isLivre() && p.getTamanho() >= processo.getTamanho()) {
                long diferenca = p.getTamanho() - processo.getTamanho();
                if (diferenca < menorDiferenca) {
                    menorDiferenca = diferenca;
                    melhorParticao = p;
                }
            }
        }

        if (melhorParticao != null) {
            melhorParticao.alocar(processo);
            System.out.println("Processo " + processo.getId() + " alocado na Partição " + melhorParticao.getId());
            return true;
        }
        return false;
    }

   private boolean realizarSwapping(Processo processoNecessitandoEspaco) {
        //encontra candidatos (partições ocupadas que são grandes o suficiente)
        List<Particao> candidatasParaSwap = new ArrayList<>();
        for (Particao particao : memoria.getParticoes()) {
            if (!particao.isLivre() && particao.getTamanho() >= processoNecessitandoEspaco.getTamanho()) {
                candidatasParaSwap.add(particao);
            }
        }

        //Escolhe aleatoriamente um candidato da lista qualificada
        Random rand = new Random();
        Particao particaoParaSwap = candidatasParaSwap.get(rand.nextInt(candidatasParaSwap.size()));
        
        //realiza o swap
        Processo processoRemovido = particaoParaSwap.liberar();
        memoria.getMemoriaSecundaria().add(processoRemovido);
        System.out.println("SWAPPING: Processo " + processoRemovido.getId() + " (" + processoRemovido.getNome() +
                ") foi movido da Partição " + particaoParaSwap.getId() + " para a memória secundária para liberar espaço.");
        
        return true;
    }

    private void mostrarEstadoAtualMemoria() {
        System.out.println("\n=== MAPA DE MEMÓRIA ATUAL ===");
        System.out.printf("%-5s | %-15s | %-10s | %-12s | %-10s | %-20s%n",

                "ID", "Tamanho", "Status", "ID Processo", "Nome Proc.", "Fragmentação Interna");

        System.out.println("------+-----------------+------------+--------------+------------+-----------------------");

        for (Particao p : memoria.getParticoes()) {
            String status = p.isLivre() ? "Livre" : "Ocupado";
            String procId = p.isLivre() ? "N/A" : String.valueOf(p.getProcessoAlocado().getId());
            String procNome = p.isLivre() ? "N/A" : p.getProcessoAlocado().getNome();
            System.out.printf("%-5d | %-15s | %-10s | %-12s | %-10s | %-20s%n",
                    p.getId(), formatarTamanho(p.getTamanho()), status, procId, procNome, formatarTamanho(p.getFragmentacaoInterna()));
        }
        System.out.println("========================================================================================");
    }

    private void mostrarEstadoFinalMemoria() {
        System.out.println("\n\n##################################################");
        System.out.println("###         ESTADO FINAL DA MEMÓRIA          ###");
        System.out.println("##################################################");

        mostrarEstadoAtualMemoria();

        long memoriaUtilizada = memoria.getMemoriaUtilizada();
        long fragInternaTotal = memoria.getFragmentacaoInternaTotal();
        long totalAlocado = memoriaUtilizada + fragInternaTotal;
        long totalLivre = memoria.getTamanhoTotal() - totalAlocado;

        System.out.println("\n--- ESTATÍSTICAS ---");

        System.out.println("Memória Total..................: " + formatarTamanho(memoria.getTamanhoTotal()));
        System.out.println("Memória Utilizada (Processos)..: " + formatarTamanho(memoriaUtilizada));
        System.out.println("Fragmentação Interna Total.....: " + formatarTamanho(fragInternaTotal));
        System.out.println("Total Alocado (Proc + Frag)....: " + formatarTamanho(totalAlocado));
        System.out.println("Total Livre....................: " + formatarTamanho(totalLivre));

        long numParticoesLivre = memoria.getParticoes().stream().filter(Particao::isLivre).count();

        System.out.println("Fragmentação Externa...........: " + formatarTamanho(totalLivre) + " (em " + numParticoesLivre + " partições)");

        if (!memoria.getMemoriaSecundaria().isEmpty()) {
            System.out.println("\n--- PROCESSOS EM MEMÓRIA SECUNDÁRIA (SWAPPING) ---");
            for (Processo p : memoria.getMemoriaSecundaria()) {
                System.out.println("- ID=" + p.getId() + ", Nome=" + p.getNome() + ", Tamanho=" + formatarTamanho(p.getTamanho()));
            }
        }
        System.out.println("\n##################################################");
    }
}