package cn.resumecraft.service;

import static cn.resumecraft.api.BizException.require;

import cn.resumecraft.mapper.TemplateMapper;
import cn.resumecraft.mapper.UserMapper;
import cn.resumecraft.model.Template;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TemplateService {
  private final TemplateMapper templateMapper;
  private final UserMapper userMapper;

  public TemplateService(TemplateMapper templateMapper, UserMapper userMapper) {
    this.templateMapper = templateMapper;
    this.userMapper = userMapper;
  }

  public List<Template> list(String filter) {
    LambdaQueryWrapper<Template> query = new LambdaQueryWrapper<>();
    if (filter.equals("free")) {
      query.eq(Template::getIsVip, false);
    }
    if (filter.equals("vip")) {
      query.eq(Template::getIsVip, true);
    }
    return templateMapper.selectList(query.orderByAsc(Template::getSort));
  }

  public Template get(Long templateId) {
    Template template = templateMapper.selectById(templateId);
    require(template != null, 404, "模板不存在");
    return template;
  }

  public void checkAccess(Long userId, Long templateId) {
    Template template = get(templateId);
    require(!template.isVip || userMapper.selectById(userId).isMember(), 403, "此模板为会员专享，请先开通会员");
  }

  public void recordUse(Long templateId) {
    templateMapper.update(
        null,
        new LambdaUpdateWrapper<Template>()
            .eq(Template::getId, templateId)
            .setIncrBy(Template::getUsedCount, 1));
  }
}
