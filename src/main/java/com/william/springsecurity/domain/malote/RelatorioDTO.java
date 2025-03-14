package com.william.springsecurity.domain.malote;
import java.util.List;

public class RelatorioDTO {
    private String imagem; // imagem em formato Base64
    private String cabecalho1;
    private String cabecalho2;
    private String cabecalho3;
    private List<Registro> registros;

    public String getImagem() {
        return imagem;
    }
    public void setImagem(String imagem) {
        this.imagem = imagem;
    }
    public String getCabecalho1() {
        return cabecalho1;
    }
    public void setCabecalho1(String cabecalho1) {
        this.cabecalho1 = cabecalho1;
    }
    public String getCabecalho2() {
        return cabecalho2;
    }
    public void setCabecalho2(String cabecalho2) {
        this.cabecalho2 = cabecalho2;
    }
    public String getCabecalho3() {
        return cabecalho3;
    }
    public void setCabecalho3(String cabecalho3) {
        this.cabecalho3 = cabecalho3;
    }
    public List<Registro> getRegistros() {
        return registros;
    }
    public void setRegistros(List<Registro> registros) {
        this.registros = registros;
    }
 
}
