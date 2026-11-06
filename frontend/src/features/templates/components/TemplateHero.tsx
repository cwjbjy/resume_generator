import { CheckCircle2, Sparkles } from 'lucide-react';

export function TemplateHero() {
  return (
    <section className="template-hero">
      <div className="hero-inner">
        <div className="eyebrow">
          <span /> 为下一段旅程，做好准备
        </div>
        <h1>
          你的经历，值得一份<span>好简历。</span>
        </h1>
        <p>从专业模板开始，把每一份努力变成看得见的竞争力。</p>
        <div className="hero-points">
          <span>
            <CheckCircle2 size={15} />
            在线编辑，实时预览
          </span>
          <span>
            <CheckCircle2 size={15} />
            高清 PDF，一键导出
          </span>
          <span>
            <CheckCircle2 size={15} />
            内容自动保存
          </span>
        </div>
      </div>
      <div className="hero-art" aria-hidden="true">
        <div className="art-orbit" />
        <div className="art-sheet back" />
        <div className="art-sheet front">
          <div className="art-mini-name">
            你的下一站
            <br />
            <b>从这里开始</b>
          </div>
          <div className="art-line blue" />
          <div className="art-line" />
          <div className="art-line short" />
          <div className="art-section" />
          <div className="art-line" />
          <div className="art-line" />
          <div className="art-line short" />
          <div className="art-section" />
          <div className="art-line" />
          <div className="art-line short" />
        </div>
        <div className="art-note">
          <CheckCircle2 size={18} />
          让好机会，看见你
        </div>
        <Sparkles className="art-spark" size={34} />
      </div>
    </section>
  );
}
