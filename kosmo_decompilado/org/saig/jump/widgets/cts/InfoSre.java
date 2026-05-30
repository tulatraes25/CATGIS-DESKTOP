/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.cts;

public class InfoSre {
    private String nombre;
    private String favorito;
    private String codigo;

    public String getFavorito() {
        return this.favorito;
    }

    public void setFavorito(String favorito) {
        this.favorito = favorito;
    }

    public String getNombre() {
        return this.nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return this.codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public boolean esFavorito() {
        return this.favorito != null && this.favorito.equals("true");
    }

    public String toString() {
        return this.nombre;
    }
}

