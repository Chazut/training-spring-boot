package com.ecommerce.microcommerce.web.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UPGRADE_REQUIRED)
public class ProduitGratuitException extends Exception {

    public ProduitGratuitException(){
        super("Le produit est gratuit !! ou à 1€ symbolique :)");
    }

}
