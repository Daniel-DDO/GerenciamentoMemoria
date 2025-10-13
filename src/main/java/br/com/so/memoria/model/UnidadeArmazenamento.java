package br.com.so.memoria.model;

import lombok.Getter;

@Getter
public enum UnidadeArmazenamento {
    PB("Petabyte"),
    TB("Terabyte"),
    GB("Gigabyte"),
    MB("Megabyte"),
    KB("Kilobyte"),
    B("Byte");

    private final String nome;

    UnidadeArmazenamento(String nome) {
        this.nome = nome;
    }

    @Override
    public String toString() {
        return nome;
    }
}
