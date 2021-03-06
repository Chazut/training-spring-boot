package com.ecommerce.microcommerce.web.controller;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.exceptions.ProduitGratuitException;
import com.ecommerce.microcommerce.web.exceptions.ProduitIntrouvableException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Api( description="API pour es opérations CRUD sur les produits.")

@RestController
public class ProductController {

    @Autowired
    private ProductDao productDao;

    //Récupérer la liste des produits
    @ApiOperation(value = "Récupérer la liste des produits")
    @RequestMapping(value = "/Produits", method = RequestMethod.GET)
    public MappingJacksonValue listeProduits() {
        Iterable<Product> produits = productDao.findAll();
        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");
        FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);
        MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);
        produitsFiltres.setFilters(listDeNosFiltres);
        return produitsFiltres;
    }

    @ApiOperation(value = "Récupérer la liste triée des produits")
    @GetMapping(value = "Produits/Trier")
    public List<Product>  trierProduitsParOrdreAlphabetique() {
        return productDao.trierProduitsParOrdreAlphabetique();
    }

    @ApiOperation(value = "Récuperer la liste des produits avec marge calculées")
    @RequestMapping(value = "/AdminProduits", method = RequestMethod.GET)
    public ArrayList<String> calculerMargeProduit(){
        Iterable<Product> produits = productDao.findAll();
        ArrayList<String>  produitsWithMarge = new ArrayList<String>();
        for(Product p : produits){
            produitsWithMarge.add(p.toString() + ":" + (p.getPrix() - p.getPrixAchat()));
        }
        return produitsWithMarge;
    }

    //Récupérer un produit par son Id
    @ApiOperation(value = "Récupère un produit grâce à son ID à condition que celui-ci soit en stock!")
    @GetMapping(value = "/Produits/{id}")
    public Product afficherUnProduit(@PathVariable int id) throws ProduitGratuitException {
        Product produit = productDao.findById(id);
        if(produit==null) throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est INTROUVABLE. Écran Bleu si je pouvais.");
        if(produit.getPrix() == 1) throw new ProduitGratuitException();
        return produit;
    }

    //ajouter un produit
    @ApiOperation(value = "Ajouter un produit")
    @PostMapping(value = "/Produits")
    public ResponseEntity<Void> ajouterProduit(@Valid @RequestParam Map<String, String> product) {
        Product productAdded =  productDao.save(new Product(Integer.parseInt(product.get("id")), product.get("nom"), Integer.parseInt(product.get("prix")), Integer.parseInt(product.get("prixAchat"))));
        if (productAdded == null)
            return ResponseEntity.noContent().build();
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productAdded.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @ApiOperation(value = "Supprimer un produit")
    @DeleteMapping (value = "/Produits/{id}")
    public void supprimerProduit(@PathVariable int id) {
        productDao.delete(id);
    }

    @ApiOperation(value = "Modifier un produit")
    @PutMapping (value = "/Produits", produces = {"application/json", "application/xml"}, consumes = {"application/x-www-form-urlencoded"})
    public void updateProduit(@RequestParam Map<String, String> product) {
        productDao.save(new Product(Integer.parseInt(product.get("id")), product.get("nom"), Integer.parseInt(product.get("prix")), Integer.parseInt(product.get("prixAchat"))));
    }

    //Pour les tests
    @ApiOperation(value = "Pour les tests")
    @GetMapping(value = "test/produits/{prix}")
    public List<Product>  testeDeRequetes(@PathVariable int prix) {
        return productDao.chercherUnProduitCher(prix);
    }
}
