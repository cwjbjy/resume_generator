import type { MatchResult } from '../model/types';

const sectionNames: Record<string, string> = {
  basic: '个人简介',
  edu: '教育经历',
  work: '工作经历',
  proj: '项目经历',
  skills: '专业技能',
  custom: '自定义模块',
};

export function MatchAnalysis({ result }: { result: MatchResult }) {
  return (
    <section className="ai-result" aria-label="岗位匹配分析">
      <div className="ai-score">
        <strong>
          {result.score}
          <small> / 100</small>
        </strong>
        <span>AI 参考匹配度</span>
      </div>
      <p>{result.summary}</p>
      {result.matches.length > 0 && (
        <div className="ai-result-section">
          <h3>岗位要求与经历依据</h3>
          {result.matches.map((match, index) => (
            <article key={index} className="ai-evidence">
              <h4>{match.requirement}</h4>
              <p>{match.analysis}</p>
              <small>关联证据 {match.evidenceIds.join('、') || '当前经历中未检索到'}</small>
            </article>
          ))}
        </div>
      )}
      {result.gaps.length > 0 && (
        <div className="ai-result-section">
          <h3>待补充信息</h3>
          <ul>
            {result.gaps.map((gap, index) => (
              <li key={index}>{gap}</li>
            ))}
          </ul>
        </div>
      )}
      {result.suggestions.length > 0 && (
        <div className="ai-result-section">
          <h3>优化建议</h3>
          <ul>
            {result.suggestions.map((suggestion, index) => (
              <li key={index}>{suggestion}</li>
            ))}
          </ul>
        </div>
      )}
      <details className="ai-result-section">
        <summary>查看检索到的简历证据</summary>
        {result.evidence.map((evidence) => (
          <article key={evidence.id} className="ai-evidence">
            <h4>
              {evidence.id} · {sectionNames[evidence.section] ?? evidence.section}
            </h4>
            <p>{evidence.text}</p>
          </article>
        ))}
        {result.evidence.length === 0 && <p>当前简历中没有可用的经历证据。</p>}
      </details>
    </section>
  );
}
