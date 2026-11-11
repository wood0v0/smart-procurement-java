package com.example.procurement;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CatalogMapper {

  @Select(
      "select id, kind, brand, model, specs, active from products where active = true "
          + "order by kind, brand, model")
  List<Product> findAllProducts();

  @Select(
      "select id, kind, brand, model, specs, active from products "
          + "where active = true and kind = #{kind} order by brand, model")
  List<Product> findProductsByKind(@Param("kind") String kind);

  @Select(
      "select id, name, rating, shipping_fee, delivery_days from suppliers order by name")
  List<Supplier> findAllSuppliers();

  @Select(
      """
      select q.product_id, p.kind, p.brand, p.model,
             s.name as supplier, s.rating, s.shipping_fee,
             q.unit_price, q.stock, q.delivery_days, q.source, q.valid_until
      from quotes q
      join products p on p.id = q.product_id
      join suppliers s on s.id = q.supplier_id
      where p.kind = #{kind}
        and p.active = true
        and (q.valid_until is null or q.valid_until >= current_date)
      order by q.unit_price asc
      """)
  List<QuoteView> findValidQuotesByKind(@Param("kind") String kind);
}
