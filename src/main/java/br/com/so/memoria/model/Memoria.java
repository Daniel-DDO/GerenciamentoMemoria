package br.com.so.memoria.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Memoria {
    private final long tamanhoTotal; 
    private final UnidadeArmazenamento unidade;
    private final List<Particao> particoes;
    private final List<Processo> memoriaSecundaria;

    public Memoria(long tamanhoTotal, UnidadeArmazenamento unidade) {
        this.tamanhoTotal = tamanhoTotal;
        this.unidade = unidade;
        this.particoes = new ArrayList<>();
        this.memoriaSecundaria = new ArrayList<>();
    }

    public boolean adicionarParticao(long tamanhoParticao) {
        long memoriaUsadaPelasParticoes = particoes.stream().mapToLong(Particao::getTamanho).sum();
        if (memoriaUsadaPelasParticoes + tamanhoParticao > this.tamanhoTotal) {
            return false;
        }
        int novoId = particoes.size() + 1;
        particoes.add(new Particao(novoId, tamanhoParticao, memoriaUsadaPelasParticoes));
        return true;
    }

    public long getMemoriaUtilizada() {
        return particoes.stream()
                .filter(p -> !p.isLivre())
                .mapToLong(p -> p.getProcessoAlocado().getTamanho())
                .sum();
    }
    
    public long getFragmentacaoInternaTotal() {
        return particoes.stream()
                .mapToLong(Particao::getFragmentacaoInterna)
                .sum();
    }

}