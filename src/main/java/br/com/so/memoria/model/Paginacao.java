package br.com.so.memoria.model;

import lombok.Data;

import java.util.*;

@Data
public class Paginacao {

    private final long tamanhoMemoFisica;
    private final long tamanhoMemVirtual;
    private final long tamanhoPagina;
    private final UnidadeArmazenamento unidadeArmazenamento;

    private final int numMoldurasFisicas;
    private final int numPaginasVirtuais;

    private final Map<Integer, Frame> tabelaMolduras = new HashMap<>();

    private final List<Processo> memoriaVirtual = new ArrayList<>();

    public Paginacao(long tamanhoFisicoBytes, long tamanhoVirtualBytes, long tamanhoPaginaBytes, UnidadeArmazenamento unidade) {
        this.tamanhoMemoFisica = tamanhoFisicoBytes;
        this.tamanhoMemVirtual = tamanhoVirtualBytes;
        this.tamanhoPagina = tamanhoPaginaBytes;
        this.unidadeArmazenamento = unidade;
        this.numMoldurasFisicas = (int) (tamanhoFisicoBytes / tamanhoPaginaBytes);
        this.numPaginasVirtuais = (int) (tamanhoVirtualBytes / tamanhoPaginaBytes);
    }

    public List<Integer> alocarPaginas(int numPaginas, Processo processo) {
        List<Integer> moldurasLivres = new ArrayList<>();
        for (int i = 0; i < numMoldurasFisicas && moldurasLivres.size() < numPaginas; i++) {
            if (!tabelaMolduras.containsKey(i)) {
                tabelaMolduras.put(i, new Frame(processo, moldurasLivres.size()));
                moldurasLivres.add(i);
            }
        }
        return moldurasLivres;
    }

    public void adicionarNaMemoriaVirtual(Processo processo) {
        memoriaVirtual.add(processo);
    }

    public int getNumMoldurasLivres() {
        return (int) (numMoldurasFisicas - tabelaMolduras.size());
    }

    public int getNumMoldurasOcupadas() {
        return tabelaMolduras.size();
    }

    @Data
    public static class Frame {
        private final Processo processo;
        private final int pagina;
    }
}
