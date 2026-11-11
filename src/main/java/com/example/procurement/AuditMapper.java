package com.example.procurement;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuditMapper {

  @Insert(
      "insert into audit_logs (request_id, action, actor, detail) "
          + "values (#{requestId}, #{action}, #{actor}, #{detail})")
  void insert(
      @Param("requestId") Long requestId,
      @Param("action") String action,
      @Param("actor") String actor,
      @Param("detail") String detail);

  @Select(
      "select id, request_id, action, actor, detail, created_at from audit_logs "
          + "where request_id = #{requestId} order by id")
  List<AuditLog> findByRequest(@Param("requestId") Long requestId);
}
