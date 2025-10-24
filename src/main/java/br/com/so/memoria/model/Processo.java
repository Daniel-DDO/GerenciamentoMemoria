package br.com.so.memoria.model;

public class Processo {
    private static int proximoId = 1;
    private int id;
    private String nome;
    private long tamanho; 

    public Processo(String nome, long tamanho) {
        this.id = proximoId++;
        this.nome = nome;
        this.tamanho = tamanho;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public long getTamanho() {
        return tamanho;
    }

    @Override
    public String toString() {
        return "ID=" + id + ", Nome='" + nome + '\'' + ", Tamanho=" + tamanho + " Bytes";
    }
}