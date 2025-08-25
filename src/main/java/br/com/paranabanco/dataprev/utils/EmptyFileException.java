package br.com.paranabanco.dataprev.utils;

/**
 * Exceção que indica que um arquivo lido está vazio.
 */
public class EmptyFileException extends Exception {

    private static final long serialVersionUID = 1L;

    public EmptyFileException(String message) {
        super(message);
    }
}

