package cn.resumecraft.mapper;

import cn.resumecraft.model.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
  @Select("SELECT * FROM orders WHERE id = #{id} FOR UPDATE")
  Order lock(Long id);
}
