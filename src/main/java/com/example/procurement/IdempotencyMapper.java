package com.example.procurement;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface IdempotencyMapper {

  @Select("select request_id from idempotency_keys where key_value = #{key}")
  Long findRequestId(@Param("key") String key);

  @Insert(
      "insert into idempotency_keys (key_value, request_id, action) "
          + "values (#{key}, #{requestId}, #{action})")
  void insert(
      @Param("key") String key,
      @Param("requestId") Long requestId,
      @Param("action") String action);
}
