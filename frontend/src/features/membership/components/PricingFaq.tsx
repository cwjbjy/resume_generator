import { Plus } from 'lucide-react';

export function PricingFaq() {
  return (
    <div className="faq">
      <h2>你可能还想了解</h2>
      {[
        [
          '免费账号可以做什么？',
          '可以使用 2 套免费模板、创建和编辑简历，每个自然日可免费导出 1 次带工坊标记的 PDF。',
        ],
        [
          '会员到期后，简历会消失吗？',
          '简历内容会保留。你可以切换到免费模板继续编辑，重新开通会员后可再次使用 VIP 模板和无限导出权益。',
        ],
        [
          '现在会产生真实扣款吗？',
          '当前版本为支付沙箱演示，不会产生真实扣款。支付成功与失败均通过模拟渠道完成。',
        ],
      ].map(([q, a]) => (
        <details key={q}>
          <summary>
            {q}
            <Plus size={16} />
          </summary>
          <p>{a}</p>
        </details>
      ))}
    </div>
  );
}
