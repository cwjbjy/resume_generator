package cn.resumecraft.mapper;

import cn.resumecraft.model.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
  @Select("SELECT * FROM users WHERE id = #{id} FOR UPDATE")
  User lock(Long id);
}
