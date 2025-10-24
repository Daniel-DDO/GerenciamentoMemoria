package br.com.so.memoria.model;

public enum UnidadeArmazenamento {
    TB("Terabyte", 1024L * 1024 * 1024 * 1024),
    GB("Gigabyte", 1024L * 1024 * 1024),
    MB("Megabyte", 1024L * 1024),
    KB("Kilobyte", 1024L),
    B("Byte", 1L);

    private final String nome;
    private final long fatorParaBytes;

    UnidadeArmazenamento(String nome, long fatorParaBytes) {
        this.nome = nome;
        this.fatorParaBytes = fatorParaBytes;
    }

    public long getFatorParaBytes() {
        return fatorParaBytes;
    }

    @Override
    public String toString() {
        return nome;
    }
}
