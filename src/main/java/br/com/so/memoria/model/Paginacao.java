package br.com.so.memoria.model;

import lombok.Data;

import java.util.*;

@Data
public class Paginacao {

    public enum AlgoritmoSubstituicao { FIFO, LRU }

    private final long tamanhoFisicaBytes;
    private final long tamanhoVirtualBytes;
    private final long tamanhoPaginaBytes;
    private final UnidadeArmazenamento unidade;

    private final int numMoldurasFisicas;
    private final int numPaginasVirtuais;

    private final Map<Integer, Frame> tabelaMolduras = new HashMap<>();
    private final Map<Integer, Set<Integer>> pageTable = new HashMap<>();
    private final List<PageOnDisk> memoriaVirtual = new ArrayList<>(); //páginas que estão na memória virt
    private final Deque<Integer> filaFIFO = new ArrayDeque<>(); //contém frame indexes na ordem de inserção

    private long pageFaults = 0;

    public Paginacao(long tamanhoFisicaBytes, long tamanhoVirtualBytes, long tamanhoPaginaBytes, UnidadeArmazenamento unidade) {
        if (tamanhoVirtualBytes < tamanhoFisicaBytes) {
            throw new IllegalArgumentException("Memória virtual deve ser maior ou igual à física.");
        }
        if (tamanhoPaginaBytes <= 0) {
            throw new IllegalArgumentException("Tamanho de página inválido.");
        }
        this.tamanhoFisicaBytes = tamanhoFisicaBytes;
        this.tamanhoVirtualBytes = tamanhoVirtualBytes;
        this.tamanhoPaginaBytes = tamanhoPaginaBytes;
        this.unidade = unidade;

        this.numMoldurasFisicas = (int) (tamanhoFisicaBytes / tamanhoPaginaBytes);
        this.numPaginasVirtuais = (int) (tamanhoVirtualBytes / tamanhoPaginaBytes);
    }

    @Data
    public static class Frame {
        private int frameIndex;
        private final int processoId;
        private final int pagina; //número da página do processo
        private long cargaTimestamp; //quando foi carregado (pra FIFO)
        private long lastAccessTimestamp; //pra LRU

        public Frame(int frameIndex, int processoId, int pagina) {
            this.frameIndex = frameIndex;
            this.processoId = processoId;
            this.pagina = pagina;
            this.cargaTimestamp = System.currentTimeMillis();
            this.lastAccessTimestamp = cargaTimestamp;
        }

    }

    @Data
    public static class PageOnDisk {
        private final int processoId;
        private final int pagina;
    }

    public boolean estaResidente(int processoId, int pagina) {
        Set<Integer> conj = pageTable.get(processoId);
        return conj != null && conj.contains(pagina);
    }

    public boolean referenciarPagina(int processoId, int pagina, AlgoritmoSubstituicao algoritmo) {
        if (estaResidente(processoId, pagina)) {
            Frame f = encontrarFrame(processoId, pagina);
            if (f != null) {
                f.setLastAccessTimestamp(System.currentTimeMillis());
            }
            return true;
        } else {
            pageFaults++;
            //tenta alocar frame livre
            Integer frameLivre = encontrarFrameLivre();
            if (frameLivre != null) {
                alocarEmFrame(frameLivre, processoId, pagina);
            } else {
                //substituir um frame
                int victim = escolherVitima(algoritmo);
                substituirFrame(victim, processoId, pagina);
            }
            return false;
        }
    }

    private Integer encontrarFrameLivre() {
        for (int i = 0; i < numMoldurasFisicas; i++) {
            if (!tabelaMolduras.containsKey(i)) return i;
        }
        return null;
    }

    private void alocarEmFrame(int frameIndex, int processoId, int pagina) {
        Frame f = new Frame(frameIndex, processoId, pagina);
        tabelaMolduras.put(frameIndex, f);
        pageTable.computeIfAbsent(processoId, k -> new HashSet<>()).add(pagina);
        filaFIFO.addLast(frameIndex); //pra FIFO
    }

    private void substituirFrame(int victimFrameIndex, int novoProcId, int novaPagina) {
        Frame victim = tabelaMolduras.get(victimFrameIndex);
        if (victim != null) {
            //registra na memória virtual
            memoriaVirtual.add(new PageOnDisk(victim.getProcessoId(), victim.getPagina()));

            //remove da page table do processo anterior
            Set<Integer> set = pageTable.get(victim.getProcessoId());
            if (set != null) set.remove(victim.getPagina());
        }

        //carrega novo
        Frame novo = new Frame(victimFrameIndex, novoProcId, novaPagina);
        tabelaMolduras.put(victimFrameIndex, novo);
        pageTable.computeIfAbsent(novoProcId, k -> new HashSet<>()).add(novaPagina);

        filaFIFO.remove(victimFrameIndex);
        filaFIFO.addLast(victimFrameIndex);
    }

    private int escolherVitima(AlgoritmoSubstituicao algoritmo) {
        if (algoritmo == AlgoritmoSubstituicao.FIFO) {
            //FIFO: frame no topo da fila
            Integer victim = filaFIFO.peekFirst();
            if (victim == null) throw new IllegalStateException("FIFO: fila vazia ao escolher vítima.");
            return victim;
        } else { //LRU
            long maisAntigo = Long.MAX_VALUE;
            int victimIdx = -1;
            for (Map.Entry<Integer, Frame> e : tabelaMolduras.entrySet()) {
                Frame f = e.getValue();
                if (f.getLastAccessTimestamp() < maisAntigo) {
                    maisAntigo = f.getLastAccessTimestamp();
                    victimIdx = e.getKey();
                }
            }
            if (victimIdx == -1) throw new IllegalStateException("LRU: não encontrou vítima.");
            return victimIdx;
        }
    }

    private Frame encontrarFrame(int processoId, int pagina) {
        for (Frame f : tabelaMolduras.values()) {
            if (f.getProcessoId() == processoId && f.getPagina() == pagina) return f;
        }
        return null;
    }

    public int getNumMoldurasLivres() {
        return numMoldurasFisicas - tabelaMolduras.size();
    }

    public int getNumMoldurasOcupadas() {
        return tabelaMolduras.size();
    }

    public void resetPageFaults() {
        this.pageFaults = 0;
    }
}
