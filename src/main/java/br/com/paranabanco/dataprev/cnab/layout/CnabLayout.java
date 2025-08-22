package br.com.paranabanco.dataprev.cnab.layout;

import java.util.List;

/**
 * Generic contract for CNAB layouts. Implementations return the list of
 * formatted lines that compose the file for the given items.
 */
public interface CnabLayout<T> {
    List<String> build(List<T> items);
}
