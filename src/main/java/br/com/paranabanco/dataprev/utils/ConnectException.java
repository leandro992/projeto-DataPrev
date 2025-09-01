package br.com.paranabanco.dataprev.utils;


/**
 * Exceção para indicar falhas não recuperáveis durante a interação com o CONNECT.
 */
public class ConnectException extends RuntimeException {
    public ConnectException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectException(String message) {
        super(message);
    }
}
