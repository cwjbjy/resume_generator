package cn.resumecraft.mapper;

import cn.resumecraft.entity.Resume;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ResumeMapper extends BaseMapper<Resume> {
  @Select(
      """
      SELECT id, template_id, title, version, updated_at,
             JSON_EXTRACT(content, '$.basic') AS content
      FROM resumes
      WHERE user_id = #{userId}
      ORDER BY updated_at DESC, id DESC
      """)
  IPage<Resume> selectSummaries(Page<Resume> page, @Param("userId") Long userId);
}
