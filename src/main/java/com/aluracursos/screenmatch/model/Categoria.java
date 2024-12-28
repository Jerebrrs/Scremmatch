package com.aluracursos.screenmatch.model;

import org.jetbrains.annotations.NotNull;

public enum Categoria {
    ACCION("Action","Acción"),
    ROMANCE("Romance","Romance"),
    COMEDIA("Comedy","Comedia"),
    CRIMEN("Crime","Crimen"),
    DRAMA("Drama","Drama");


    private String categoiaOmdb;
    private String categoriaEspañol;

    Categoria(String categoiaOmdb,String categoriaEspañol){
        this.categoiaOmdb = categoiaOmdb;
        this.categoriaEspañol= categoriaEspañol;
    }

    @NotNull
    public static Categoria fromString(String text){
        for (Categoria categoria: Categoria.values()){
            if (categoria.categoiaOmdb.equalsIgnoreCase(text)){
                return categoria;
            }
        }
        throw new IllegalArgumentException("Ninguna categoria encontrada: "+ text);
    }

    @NotNull
    public static Categoria fromEspañol(String text){
        for (Categoria categoria: Categoria.values()){
            if (categoria.categoriaEspañol.equalsIgnoreCase(text)){
                return categoria;
            }
        }
        throw new IllegalArgumentException("Ninguna categoria encontrada: "+ text);
    }


}
