import { type CSSProperties } from 'react';
import { sections } from '../model/resumeSections';
import { type Content, type Template } from '../../../types/index';

export function ResumePreview({
  content,
  template,
  watermark = false,
}: {
  content: Content;
  template: Template;
  watermark?: boolean;
}) {
  return (
    <article
      className={`resume-paper template-${template.style}`}
      style={{ '--resume-color': template.color } as CSSProperties}
    >
      <header className="resume-header">
        <div>
          <h1>{content.basic.name || '你的姓名'}</h1>
          <p className="resume-job">求职意向 / {content.basic.job || '意向岗位'}</p>
          <p className="resume-contact">
            {[content.basic.phone, content.basic.email, content.basic.city]
              .filter(Boolean)
              .join('  ·  ')}
          </p>
        </div>
        <div className="resume-monogram">{content.basic.name.slice(-2) || '简历'}</div>
      </header>
      {sections(content).map((section) => (
        <section className="resume-section" key={section.key}>
          {section.text || section.entries?.length || section.skills?.length ? (
            <>
              <h2>{section.title}</h2>
              {section.text && <p className="resume-description">{section.text}</p>}
              {section.entries?.map((entry) => (
                <div className="resume-entry" key={entry.id}>
                  <div className="resume-entry-heading">
                    <strong>{entry.school || entry.company || entry.name}</strong>
                    <span>
                      {entry.start?.replace('-', '.')} {entry.start && '—'}{' '}
                      {entry.end?.replace('-', '.') || '至今'}
                    </span>
                  </div>
                  <p className="resume-subtitle">
                    {[entry.role, entry.major, entry.degree].filter(Boolean).join(' / ')}
                  </p>
                  {entry.desc && (
                    <ul>
                      {entry.desc
                        .split('\n')
                        .filter(Boolean)
                        .map((line, i) => (
                          <li key={i}>{line}</li>
                        ))}
                    </ul>
                  )}
                </div>
              ))}
              {section.skills && (
                <div className="resume-skills">
                  {section.skills.map((skill) => (
                    <span key={skill}>{skill}</span>
                  ))}
                </div>
              )}
            </>
          ) : null}
        </section>
      ))}
      {watermark && <div className="resume-watermark">简历工坊 · 免费版</div>}
    </article>
  );
}
