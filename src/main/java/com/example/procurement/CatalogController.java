package com.example.procurement;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {

  private final CatalogMapper catalog;

  public CatalogController(CatalogMapper catalog) {
    this.catalog = catalog;
  }

  @GetMapping("/products")
  public List<Product> products(@RequestParam(required = false) String kind) {
    return kind == null || kind.isBlank()
        ? catalog.findAllProducts()
        : catalog.findProductsByKind(kind);
  }

  @GetMapping("/suppliers")
  public List<Supplier> suppliers() {
    return catalog.findAllSuppliers();
  }
}
