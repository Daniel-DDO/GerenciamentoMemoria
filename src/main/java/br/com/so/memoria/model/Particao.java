package br.com.so.memoria.model;

public class Particao {
    private int id;
    private long tamanho; 
    private long enderecoInicio; 
    private Processo processoAlocado;
    private long fragmentacaoInterna; 

    public Particao(int id, long tamanho, long enderecoInicio) {
        this.id = id;
        this.tamanho = tamanho;
        this.enderecoInicio = enderecoInicio;
        this.processoAlocado = null;
        this.fragmentacaoInterna = 0;
    }

    public boolean isLivre() {
        return processoAlocado == null;
    }

    public void alocar(Processo processo) {
        if (processo.getTamanho() <= this.tamanho) {
            this.processoAlocado = processo;
            this.fragmentacaoInterna = this.tamanho - processo.getTamanho();
        }
    }

    public Processo liberar() {
        Processo processoRemovido = this.processoAlocado;
        this.processoAlocado = null;
        this.fragmentacaoInterna = 0;
        return processoRemovido;
    }
    
    // --- Getters ---
    public int getId() { return id; }
    public long getTamanho() { return tamanho; }
    public long getEnderecoInicio() { return enderecoInicio; }
    public Processo getProcessoAlocado() { return processoAlocado; }
    public long getFragmentacaoInterna() { return fragmentacaoInterna; }

    @Override
    public String toString() {
        return "Particao{" +
                "id=" + id +
                ", tamanho=" + tamanho + " Bytes" +
                ", livre=" + isLivre() +
                ", processoAlocado=" + (processoAlocado != null ? processoAlocado.getId() : "N/A") +
                ", fragmentacaoInterna=" + fragmentacaoInterna + " Bytes" +
                '}';
    }
}