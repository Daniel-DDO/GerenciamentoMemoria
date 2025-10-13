package br.com.so.memoria.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Paginacao {
    private double tamanhoMemoFisica;
    private double tamanhoMemVirtual;
    private UnidadeArmazenamento unidadeArmazenamento;

}
