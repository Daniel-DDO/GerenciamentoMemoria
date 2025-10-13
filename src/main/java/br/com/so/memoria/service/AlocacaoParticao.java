package br.com.so.memoria.service;

import br.com.so.memoria.model.Particao;
import br.com.so.memoria.model.UnidadeArmazenamento;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Getter
@Setter
public class AlocacaoParticao {

    private int quantParticoes;
    private UnidadeArmazenamento unidadeArmazenamento;
    private double tamanhoMemoFisica;
    private List<Particao> particoes = new ArrayList<>();

    private static AlocacaoParticao alocacaoParticao;

    public static AlocacaoParticao getAlocacaoParticao() {
        if (alocacaoParticao == null) {
            alocacaoParticao = new AlocacaoParticao();
        }
        return alocacaoParticao;
    }

    Scanner scanner = new Scanner(System.in);

    private void selecionarUnidade() {
        int opcao;
        do {
            System.out.println("1. PB\n2. TB\n3. GB\n4. MB\n5. KB\n6. B");
            opcao = scanner.nextInt();

            if (opcao == 1) {
                this.unidadeArmazenamento = UnidadeArmazenamento.PB;
            } else if (opcao == 2) {
                this.unidadeArmazenamento = UnidadeArmazenamento.TB;
            } else if (opcao == 3) {
                this.unidadeArmazenamento = UnidadeArmazenamento.GB;
            } else if (opcao == 4) {
                this.unidadeArmazenamento = UnidadeArmazenamento.MB;
            } else if (opcao == 5) {
                this.unidadeArmazenamento = UnidadeArmazenamento.KB;
            } else if (opcao == 6) {
                this.unidadeArmazenamento = UnidadeArmazenamento.B;
            } else {
                System.err.println("Erro, selecione novamente.");
            }
        } while (opcao < 1 || opcao > 6);
    }

    private void definirQuantParticoes(UnidadeArmazenamento unidade) {
        int opcao;
        double tamParticaoInd;
        do {
            System.out.println("\nDefina a quantidade de partições: ");
            this.quantParticoes = scanner.nextInt();
            while (this.quantParticoes < 1) {
                System.err.println("Erro. Digite a quantidade de partições: ");
                this.quantParticoes = scanner.nextInt();
            }

            tamParticaoInd = (this.tamanhoMemoFisica / this.quantParticoes);

            System.out.println("Você terá "+this.quantParticoes+" partições, cada uma com o tamanho de "+tamParticaoInd+" "+unidade+"s. "+
                "Se deseja confirmar, digite 1. Caso deseje alterar, digite qualquer número.");
            opcao = scanner.nextInt();
        } while (opcao != 1);

        for (int i = 0; i < this.quantParticoes; i++) {
            Particao particao = new Particao();
            particao.setTamanhoMemoFisica(tamParticaoInd);
            particao.setUnidadeArmazenamento(unidade);

            particoes.add(particao);
        }

        System.out.println("Foram criadas "+particoes.size()+" partições de "+tamParticaoInd+" "+unidade+"s. ");
    }

    private void definirTamMemoria() {
        double tamanho;
        int opcao;

        do {
            tamanho = scanner.nextDouble();
            System.out.println("O tamanho total de memória física será de "+tamanho+" "+this.unidadeArmazenamento+"s. "+
                    "Se deseja confirmar, digite 1. Caso deseje alterar, digite qualquer número.");
            opcao = scanner.nextInt();
            if (opcao != 1) {
                System.out.println("Digite a quantidade de memória física em "+this.unidadeArmazenamento+"s: ");
            } else {
                this.tamanhoMemoFisica = tamanho;
            }
        } while (opcao != 1);
    }

    public void executarAlocacaoParticao() {
        System.out.println("Selecione a unidade de medida que será usada: ");
        selecionarUnidade();
        System.out.println("Digite a quantidade de memória física em "+this.unidadeArmazenamento+"s: ");
        definirTamMemoria();
        definirQuantParticoes(this.unidadeArmazenamento);
        System.out.println();
    }
}
