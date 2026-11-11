package com.example.procurement;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RequestMapper {

  @Insert(
      "insert into purchase_requests (pr_no, raw_text, status, total) "
          + "values (#{prNo}, #{rawText}, #{status}, #{total})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insertRequest(PurchaseRequest request);

  @Update("update purchase_requests set pr_no = #{prNo} where id = #{id}")
  void updatePrNo(@Param("id") Long id, @Param("prNo") String prNo);

  @Select(
      "select id, pr_no, raw_text, status, total, created_at from purchase_requests "
          + "order by id desc")
  List<PurchaseRequest> findAllRequests();

  @Select(
      "select id, pr_no, raw_text, status, total, created_at from purchase_requests where id = #{id}")
  PurchaseRequest findRequestById(@Param("id") Long id);

  @Insert(
      "insert into request_items "
          + "(request_id, product_id, kind, brand, model, quantity, unit_price, supplier, total) "
          + "values (#{requestId}, #{productId}, #{kind}, #{brand}, #{model}, #{quantity}, "
          + "#{unitPrice}, #{supplier}, #{total})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insertItem(RequestItem item);

  @Select(
      "select id, request_id, product_id, kind, brand, model, quantity, unit_price, supplier, total "
          + "from request_items where request_id = #{requestId} order by id")
  List<RequestItem> findItemsByRequest(@Param("requestId") Long requestId);

  @Insert(
      "insert into purchase_orders (po_no, request_id, supplier, total, status) "
          + "values (#{poNo}, #{requestId}, #{supplier}, #{total}, #{status})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insertOrder(PurchaseOrder order);

  @Select(
      "select id, po_no, request_id, supplier, total, status, created_at from purchase_orders "
          + "where request_id = #{requestId} order by id desc limit 1")
  PurchaseOrder findOrderByRequest(@Param("requestId") Long requestId);

  @Insert(
      "insert into approvals (request_id, approver, action, note) "
          + "values (#{requestId}, #{approver}, #{action}, #{note})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insertApproval(Approval approval);

  @Select(
      "select id, request_id, approver, action, note, created_at from approvals "
          + "where request_id = #{requestId} order by id")
  List<Approval> findApprovalsByRequest(@Param("requestId") Long requestId);

  @Update("update purchase_requests set status = #{status} where id = #{id}")
  void updateStatus(@Param("id") Long id, @Param("status") String status);
}
